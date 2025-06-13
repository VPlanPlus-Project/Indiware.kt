package plus.vplan.lib.indiware

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import plus.vplan.lib.indiware.source.Response

class IntelligentTest : FunSpec() {
    init {
        test("Get classes") {
            val response = commonIndiwareClient.getAllClassesIntelligent()
            val classes = (response as? Response.Success)?.data

            classes shouldNotBeNull {
                this.isEmpty() shouldBe false
                this.forAll {
                    it shouldNotBe ""
                }
            }
        }
    }
}