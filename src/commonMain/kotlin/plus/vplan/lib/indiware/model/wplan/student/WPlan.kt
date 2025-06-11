package plus.vplan.lib.indiware.model.wplan.student

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@SerialName("WplanVp")
data class WPlanStudentData(
    @SerialName("Klassen") @XmlChildrenName("Kl") val classes: List<Class>,

    @Transient val raw: String = "",
) {

    @SerialName("Kl")
    @Serializable
    data class Class(
        @SerialName("Unterricht") @XmlChildrenName("Ue") val subjectInstanceWrapper: List<SubjectInstanceWrapper>,
        @SerialName("Pl") @XmlChildrenName("Std") val lessons: List<Lesson>,
    ) {
        @SerialName("Ue")
        @Serializable
        data class SubjectInstanceWrapper(
            @SerialName("UeNr") val subjectInstance: SubjectInstance
        ) {
            @Serializable
            @SerialName("UeNr")
            data class SubjectInstance(
                @XmlValue val subjectInstanceNumber: Int,
                @SerialName("UeLe") val teacherName: String? = null,
                @SerialName("UeFa") val subjectName: String,
                @SerialName("UeGr") val courseName: String? = null
            )
        }

        @SerialName("Std")
        @Serializable
        data class Lesson(
            @SerialName("Le") val teacher: Teacher,
            @SerialName("Ra") val room: Room
        ) {
            @Serializable
            @SerialName("Le")
            data class Teacher(
                @XmlValue val name: String
            )

            @Serializable
            @SerialName("Ra")
            data class Room(
                @XmlValue val name: String
            )
        }
    }
}