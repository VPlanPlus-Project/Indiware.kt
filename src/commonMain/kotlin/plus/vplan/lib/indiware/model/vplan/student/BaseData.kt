package plus.vplan.lib.indiware.model.vplan.student

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@SerialName("vp")
data class VPlanBaseDataStudent(
    @SerialName("kopf") val head: Head,
    @SerialName("freietage")
    @XmlChildrenName("ft") val holidays: List<String>,
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
    @SerialName("kopf")
    data class Head(
        @SerialName("schulname") val schoolName: SchoolName? = null
    ) {
        @Serializable
        @SerialName("schulname")
        data class SchoolName(
            @XmlValue val name: String
        )
    }
}