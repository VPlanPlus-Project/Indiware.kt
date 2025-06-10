package plus.vplan.lib.indiware

import kotlinx.coroutines.runBlocking
import plus.vplan.lib.indiware.source.Response
import kotlin.test.Test
import kotlin.test.assertTrue

class IntelligentTest {
    @Test
    fun `Get classes`() = runBlocking {
        val classes = (indiwareClient.getAllClassesIntelligent() as Response.Success).data
        println(classes)
        assertTrue("Classes should not be empty") { classes.isNotEmpty() }
    }
}