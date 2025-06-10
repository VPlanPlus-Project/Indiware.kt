package plus.vplan.lib.indiware.model.wplan.student

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@SerialName("splan")
data class WPlanStudentBaseData(
    @SerialName("Kopf") val head: Head,
    @SerialName("FreieTage")
    @XmlChildrenName("ft")
    val holidays: List<String>,
) {
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