package plus.vplan.lib.indiware

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.lib.indiware.source.Response

class WPlanTests : FunSpec() {
    init {
        test("Get wplan base") {
            val baseData = commonIndiwareClient.getWPlanBaseDataStudent() as? Response.Success
            baseData shouldNotBeNull {
                this.data.classes.isEmpty() shouldBe false
            }
        }

        test("Get wplan for today") {
            val today = commonIndiwareClient.getWPlanDataStudent(authentication = getWPlanSchool(), date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) as? Response.Success
            today shouldNotBeNull {
                this.data.classes.isEmpty() shouldBe false
            }
        }
    }
}