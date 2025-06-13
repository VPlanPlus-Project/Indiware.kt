package plus.vplan.lib.indiware

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import plus.vplan.lib.indiware.source.Authentication
import plus.vplan.lib.indiware.source.TestConnectionResult

class TestConnection : FunSpec() {
    init {
        test("Non-existent connection") {
            commonIndiwareClient.testConnection(Authentication("00000000", "invalidUser", "invalidPass")) shouldBe TestConnectionResult.NotFound
        }

        test("Invalid credentials") {
            val response = commonIndiwareClient.testConnection(Authentication("10063764", "invalidUser", "invalidPass"))
            response shouldBe TestConnectionResult.Unauthorized
        }

        test("Valid connection") {
            val response = commonIndiwareClient.testConnection(Authentication("10000000", "schueler", "123123"))
            response shouldBe TestConnectionResult.Success
        }
    }
}