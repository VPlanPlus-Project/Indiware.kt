package plus.vplan.lib.indiware

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import plus.vplan.lib.indiware.source.Authentication
import plus.vplan.lib.indiware.source.IndiwareClient
import plus.vplan.lib.indiware.source.Response
import kotlin.test.Test
import kotlin.test.assertTrue

class CommonGreetingTest {

    @Test
    fun testExample() = runBlocking {
        val client = HttpClient(CIO)
        val indiwareClient = IndiwareClient(
            authentication = Authentication(
                indiwareSchoolId = "10000000",
                username = "schueler",
                password = "123123"
            ),
            client = client
        )
        val result = indiwareClient.getSubstitutionPlan(LocalDate(2025, 5, 26))
        assertTrue(result is Response.Success)
        println(result.data)
        println(Json.encodeToString(result.data.classes.first().lessons.first()))
    }

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