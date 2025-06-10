package plus.vplan.lib.indiware

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking
import plus.vplan.lib.indiware.source.Authentication
import plus.vplan.lib.indiware.source.IndiwareClient
import plus.vplan.lib.indiware.source.Response
import kotlin.test.Test
import kotlin.test.assertTrue

class CommonGreetingTest {
    @Test
    fun exampleMobdaten() {
        val client = HttpClient(CIO)
        val indiwareClient = IndiwareClient(
            authentication = Authentication(
                indiwareSchoolId = "10000000",
                username = "schueler",
                password = "123123"
            ),
            client = client
        )
        runBlocking {
            val result = indiwareClient.getBaseDataStudentMobile()
            assertTrue(result is Response.Success)
            println(result.data)
        }
    }
}