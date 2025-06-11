package plus.vplan.lib.indiware.source

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig.Companion.IGNORING_UNKNOWN_CHILD_HANDLER
import plus.vplan.lib.indiware.model.mobile.student.MobileStudentBaseData
import plus.vplan.lib.indiware.model.mobile.student.MobileStudentData
import plus.vplan.lib.indiware.model.splan.student.SPlanBaseDataStudent
import plus.vplan.lib.indiware.model.vplan.student.VPlanBaseDataStudent
import plus.vplan.lib.indiware.model.wplan.student.WPlanStudentBaseData
import plus.vplan.lib.indiware.model.wplan.student.WPlanStudentData
import kotlin.time.ExperimentalTime

@Suppress("unused")
class IndiwareClient(
    val authentication: Authentication,
    val client: HttpClient = HttpClient()
) {
    constructor(client: HttpClient) : this(
        authentication = Authentication(
            indiwareSchoolId = "000000",
            username = "username",
            password = "password"
        ),
        client = client
    )

    @OptIn(ExperimentalXmlUtilApi::class)
    private val xml: XML = XML {
        xmlVersion = XmlVersion.XML10
        xmlDeclMode = XmlDeclMode.Auto
        repairNamespaces = true
        defaultPolicy {
            unknownChildHandler = IGNORING_UNKNOWN_CHILD_HANDLER
        }
    }

    suspend fun testConnection(
        authentication: Authentication = this.authentication,
    ): TestConnectionResult {
        safeRequest(onError = { return TestConnectionResult.Error(it) }) {
            val response = client.get {
                url(
                    scheme = "https",
                    host = "stundenplan24.de",
                    path = "/${authentication.indiwareSchoolId}/mobil/mobdaten/Klassen.xml"
                )
                authentication.useInRequest(this)
            }

            when (response.status) {
                HttpStatusCode.NotFound -> return TestConnectionResult.NotFound
                HttpStatusCode.Unauthorized -> return TestConnectionResult.Unauthorized
                HttpStatusCode.OK -> return TestConnectionResult.Success
                else -> {
                    val error = response.handleUnsuccessfulStates()
                    return if (error != null) TestConnectionResult.Error(error)
                    else {
                        TestConnectionResult.Error(
                            Response.Error.Other(
                                "Unexpected status code: ${response.status.value} (${response.status.description}) - body: ${response.bodyAsText()}"
                            )
                        )
                    }
                }
            }
        }

        throw IllegalStateException("This should never happen, if it does, please report a bug.")
    }

    suspend fun getMobileBaseDataStudent(
        authentication: Authentication = this.authentication,
    ): Response<MobileStudentBaseData> {
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

            val mobileBaseDataStudent = try {
                xml.decodeFromString(
                    deserializer = MobileStudentBaseData.serializer(),
                    string = response.bodyAsText().sanitizeRawPayload()
                ).copy(raw = response.bodyAsText().sanitizeRawPayload())
            } catch (e: Exception) {
                throw PayloadParsingException(
                    url = response.request.url.toString(),
                    cause = e
                )
            }

            val result = Response.Success(data = mobileBaseDataStudent)
            return result
        }

        throw IllegalStateException("This should never happen, if it does, please report a bug.")
    }

    suspend fun getMobileDataStudent(
        authentication: Authentication = this.authentication,
        date: LocalDate
    ): Response<MobileStudentData> {
        safeRequest(onError = { return it }) {
            val response = client.get {
                url(
                    scheme = "https",
                    host = "stundenplan24.de",
                    path = "/${authentication.indiwareSchoolId}/mobil/mobdaten/PlanKl${date.let { 
                        val format = LocalDate.Format { 
                            year(Padding.ZERO)
                            monthNumber(Padding.ZERO)
                            dayOfMonth(Padding.ZERO)
                        }
                        date.format(format)
                    }}.xml"
                )
                authentication.useInRequest(this)
            }

            response.handleUnsuccessfulStates()?.let { return it }

            val mobileDataStudent = try {
                xml.decodeFromString(
                    deserializer = MobileStudentData.serializer(),
                    string = response.bodyAsText().sanitizeRawPayload()
                ).copy(raw = response.bodyAsText().sanitizeRawPayload())
            } catch (e: Exception) {
                throw PayloadParsingException(
                    url = response.request.url.toString(),
                    cause = e
                )
            }

            return Response.Success(data = mobileDataStudent)
        }

        throw IllegalStateException("This should never happen, if it does, please report a bug.")
    }

    suspend fun getWPlanBaseDataStudent(
        authentication: Authentication = this.authentication
    ): Response<WPlanStudentBaseData> {
        safeRequest(onError = { return it }) {
            val response = client.get {
                url(
                    scheme = "https",
                    host = "stundenplan24.de",
                    path = "/${authentication.indiwareSchoolId}/wplan/wdatenk/SPlanKl_Basis.xml"
                )
                authentication.useInRequest(this)
            }

            response.handleUnsuccessfulStates()?.let { return it }
            val wPlanBaseDataStudent = try {
                xml.decodeFromString(
                    deserializer = WPlanStudentBaseData.serializer(),
                    string = response.bodyAsText().sanitizeRawPayload()
                ).copy(raw = response.bodyAsText().sanitizeRawPayload())
            } catch (e: Exception) {
                throw PayloadParsingException(
                    url = response.request.url.toString(),
                    cause = e
                )
            }

            return Response.Success(data = wPlanBaseDataStudent)
        }

        throw IllegalStateException("This should never happen, if it does, please report a bug.")
    }

    suspend fun getWPlanDataStudent(
        authentication: Authentication = this.authentication,
        date: LocalDate
    ): Response<WPlanStudentData> {
        safeRequest(onError = { return it }) {
            val response = client.get {
                url(
                    scheme = "https",
                    host = "stundenplan24.de",
                    path = "/${authentication.indiwareSchoolId}/wplan/wdatenk/WPlanKl_${date.let {
                        val format = LocalDate.Format { 
                            year(Padding.ZERO)
                            monthNumber(Padding.ZERO)
                            dayOfMonth(Padding.ZERO)
                        }
                        date.format(format)
                    }}.xml"
                )
                authentication.useInRequest(this)
            }

            response.handleUnsuccessfulStates()?.let { return it }
            val wPlanBaseDataStudent = try {
                xml.decodeFromString(
                    deserializer = WPlanStudentData.serializer(),
                    string = response.bodyAsText().sanitizeRawPayload()
                ).copy(raw = response.bodyAsText().sanitizeRawPayload())
            } catch (e: Exception) {
                throw PayloadParsingException(
                    url = response.request.url.toString(),
                    cause = e
                )
            }

            return Response.Success(data = wPlanBaseDataStudent)
        }

        throw IllegalStateException("This should never happen, if it does, please report a bug.")
    }

    suspend fun getVPlanBaseDataStudent(
        authentication: Authentication = this.authentication
    ): Response<VPlanBaseDataStudent> {
        safeRequest(onError = { return it }) {
            val response = client.get {
                url(
                    scheme = "https",
                    host = "stundenplan24.de",
                    path = "/${authentication.indiwareSchoolId}/vplan/vdaten/VplanKl.xml"
                )
                authentication.useInRequest(this)
            }

            response.handleUnsuccessfulStates()?.let { return it }
            val vPlanBaseDataStudent = try {
                xml.decodeFromString(
                    deserializer = VPlanBaseDataStudent.serializer(),
                    string = response.bodyAsText().sanitizeRawPayload()
                ).copy(raw = response.bodyAsText().sanitizeRawPayload())
            } catch (e: Exception) {
                throw PayloadParsingException(
                    url = response.request.url.toString(),
                    cause = e
                )
            }

            return Response.Success(data = vPlanBaseDataStudent)
        }

        throw IllegalStateException("This should never happen, if it does, please report a bug.")
    }

    suspend fun getSPlanBaseDataStudent(
        authentication: Authentication = this.authentication
    ): Response<SPlanBaseDataStudent> {
        safeRequest(onError = { return it }) {
            val response = client.get {
                url(
                    scheme = "https",
                    host = "stundenplan24.de",
                    path = "/${authentication.indiwareSchoolId}/splan/sdaten/splank.xml"
                )
                authentication.useInRequest(this)
            }

            response.handleUnsuccessfulStates()?.let { return it }
            val sPlanBaseDataStudent = try {
                xml.decodeFromString(
                    deserializer = SPlanBaseDataStudent.serializer(),
                    string = response.bodyAsText().sanitizeRawPayload()
                ).copy(raw = response.bodyAsText().sanitizeRawPayload())
            } catch (e: Exception) {
                throw PayloadParsingException(
                    url = response.request.url.toString(),
                    cause = e
                )
            }

            return Response.Success(data = sPlanBaseDataStudent)
        }

        throw IllegalStateException("This should never happen, if it does, please report a bug.")
    }

    suspend fun getHolidays(
        authentication: Authentication = this.authentication
    ): Response<Set<LocalDate>> {
        val baseData = getMobileBaseDataStudent(authentication).let {
            if (it is Response.Success) it.data
            else if (it is Response.Error.OnlineError.NotFound) null
            else return it as Response.Error
        }
        baseData?.holidays?.ifEmpty { null }
            ?.let { return Response.Success(baseData.prettifiedHolidays) }

        val vPlanBaseDataStudent = getVPlanBaseDataStudent(authentication).let {
            if (it is Response.Success) it.data
            else if (it is Response.Error.OnlineError.NotFound) null
            else return it as Response.Error
        }
        vPlanBaseDataStudent?.holidays?.ifEmpty { null }
            ?.let { return Response.Success(vPlanBaseDataStudent.prettifiedHolidays) }

        val wPlanBaseDataStudent = getWPlanBaseDataStudent(authentication).let {
            if (it is Response.Success) it.data
            else if (it is Response.Error.OnlineError.NotFound) null
            else return it as Response.Error
        }
        wPlanBaseDataStudent?.holidays?.ifEmpty { null }
            ?.let { return Response.Success(wPlanBaseDataStudent.prettifiedHolidays) }

        val sPlanBaseDataStudent = getSPlanBaseDataStudent(authentication).let {
            if (it is Response.Success) it.data
            else if (it is Response.Error.OnlineError.NotFound) null
            else return it as Response.Error
        }
        sPlanBaseDataStudent?.holidays?.ifEmpty { null }
            ?.let { return Response.Success(sPlanBaseDataStudent.prettifiedHolidays) }

        return Response.Success(data = emptySet())
    }

    /**
     * Fetches the school name, trying all of the available data sources if necessary.
     * @return If successful, returns the school name as a [Response.Success] with the name as data. If the name is null, it was not found in any of the data sources.
     */
    suspend fun getSchoolName(
        authentication: Authentication = this.authentication
    ): Response<String?> {
        val wPlanStudentBaseData = getWPlanBaseDataStudent(authentication).let {
            if (it is Response.Success) it.data
            else if (it is Response.Error.OnlineError.NotFound) null
            else return it as Response.Error
        }
        wPlanStudentBaseData?.head?.schoolName?.name?.let { return Response.Success(it) }

        val vPlanBaseDataStudent = getVPlanBaseDataStudent(authentication).let {
            if (it is Response.Success) it.data
            else if (it is Response.Error.OnlineError.NotFound) null
            else return it as Response.Error
        }
        vPlanBaseDataStudent?.head?.schoolName?.name?.let { return Response.Success(it) }

        val sPlanBaseDataStudent = getSPlanBaseDataStudent(authentication).let {
            if (it is Response.Success) it.data
            else if (it is Response.Error.OnlineError.NotFound) null
            else return it as Response.Error
        }
        sPlanBaseDataStudent?.head?.schoolName?.name?.let { return Response.Success(it) }

        return Response.Success(null)
    }

    /**
     * Fetches all classes from the available data sources, intelligently combining, deduplicating, and trimming them.
     * @return A [Response.Success] with a set of class names, or an error if any of the data sources fail critically, which means, that a 404 will be tolerated.
     */
    suspend fun getAllClassesIntelligent(
        authentication: Authentication = this.authentication
    ): Response<Set<String>> {
        val classes = mutableSetOf<String>()
        val mobileBaseData = getMobileBaseDataStudent(authentication).let {
            if (it is Response.Success) it.data
            else if (it is Response.Error.OnlineError.NotFound) null
            else return it as Response.Error
        }
        classes.addAll(mobileBaseData?.classes.orEmpty().map { it.name.name })

        val vPlanBaseDataStudent = getVPlanBaseDataStudent(authentication).let {
            if (it is Response.Success) it.data
            else if (it is Response.Error.OnlineError.NotFound) null
            else return it as Response.Error
        }
        classes.addAll(vPlanBaseDataStudent?.actions.orEmpty().map { it.className.name })

        val wPlanStudentBaseData = getWPlanBaseDataStudent(authentication).let {
            if (it is Response.Success) it.data
            else if (it is Response.Error.OnlineError.NotFound) null
            else return it as Response.Error
        }
        classes.addAll(wPlanStudentBaseData?.classes.orEmpty().map { it.name.name })

        val sPlanBaseDataStudent = getSPlanBaseDataStudent(authentication).let {
            if (it is Response.Success) it.data
            else if (it is Response.Error.OnlineError.NotFound) null
            else return it as Response.Error
        }
        classes.addAll(sPlanBaseDataStudent?.classes.orEmpty().map { it.name.name })

        return Response.Success(classes.map { it.trim() }.filterNot { it.isBlank() }.toSet())
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getAllTeachersIntelligent(
        authentication: Authentication = this.authentication
    ): Response<Set<String>> {
        val teachers = mutableSetOf<String>()

        val sPlanBaseDataStudent = getSPlanBaseDataStudent(authentication).let {
            if (it is Response.Success) it.data
            else if (it is Response.Error.OnlineError.NotFound) null
            else return it as Response.Error
        }
        teachers.addAll(sPlanBaseDataStudent?.classes.orEmpty().flatMap { it.lessons.mapNotNull { l -> l.teacher.name.ifBlank { null } } }.flatMap { it.split(",") })

        val weekStart = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.let {
            it.minus(DatePeriod(days = it.dayOfWeek.isoDayNumber.minus(1)))
        }
        if (sPlanBaseDataStudent == null) repeat(5) { i ->
            val date = weekStart.plus(DatePeriod(days = i))
            val data = getMobileDataStudent(authentication = authentication, date = date).let {
                if (it is Response.Success) it.data
                else if (it is Response.Error.OnlineError.NotFound) null
                else return it as Response.Error
            }
            teachers.addAll(data?.classes.orEmpty().flatMap { it.lessons.mapNotNull { l -> l.teacher.name.ifBlank { null } } })

            val wPlanData = getWPlanDataStudent(authentication = authentication, date = date).let {
                if (it is Response.Success) it.data
                else if (it is Response.Error.OnlineError.NotFound) null
                else return it as Response.Error
            }
            teachers.addAll(wPlanData?.classes.orEmpty().flatMap { it.subjectInstanceWrapper.mapNotNull { si -> si.subjectInstance.teacherName?.ifBlank { null } } })
            teachers.addAll(wPlanData?.classes.orEmpty().flatMap { it.lessons.mapNotNull { l -> l.teacher.name.ifBlank { null } } })
        }

        return Response.Success(teachers.handleSchoolEntities())
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getAllRoomsIntelligent(
        authentication: Authentication = this.authentication
    ): Response<Set<String>> {
        val rooms = mutableSetOf<String>()

        val sPlanBaseDataStudent = getSPlanBaseDataStudent(authentication).let {
            if (it is Response.Success) it.data
            else if (it is Response.Error.OnlineError.NotFound) null
            else return it as Response.Error
        }
        rooms.addAll(sPlanBaseDataStudent?.classes.orEmpty().flatMap { it.lessons.mapNotNull { l -> l.room.name.ifBlank { null } } }.flatMap { it.split(",") })

        val weekStart = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.let {
            it.minus(DatePeriod(days = it.dayOfWeek.isoDayNumber.minus(1)))
        }
        if (sPlanBaseDataStudent == null) repeat(5) { i ->
            val date = weekStart.plus(DatePeriod(days = i))
            val data = getMobileDataStudent(authentication = authentication, date = date).let {
                if (it is Response.Success) it.data
                else if (it is Response.Error.OnlineError.NotFound) null
                else return it as Response.Error
            }
            rooms.addAll(data?.classes.orEmpty().flatMap { it.lessons.mapNotNull { l -> l.room.name.ifBlank { null } } })

            val wPlanData = getWPlanDataStudent(authentication = authentication, date = date).let {
                if (it is Response.Success) it.data
                else if (it is Response.Error.OnlineError.NotFound) null
                else return it as Response.Error
            }
            rooms.addAll(wPlanData?.classes.orEmpty().flatMap { it.lessons.mapNotNull { l -> l.room.name.ifBlank { null } } })
        }

        return Response.Success(rooms.handleSchoolEntities())
    }
}

fun Set<String>.handleSchoolEntities() =
    this
        .map { it.trim() }
        .filterNot { it.isBlank() }
        .filterNot { it.matches(Regex("-+")) }
        .filterNot { it == "&nbsp;" }
        .map { it.dropWhile { it == '-' || it == ' ' } }
        .map { it.dropLastWhile { it == '-' || it == ' ' } }
        .sorted()
        .let { items ->
            items.filter { name ->
                if (' ' !in name && '-' !in name) true
                else {
                    var toConsume = name
                    val itemMap: MutableMap<String, Boolean?> = items.filter { it != name }.associateWith { null }.toMutableMap()
                    fun consumeNext(): Boolean {
                        val oldValue = toConsume
                        val candidates = itemMap.filterValues { it == null }.filterKeys { toConsume.startsWith(it) }
                        if (candidates.isEmpty()) return true
                        val candidate = candidates.keys.first()
                        itemMap[candidate] = true
                        toConsume = toConsume.substring(candidate.length).trimStart()
                        if (toConsume.isEmpty()) return false
                        if (!consumeNext()) {
                            itemMap[candidate] = false
                            toConsume = oldValue
                            return false
                        }
                        return toConsume.isNotEmpty()
                    }
                    consumeNext()
                }
            }
        }
        .toSet()


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

class PayloadParsingException(
    url: String,
    cause: Throwable? = null
) : Exception() {
    override val message: String? =
        "Failed to parse payload from $url. This is unexpected.\nPlease file a bug report at the official repository at $PROJECT_URL:\n${cause?.stackTraceToString()}"
}

internal const val PROJECT_URL =
    "https://gitlab.jvbabi.es/vplanplus/lib/Indiware-kt or https://github.com/VPlanPlus-Project/Indiware.kt"

internal fun String.sanitizeRawPayload() =
    this
        .dropWhile { it != '<' }
        .dropLastWhile { it != '>' }
        .lines()
        .joinToString("\n") { it.trim() }

sealed class TestConnectionResult {
    data object NotFound : TestConnectionResult()
    data object Unauthorized : TestConnectionResult()
    data object Success : TestConnectionResult()
    data class Error(val error: Response.Error) : TestConnectionResult()
}