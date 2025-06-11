package plus.vplan.lib.indiware

import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import plus.vplan.lib.indiware.source.Authentication
import plus.vplan.lib.indiware.source.Response
import java.io.File
import kotlin.test.Test

class Test {

    @Test
    fun `Test raw data`() = runBlocking {
        val mobile = (indiwareClient.getMobileBaseDataStudent() as Response.Success).data.raw
        assert(mobile.startsWith("<?xml") && mobile.endsWith(">"))

        val wplan = (indiwareClient.getWPlanBaseDataStudent() as Response.Success).data.raw
        assert(wplan.startsWith("<?xml") && wplan.endsWith(">"))

        val vplan = (indiwareClient.getVPlanBaseDataStudent() as Response.Success).data.raw
        assert(vplan.startsWith("<?xml") && vplan.endsWith(">"))
    }

    @Test
    fun `Get school names`() {
        val file = File("./access.csv")
        if (!file.exists()) {
            Napier.e("File not found: ${file.absolutePath}")
            return
        }

        val lines = file.readLines().drop(1)
        val accessList = lines.map {
            val (indiwareId, username, password, _) = it.split(",")
            Authentication(indiwareId, username.replace("\"", ""), password.replace("\"", ""))
        }
        runBlocking {
            accessList.forEach { access ->
                val schoolName = indiwareClient.getSchoolName(access)
                println("${access.indiwareSchoolId}: $schoolName")
            }
        }
    }

    @Test
    fun `Get classes`() {
        val file = File("./access.csv")
        if (!file.exists()) {
            Napier.e("File not found: ${file.absolutePath}")
            return
        }

        val lines = file.readLines().drop(1)
        val accessList = lines.map {
            val (indiwareId, username, password, _) = it.split(",")
            Authentication(indiwareId, username.replace("\"", ""), password.replace("\"", ""))
        }
        runBlocking {
            accessList.forEach { access ->
                val classes = indiwareClient.getAllClassesIntelligent(access) as? Response.Success
                println("School ID: ${access.indiwareSchoolId}, Classes: ${classes?.data?.joinToString()}")
            }
        }
    }

    @Test
    fun `Get teachers`() {
        val file = File("./access.csv")
        if (!file.exists()) {
            Napier.e("File not found: ${file.absolutePath}")
            return
        }

        val lines = file.readLines().drop(1)
        val accessList = lines.map {
            val (indiwareId, username, password, _) = it.split(",")
            Authentication(indiwareId, username.replace("\"", ""), password.replace("\"", ""))
        }
        runBlocking {
            accessList.forEach { access ->
                val teachers = indiwareClient.getAllTeachersIntelligent(access).let {
                    if (it !is Response.Success) println(it)
                    it as? Response.Success
                }
                println("School ID: ${access.indiwareSchoolId}, Teachers: ${teachers?.data?.joinToString()}")
            }
        }
    }

    @Test
    fun `Get rooms`() {
        val file = File("./access.csv")
        if (!file.exists()) {
            Napier.e("File not found: ${file.absolutePath}")
            return
        }

        val lines = file.readLines().drop(1)
        val accessList = lines.map {
            val (indiwareId, username, password, _) = it.split(",")
            Authentication(indiwareId, username.replace("\"", ""), password.replace("\"", ""))
        }
        runBlocking {
            accessList.forEach { access ->
                val rooms = indiwareClient.getAllRoomsIntelligent(access).let {
                    if (it !is Response.Success) println(it)
                    it as? Response.Success
                }
                println("School ID: ${access.indiwareSchoolId}, Rooms: ${rooms?.data?.joinToString()}")
            }
        }
    }

    @Test
    fun `Test data`() = runBlocking {
        val mobile = indiwareClient.getMobileDataStudent(date = LocalDate(2025, 6, 10))
        println(mobile)
    }
}