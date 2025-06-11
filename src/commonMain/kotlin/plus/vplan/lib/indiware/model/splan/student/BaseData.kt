package plus.vplan.lib.indiware.model.splan.student

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@SerialName("splan")
data class SPlanBaseDataStudent(
    @SerialName("Kopf") val head: Head,
    @SerialName("FreieTage")
    @XmlChildrenName("ft")
    val holidays: List<String>,

    @SerialName("Klassen")
    @XmlChildrenName("Kl")
    val classes: List<SPlanClassStudent>,

    @Transient val raw: String = ""
) {

    companion object {
        private val holidayFormat = LocalDate.Format {
            yearTwoDigits(2000)
            monthNumber()
            dayOfMonth()
        }
    }
    val prettifiedHolidays: Set<LocalDate> = this.holidays.map { LocalDate.parse(it, holidayFormat) }.toSet()

    @Serializable
    @SerialName("Kopf")
    data class Head(
        @SerialName("schulname") val schoolName: SchoolName? = null
    ) {
        @Serializable
        @SerialName("schulname")
        data class SchoolName(
            @XmlValue val name: String
        )
    }

    @Serializable
    @SerialName("Kl")
    data class SPlanClassStudent(
        @SerialName("Kurz") val name: ClassName,
        @SerialName("Pl")
        @XmlChildrenName("Std")
        val lessons: List<SPlanLessonStudent>
    ) {
        @Serializable
        @SerialName("Kurz")
        data class ClassName(
            @XmlValue val name: String
        )

        @Serializable
        @SerialName("Std")
        data class SPlanLessonStudent(
            @SerialName("PlLe") val teacher: Teacher,
            @SerialName("PlRa") val room: Room
        ) {
            @Serializable
            @SerialName("PlLe")
            data class Teacher(
                @XmlValue val name: String
            )

            @Serializable
            @SerialName("PlRa")
            data class Room(
                @XmlValue val name: String
            )
        }
    }
}