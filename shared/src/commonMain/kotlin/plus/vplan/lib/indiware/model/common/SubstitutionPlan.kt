package plus.vplan.lib.indiware.model.common

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class SubstitutionPlan(
    val classes: List<SubstitutionPlanClass>,
    val info: String?,
    val date: LocalDate,
    val createdAt: LocalDateTime
) {
    data class SubstitutionPlanClass(
        val name: String,
        val lessons: List<SubstitutionPlanLesson>
    )
}