package plus.vplan.lib.indiware.source

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atDate
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig.Companion.IGNORING_UNKNOWN_CHILD_HANDLER
import plus.vplan.lib.indiware.model.common.BaseData
import plus.vplan.lib.indiware.model.common.SubstitutionPlan
import plus.vplan.lib.indiware.model.common.SubstitutionPlanLesson
import plus.vplan.lib.indiware.model.mobile.student.MobileStudentBaseData
import plus.vplan.lib.indiware.model.mobile.student.VPlan

@Suppress("unused")
class IndiwareClient(
    val authentication: Authentication,
    val client: HttpClient = HttpClient()
) {
    @OptIn(ExperimentalXmlUtilApi::class)
    private val xml: XML = XML {
        xmlVersion = XmlVersion.XML10
        xmlDeclMode = XmlDeclMode.Auto
        indentString = "  "
        repairNamespaces = true
        defaultPolicy {
            unknownChildHandler = IGNORING_UNKNOWN_CHILD_HANDLER
        }
    }

    suspend fun getSubstitutionPlan(
        date: LocalDate,
        authentication: Authentication = this.authentication,
        knownRoomNames: List<String> = emptyList(),
        knownTeacherNames: List<String> = emptyList()
    ): Response<SubstitutionPlan> {
        safeRequest(onError = { return it }) {
            val response = client.get {
                url(
                    scheme = "https",
                    host = "stundenplan24.de",
                    path = "/${authentication.indiwareSchoolId}/mobil/mobdaten/PlanKl${date.format(LocalDate.Format {
                        year()
                        monthNumber(Padding.ZERO)
                        dayOfMonth(Padding.ZERO)
                    })}.xml"
                )
                authentication.useInRequest(this)
            }
            response.handleUnsuccessfulStates()?.let { return it }
            val substitutionPlan = xml.decodeFromString(
                deserializer = VPlan.serializer(),
                string = response.bodyAsText().dropWhile { it != '<' }
            )

            val createdAtFormat = LocalDateTime.Format {
                dayOfMonth(Padding.ZERO)
                char('.')
                monthNumber(Padding.ZERO)
                char('.')
                year()
                chars(", ")
                hour(Padding.ZERO)
                char(':')
                minute(Padding.ZERO)
            }

            val createdAt = LocalDateTime.parse(
                substitutionPlan.head.timestamp.value,
                format = createdAtFormat
            )

            return Response.Success(
                data = SubstitutionPlan(
                    date = date,
                    info = substitutionPlan.info.joinToString("\n").ifBlank { null },
                    classes = substitutionPlan.classes.map { clazz ->
                        SubstitutionPlan.SubstitutionPlanClass(
                            name = clazz.name.name,
                            lessons = clazz.lessons.map { lesson ->
                                SubstitutionPlanLesson(
                                    lessonNumber = lesson.lessonNumber.value,
                                    subject = lesson.subject.value.let {
                                        if (it == "---") return@let null
                                        else return@let it
                                    },
                                    isSubjectChanged = lesson.subject.changed.orEmpty().isNotBlank(),
                                    teachers = lesson.teacher.value.splitWithKnownValuesBySpace(knownTeacherNames),
                                    areTeachersChanged = lesson.teacher.changed.orEmpty().isNotBlank(),
                                    rooms = lesson.room.value.splitWithKnownValuesBySpace(knownRoomNames),
                                    areRoomsChanged = lesson.room.changed.orEmpty().isNotBlank(),
                                    info = lesson.info.value.ifBlank { null },
                                    start = LocalTime.parseOrNull(lesson.start.value)?.atDate(date),
                                    end = LocalTime.parseOrNull(lesson.end.value)?.atDate(date),
                                    date = date
                                )
                            }
                        )
                    },
                    createdAt = createdAt
                )
            )

        }
        throw IllegalStateException()
    }

    suspend fun getBaseDataStudentMobile(
        authentication: Authentication = this.authentication,
        knownRoomNames: List<String> = emptyList(),
        knownTeacherNames: List<String> = emptyList()
    ): Response<BaseData> {
        safeRequest(onError = { return it }) {
            val response = client.get {
                url(
                    scheme = "https",
                    host = "stundenplan24.de",
                    path = "/${authentication.indiwareSchoolId}/mobil/mobdaten/Klassen.xml"
                )
                authentication.useInRequest(this)
            }
            response.handleUnsuccessfulStates()?.let { return it }

            val baseData = xml.decodeFromString<MobileStudentBaseData>(
                deserializer = MobileStudentBaseData.serializer(),
                string = response.bodyAsText().dropWhile { it != '<' }
            )

            val holidayFormat = LocalDate.Format {
                yearTwoDigits(2000)
                monthNumber()
                dayOfMonth()
            }

            return Response.Success(
                data = BaseData(
                    schoolName = null,
                    holidays = baseData.holidays.map { LocalDate.parse(it, holidayFormat) },
                    classes = baseData.classes.map { baseDataClass ->
                        BaseData.BaseDataClass(
                            name = baseDataClass.name.name,
                            lessonTimes = baseDataClass.lessonTimes.map { lessonTime ->
                                BaseData.BaseDataClass.BaseDataLessonTime(
                                    number = lessonTime.lessonNumber,
                                    start = LocalTime.parse(lessonTime.startTime),
                                    end = LocalTime.parse(lessonTime.startTime)
                                )
                            },
                            subjectInstances = baseDataClass.subjectInstances.map { subjectInstance ->
                                BaseData.BaseDataClass.BaseDataSubjectInstance(
                                    subject = subjectInstance.subjectInstance.subjectName,
                                    teachers = subjectInstance.subjectInstance.teacherName.splitWithKnownValuesBySpace(knownTeacherNames),
                                )
                            },
                            courses = baseDataClass.courses.map { course ->
                                BaseData.BaseDataClass.BaseDataCourse(
                                    name = course.course.courseName,
                                    teachers = course.course.courseTeacherName.splitWithKnownValuesBySpace(knownTeacherNames)
                                )
                            }
                        )
                    }
                )
            )
        }
        throw IllegalStateException()
    }
}

internal inline fun safeRequest(
    onError: (error: Response.Error) -> Unit,
    request: () -> Unit
) {
    try {
        request()
    } catch (e: Exception) {
        onError(
            when (e) {
                is ClientRequestException, is HttpRequestTimeoutException -> Response.Error.OnlineError.ConnectionError
                is ServerResponseException -> Response.Error.Other(e.message)
                is CancellationException -> Response.Error.Cancelled
                else -> Response.Error.Other(e.stackTraceToString())
            }
        )
    }
}

internal fun String.splitWithKnownValuesBySpace(values: List<String>): List<String> {
    if (values.isEmpty()) {
        return if (this.contains(",")) this.split(",").map { it.trim() }
        else this.split(" ").map { it.trim() }.filter { it.isNotBlank() }
    }
    val regex = Regex(values.joinToString("|") { Regex.escape(it) })
    val matches = mutableListOf<String>()
    var remaining = this
    while (true) {
        val match = regex.find(remaining) ?: break
        matches.add(match.value)
        remaining = remaining.removeRange(match.range).trim()
    }

    return if (remaining.isEmpty()) matches else emptyList()
}

internal fun LocalTime.Companion.parseOrNull(time: String): LocalTime? {
    return try {
        parse(time)
    } catch (_: IllegalArgumentException) {
        null
    }
}

internal suspend inline fun HttpResponse.handleUnsuccessfulStates(): Response.Error? {
    if (this.status == HttpStatusCode.Unauthorized) return Response.Error.OnlineError.Unauthorized
    if (this.status == HttpStatusCode.NotFound) return Response.Error.OnlineError.NotFound
    if (this.status != HttpStatusCode.OK) {
        return Response.Error.Other(
            "Unexpected status code: ${this.status.value} (${this.status.description}) - body: ${this.bodyAsText()}"
        )
    }
    return null
}