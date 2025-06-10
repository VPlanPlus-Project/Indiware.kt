package plus.vplan.lib.indiware

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import plus.vplan.lib.indiware.source.Authentication
import plus.vplan.lib.indiware.source.IndiwareClient

val client = HttpClient(CIO) {
    install(HttpCache)
    install(Logging) {
        logger = object: Logger {
            override fun log(message: String) {
                Napier.v("HTTP Client", null, message)
            }
        }
        level = LogLevel.HEADERS
    }
}

val indiwareClient = IndiwareClient(
    authentication = Authentication(
        indiwareSchoolId = "10000000",
        username = "schueler",
        password = "123123"
    ),
    client = client
)