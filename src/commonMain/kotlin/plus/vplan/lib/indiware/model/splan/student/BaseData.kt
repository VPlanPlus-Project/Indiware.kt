package plus.vplan.lib.indiware.model.splan.student

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@SerialName("splan")
data class SPlanBaseDataStudent(
    @SerialName("Kopf") val head: Head,
    @SerialName("FreieTage") @XmlChildrenName("ft")
    val holidays: List<String>,

    @SerialName("Klassen") @XmlChildrenName("Kl")
    val classes: List<SPlanClassStudent> = emptyList(),

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
    @XmlSerialName("Kopf")
    data class Head(
        @XmlElement @SerialName("schulname") val schoolName: String? = null,
    )

    @Serializable
    @XmlSerialName("Kl")
    data class SPlanClassStudent(
        @XmlElement @SerialName("Kurz") val name: String,
        @SerialName("Pl") val plan: Plan? = null
    ) {
        @Serializable
        @XmlSerialName("Pl")
        data class Plan(
            @XmlValue val lessons: List<Lesson>
        ) {
            @Serializable
            @SerialName("Std")
            data class Lesson(
                @XmlElement @SerialName("PlLe") val teacher: String,
                @XmlElement @SerialName("PlRa") val room: String
            )
        }
    }
}