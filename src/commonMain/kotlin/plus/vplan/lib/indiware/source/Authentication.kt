package plus.vplan.lib.indiware.source

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.basicAuth

/**
 * Used to authenticate against the stundenplan24.de-Service.
 * @param indiwareSchoolId The 8-digit school ID used to identify the school. Is a string because we don't trust Indiware about schools not starting with zeros. We haven't encountered them yet but who knows what the future brings.
 * @param username The username of the user. Usually `schueler` or `lehrer`.
 * @param password The password provided by the school.
 */
data class Authentication(
    val indiwareSchoolId: String,
    val username: String,
    val password: String,
) {
    internal fun useInRequest(requestBuilder: HttpRequestBuilder) {
        requestBuilder.basicAuth(username, password)
    }
}