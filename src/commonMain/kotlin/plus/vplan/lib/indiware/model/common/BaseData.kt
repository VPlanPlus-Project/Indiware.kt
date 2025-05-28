package plus.vplan.lib.indiware.model.common

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param schoolName The name of the school. Some APIs don't provide this field so it can be `null`. Use the dedicated getSchoolName() function to get the name of the school.
 */
@Serializable
data class BaseData(
    @SerialName("school_name") val schoolName: String?,
    @SerialName("holidays") val holidays: List<LocalDate>,
    @SerialName("classes") val classes: List<BaseDataClass>,
) {
    @Serializable
    data class BaseDataClass(
        @SerialName("name") val name: String,
        @SerialName("lesson_times") val lessonTimes: List<BaseDataLessonTime>,
        @SerialName("subject_instances") val subjectInstances: List<BaseDataSubjectInstance>,
        @SerialName("courses") val courses: List<BaseDataCourse>
    ) {
        @Serializable
        data class BaseDataLessonTime(
            @SerialName("number") val number: Int,
            @SerialName("start") val start: LocalTime,
            @SerialName("end") val end: LocalTime
        )

        @Serializable
        data class BaseDataSubjectInstance(
            @SerialName("subject") val subject: String,
            @SerialName("teachers") val teachers: List<String>,
        )

        @Serializable
        data class BaseDataCourse(
            @SerialName("name") val name: String,
            @SerialName("teachers") val teachers: List<String>
        )
    }
}
