package plus.vplan.lib.indiware

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeTypeOf
import plus.vplan.lib.indiware.source.IndiwareClient
import plus.vplan.lib.indiware.source.Response

class SubjectInstanceTest : FunSpec() {
    init {
        test("Get subject instances") {
            val subjectInstances = commonIndiwareClient.getAllSubjectInstances()
            subjectInstances.shouldBeTypeOf<Response.Success<Map<String, List<IndiwareClient.SubjectInstance>>>>()

            val courses = commonIndiwareClient.getAllCourses()
            courses.shouldBeTypeOf<Response.Success<Map<String, List<IndiwareClient.Course>>>>()

            val teachers = commonIndiwareClient.getAllTeachersIntelligent()
            teachers.shouldBeTypeOf<Response.Success<Set<String>>>()
            teachers.data.shouldNotBeEmpty()

            subjectInstances.data.forEach { (className, instances) ->
                val coursesForClass = (courses.data[className] ?: emptyList()).map { it.name }
                instances.forAll { instance ->
                    instance.subject.shouldNotBeEmpty()
                    if (instance.teacher != null) instance.teacher shouldBeIn teachers.data
                    if (instance.course != null) instance.course shouldBeIn coursesForClass
                }
            }
        }
    }
}