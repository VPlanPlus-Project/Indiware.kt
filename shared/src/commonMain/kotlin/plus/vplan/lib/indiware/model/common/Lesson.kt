package plus.vplan.lib.indiware.model.common

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * A lesson used in a substitution plan. This is recommended to be used whenever possible since it contains all the information about a lesson.
 * @param lessonNumber The number of the lesson in the day. This *can* start at zero, but it doesn't have to be. Don't fall for this trap when using it as an index for an [Iterable]. Some schools have a zero-based numbering, while others have a one-based numbering, depending on whether a school can have a lesson before the regular first lesson.
 * @param subject The subject of the lesson. This can be `null` if the lesson got cancelled.
 * @param isSubjectChanged Whether the subject of the lesson has changed. This is `true` if the subject is different from the original subject, or if the subject is `null`. This data directly comes from the Indiware API and is not validated against the original subject.
 * @param teachers The list of teachers for the lesson. This can be empty if the lesson got cancelled or if no teacher is assigned to the lesson.
 * @param areTeachersChanged Whether the teachers of the lesson have changed. This is `true` if the teachers are different from the original teachers, or if the list of teachers is empty. This data directly comes from the Indiware API and is not validated against the original teachers.
 * @param rooms The list of rooms for the lesson. This can be empty if the lesson got cancelled or if no room is assigned to the lesson. In some cases it's really hard to distinguish between room names since they are NOT seperated by commas in the raw data, so the list can contain wrong rooms especially if a room name regularly contains a space. Use this with caution. The library tries its best to split the names up by comparing them against a list of known names.
 * @param areRoomsChanged Whether the rooms of the lesson have changed. This is `true` if the rooms are different from the original rooms, or if the list of rooms is empty. This data directly comes from the Indiware API and is not validated against the original rooms.
 * @param info Additional information about the lesson. This can be `null` if there is no additional information.
 * @param start The start time of the lesson. This can be `null` if the lesson has no start time. That happens, when the school does not provide time information for specific lesson numbers.
 * @param end The end time of the lesson. This can be `null` if the lesson has no end time. That happens, when the school does not provide time information for specific lesson numbers.
 * @param date The date of the lesson. If you need a date, use this one. It is guaranteed to be not `null` and is the date of the day the lesson is scheduled for.
 */
data class SubstitutionPlanLesson(
    val lessonNumber: Int,
    val subject: String?,
    val isSubjectChanged: Boolean,
    val teachers: List<String>,
    val areTeachersChanged: Boolean,
    val rooms: List<String>,
    val areRoomsChanged: Boolean,
    val info: String?,
    val start: LocalDateTime?,
    val end: LocalDateTime?,
    val date: LocalDate
)
