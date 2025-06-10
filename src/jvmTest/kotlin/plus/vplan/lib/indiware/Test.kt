package plus.vplan.lib.indiware

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.runBlocking
import plus.vplan.lib.indiware.source.Authentication
import plus.vplan.lib.indiware.source.IndiwareClient
import java.io.File
import kotlin.test.Test

class Test {
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

    val indiware = IndiwareClient(client = client)

    @Test
    fun `Get school names`() {
        val file = File("./access.csv")
        if (!file.exists()) {
            Napier.e("File not found: ${file.absolutePath}")
            return
        }

        val lines = file.readLines().drop(1)
        val accessList = lines.map {
            val (indiwareId, username, password, _) = it.split(",")
            Authentication(indiwareId, username.replace("\"", ""), password.replace("\"", ""))
        }
        runBlocking {
            accessList.filter { it.indiwareSchoolId == "10233497" }.forEach { access ->
                val schoolName = indiware.getSchoolName(access)
                println("${access.indiwareSchoolId}: $schoolName")
            }
        }
    }
}