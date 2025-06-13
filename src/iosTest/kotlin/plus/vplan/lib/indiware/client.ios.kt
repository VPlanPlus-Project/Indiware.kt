package plus.vplan.lib.indiware

import plus.vplan.lib.indiware.source.Authentication
import plus.vplan.lib.indiware.source.IndiwareClient

actual fun getClient(): IndiwareClient {
    TODO("Not yet implemented")
}

actual fun getWPlanSchool(): Authentication {
    throw RuntimeException("Cannot load credentials on iOS tests. Use the JVM tests to load credentials from a file.")
}

actual fun getSPlanSchool(): Authentication {
    throw RuntimeException("Cannot load credentials on iOS tests. Use the JVM tests to load credentials from a file.")
}