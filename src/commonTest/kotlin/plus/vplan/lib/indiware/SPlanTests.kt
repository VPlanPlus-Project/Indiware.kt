package plus.vplan.lib.indiware

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAtLeastOne
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeTypeOf
import plus.vplan.lib.indiware.model.splan.student.SPlanBaseDataStudent
import plus.vplan.lib.indiware.source.Response

class SPlanTests : FunSpec() {
    init {
        test("SPlan tests base data") {
            val baseData = commonIndiwareClient.getSPlanBaseDataStudent(getSPlanSchool())
            baseData.shouldBeTypeOf<Response.Success<SPlanBaseDataStudent>>()

            baseData.data.holidays.shouldNotBeEmpty()

            baseData.data.classes.forAtLeastOne {
                it.plan?.lessons.orEmpty().shouldNotBeEmpty()
            }
        }
    }
}