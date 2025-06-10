package plus.vplan.lib.indiware

import kotlinx.coroutines.runBlocking
import plus.vplan.lib.indiware.source.Authentication
import plus.vplan.lib.indiware.source.TestConnectionResult
import kotlin.test.Test
import kotlin.test.assertTrue

class ConnectionTests {
    @Test
    fun `Test non-existent connection`() = runBlocking {
        val response = indiwareClient.testConnection(Authentication("00000000", "invalidUser", "invalidPass"))
        assertTrue(response is TestConnectionResult.NotFound)
    }

    @Test
    fun `Test invalid credentials`() = runBlocking {
        val response = indiwareClient.testConnection(Authentication("10063764", "invalidUser", "invalidPass"))
        assertTrue(response is TestConnectionResult.Unauthorized)
    }

    @Test
    fun `Test valid connection`() = runBlocking {
        val response = indiwareClient.testConnection(Authentication("10000000", "schueler", "123123"))
        assertTrue(response is TestConnectionResult.Success)
    }
}