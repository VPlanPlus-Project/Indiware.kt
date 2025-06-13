package plus.vplan.lib.indiware

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.runBlocking
import plus.vplan.lib.indiware.source.IndiwareClient
import plus.vplan.lib.indiware.source.Response

class CoursesTests : FunSpec() {
    init {
        test("Get courses") {
            runBlocking {
                val classCourses = commonIndiwareClient.getAllCourses()
                classCourses.shouldBeTypeOf<Response.Success<Map<String, List<IndiwareClient.Course>>>>()

                val classes = commonIndiwareClient.getAllClassesIntelligent()
                classes.shouldBeTypeOf<Response.Success<Set<String>>>()
                classes.data.shouldNotBeEmpty()

                val teachers = commonIndiwareClient.getAllTeachersIntelligent()
                teachers.shouldBeTypeOf<Response.Success<Set<String>>>()
                teachers.data.shouldNotBeEmpty()

                classCourses.data.forAll { (className, courses) ->
                    className shouldBeIn classes.data
                    courses.forAll { course ->
                        course.teacher shouldBeIn teachers.data
                    }
                }
            }
        }
    }
}