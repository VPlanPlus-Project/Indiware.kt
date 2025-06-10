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
import plus.vplan.lib.indiware.source.Response
import kotlin.test.Test
import kotlin.test.assertTrue

class CommonGreetingTest {
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

    @Test
    fun exampleMobdaten() {
        runBlocking {
            val result = indiwareClient.getMobileBaseDataStudent()
            assertTrue(result is Response.Success)
            println(result.data)
        }
    }

    @Test
    fun `Get holidays`() = runBlocking {
        val holidays = indiwareClient.getHolidays()
        assertTrue(holidays is Response.Success)
        println(holidays.data)
    }

    @Test
    fun `Get mobile base data`() = runBlocking {
        val baseData = indiwareClient.getMobileBaseDataStudent()
        assertTrue(baseData is Response.Success)
        println(baseData.data)
    }

    @Test
    fun `Get wplan student base data`() = runBlocking {
        val baseData = indiwareClient.getWPlanBaseDataStudent()
        assertTrue(baseData is Response.Success)
        println(baseData.data)
    }
}