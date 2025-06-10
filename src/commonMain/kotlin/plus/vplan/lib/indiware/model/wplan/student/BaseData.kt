package plus.vplan.lib.indiware.model.wplan.student

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@SerialName("splan")
data class WPlanStudentBaseData(
    @SerialName("Kopf") val head: Head,
    @SerialName("FreieTage")
    @XmlChildrenName("ft")
    val holidays: List<String>,

    @Transient
    val raw: String = ""
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
        @SerialName("schulname") val schoolName: SchoolName? = null,
    ) {
        @Serializable
        @SerialName("schulname")
        data class SchoolName(
            @XmlValue val name: String
        )
    }
}