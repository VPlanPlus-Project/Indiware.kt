package plus.vplan.lib.indiware

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
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
            val holidays = commonIndiwareClient.getHolidays(getWPlanSchool())
            holidays.shouldBeTypeOf<Response.Success<Set<LocalDate>>>()
            val date = run {
                var date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                while (date in holidays.data || date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
                    date = date.minus(DatePeriod(days = 1))
                }
                date
            }
            val today = commonIndiwareClient.getWPlanDataStudent(authentication = getWPlanSchool(), date = date) as? Response.Success
            today shouldNotBeNull {
                this.data.classes.isEmpty() shouldBe false
            }
        }
    }
}