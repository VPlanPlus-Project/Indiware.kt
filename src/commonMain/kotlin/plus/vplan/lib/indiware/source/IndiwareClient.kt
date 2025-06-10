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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig.Companion.IGNORING_UNKNOWN_CHILD_HANDLER
import plus.vplan.lib.indiware.model.mobile.student.MobileStudentBaseData
import plus.vplan.lib.indiware.model.mobile.student.MobileStudentData
import plus.vplan.lib.indiware.model.vplan.student.VPlanBaseDataStudent
import plus.vplan.lib.indiware.model.wplan.student.WPlanStudentBaseData

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
    ): Response<Unit> {
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

            return if (response.status == HttpStatusCode.OK) {
                Response.Success(data = Unit)
            } else {
                Response.Error.Other("Unexpected status code: ${response.status.value} (${response.status.description})")
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

        return Response.Success(null)
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