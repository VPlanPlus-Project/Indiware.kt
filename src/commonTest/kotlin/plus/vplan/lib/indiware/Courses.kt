package plus.vplan.lib.indiware

import kotlinx.coroutines.runBlocking
import plus.vplan.lib.indiware.source.Response
import kotlin.test.Test

class Courses {
    @Test
    fun `Get courses`() = runBlocking {
        val classCourses = (commonIndiwareClient.getAllCourses() as Response.Success).data
        val classes = (commonIndiwareClient.getAllClassesIntelligent() as Response.Success).data
        val teachers = (commonIndiwareClient.getAllTeachersIntelligent() as Response.Success).data

        classCourses.forEach { classCourse ->

        }
    }
}