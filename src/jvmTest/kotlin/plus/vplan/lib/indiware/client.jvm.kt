package plus.vplan.lib.indiware

import plus.vplan.lib.indiware.source.Authentication
import plus.vplan.lib.indiware.source.IndiwareClient
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2

actual fun getClient(): IndiwareClient{
    System.setProperty("kotest.assertions.collection.print.size", "1000")
    return indiwareClient
}

actual fun getWPlanSchool(): Authentication {
    val file = File("./10063764.txt")
    val (username, password) = file.readLines()
    return Authentication(
        indiwareSchoolId = "10063764",
        username = username,
        password = password
    )
}

actual fun getSPlanSchool(): Authentication {
    val file = File("./20299165.txt")
    val (username, password) = file.readLines()
    return Authentication(
        indiwareSchoolId = "20299165",
        username = username,
        password = password
    )
}