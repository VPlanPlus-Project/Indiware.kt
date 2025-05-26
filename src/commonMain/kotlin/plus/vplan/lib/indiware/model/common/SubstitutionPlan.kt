package plus.vplan.lib.indiware.model.common

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubstitutionPlan(
    @SerialName("classes") val classes: List<SubstitutionPlanClass>,
    @SerialName("info") val info: String?,
    @SerialName("date") val date: LocalDate,
    @SerialName("created_at") val createdAt: LocalDateTime
) {
    @Serializable
    data class SubstitutionPlanClass(
        @SerialName("name") val name: String,
        @SerialName("lessons") val lessons: List<SubstitutionPlanLesson>
    )
}