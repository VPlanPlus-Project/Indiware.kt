package plus.vplan.lib.indiware

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeTypeOf
import plus.vplan.lib.indiware.source.Response

class SchoolEntityTest : FunSpec() {
    init {
        test("Get classes") {
            val response = commonIndiwareClient.getAllClassesIntelligent()
            val classes = (response as? Response.Success)?.data

            classes shouldNotBeNull {
                this.shouldNotBeEmpty()
                this.forAll {
                    it shouldNotBe ""
                }
            }
        }

        test("Get teachers") {
            val response = commonIndiwareClient.getAllTeachersIntelligent()
            response.shouldBeTypeOf<Response.Success<Set<String>>>()

            response.data shouldNotBeNull {
                this.shouldNotBeEmpty()
                this.forAll {
                    it shouldNotBe ""
                }
            }
        }
    }
}