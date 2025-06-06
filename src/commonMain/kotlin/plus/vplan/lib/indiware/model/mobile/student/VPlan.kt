package plus.vplan.lib.indiware.model.mobile.student

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@SerialName("VpMobil")
internal data class VPlan(
    @SerialName("Klassen")
    @XmlChildrenName("Kl")
    val classes: List<Class>,

    @SerialName("Kopf")
    val head: Head,

    @SerialName("ZusatzInfo")
    @XmlChildrenName("ZiZeile")
    val info: List<Info>
) {
    @Serializable
    @SerialName("Kopf")
    data class Head(
        @SerialName("zeitstempel") val timestamp: Zeitstempel,
    ) {
        @Serializable
        @SerialName("zeitstempel")
        data class Zeitstempel(
            @XmlValue val value: String
        )
    }

    @Serializable
    @SerialName("Kl")
    data class Class(
        @SerialName("Kurz") val name: ClassName,
        @SerialName("Pl")
        @XmlChildrenName("Std")
        val lessons: List<ClassLesson>
    ) {
        @Serializable
        @SerialName("Kurz")
        data class ClassName(
            @XmlValue val name: String
        )

        @Serializable
        @SerialName("Std")
        data class ClassLesson(
            @SerialName("St") val lessonNumber: LessonNumber,
            @SerialName("Fa") val subject: Subject,
            @SerialName("Le") val teacher: Teacher,
            @SerialName("Ra") val room: Room,
            @SerialName("If") val info: Info,
            @SerialName("Nr") val subjectInstanceNumber: SubjectInstanceNumber? = null,
            @SerialName("Beginn") val start: Start,
            @SerialName("Ende") val end: End
        ) {

            @Serializable
            @SerialName("Beginn")
            data class Start(
                @XmlValue val value: String
            )

            @Serializable
            @SerialName("Ende")
            data class End(
                @XmlValue val value: String
            )

            @Serializable
            @SerialName("Nr")
            data class SubjectInstanceNumber(
                @XmlValue val value: String
            )

            @Serializable
            @SerialName("St")
            data class LessonNumber(
                @XmlValue val value: Int
            )

            @Serializable
            @SerialName("Fa")
            data class Subject(
                @XmlValue val value: String,
                @SerialName("FaAe") val changed: String? = null
            )

            @Serializable
            @SerialName("Le")
            data class Teacher(
                @XmlValue val value: String,
                @SerialName("LeAe") val changed: String? = null
            )

            @Serializable
            @SerialName("Ra")
            data class Room(
                @XmlValue val value: String,
                @SerialName("RaAe") val changed: String? = null
            )

            @Serializable
            @SerialName("If")
            data class Info(
                @XmlValue val value: String
            )
        }
    }

    @Serializable
    @SerialName("ZiZeile")
    data class Info(
        @XmlValue val value: String
    )
}