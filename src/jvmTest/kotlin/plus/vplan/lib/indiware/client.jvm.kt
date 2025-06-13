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
    return getAuthFromFile("10063764")
}

actual fun getSPlanSchool(): Authentication {
    return getAuthFromFile("20299165")
}

private fun getAuthFromFile(schoolId: String): Authentication {
    val file = File("$schoolId.txt")
    if (!file.exists()) {
        throw IllegalStateException("Authentication file for school ID $schoolId does not exist.")
    }
    val (username, password) = try {
        file.readLines().first().split(" ")
    } catch (e: Exception) {
        throw IllegalStateException("Failed to read authentication details from file: ${file.path}\n${file.readText()}", e)
    }

    return Authentication(
        indiwareSchoolId = schoolId,
        username = username,
        password = password
    )
}