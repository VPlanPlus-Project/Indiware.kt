package plus.vplan.lib.indiware.model.mobile.student

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Suppress("unused")
@Serializable
@SerialName("VpMobil")
data class MobileStudentData(
    @SerialName("Kopf") val header: Header,
    @SerialName("FreieTage") @XmlChildrenName("ft") val holidays: List<Holiday>,
    @SerialName("Klassen") @XmlChildrenName("Kl") val classes: List<Class>,

    @Transient val raw: String = ""
) {
    @Serializable
    @SerialName("Kopf")
    data class Header(
        @SerialName("planart") val planType: PlanType,
        @SerialName("zeitstempel") val timestamp: Timestamp,
        @SerialName("DatumPlan") val date: Date,
        @SerialName("woche") val week: Week,
    ) {
        @Serializable
        @SerialName("planart")
        data class PlanType(
            @XmlValue val type: String
        )

        @Serializable
        @SerialName("zeitstempel")
        data class Timestamp(
            @XmlValue val value: String
        ) {
            val createdAt = run {
                val format = LocalDateTime.Format {
                    dayOfMonth(Padding.ZERO)
                    char('.')
                    monthNumber(Padding.ZERO)
                    char('.')
                    year(Padding.ZERO)
                    chars(", ")
                    hour(Padding.ZERO)
                    char(':')
                    minute(Padding.ZERO)
                }
                LocalDateTime.parse(value, format)
            }
        }

        @Serializable
        @SerialName("DatumPlan")
        data class Date(
            @XmlValue val value: String
        ) {
            val date = run {
                val format = LocalDate.Format {
                    dayOfWeek(DayOfWeekNames("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"))
                    chars(", ")
                    dayOfMonth(Padding.ZERO)
                    chars(". ")
                    monthName(MonthNames("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"))
                    char(' ')
                    year(Padding.ZERO)
                }
                LocalDate.parse(value, format)
            }
        }

        @Serializable
        @SerialName("woche")
        data class Week(
            @XmlValue val value: String
        )
    }

    @Serializable
    @SerialName("ft")
    data class Holiday(
        @XmlValue val value: String
    ) {
        companion object {
            private val holidayFormat = LocalDate.Format {
                yearTwoDigits(2000)
                monthNumber()
                dayOfMonth()
            }
        }

        val date = LocalDate.parse(value, holidayFormat)
    }

    @Serializable
    @SerialName("Kl")
    data class Class(
        @SerialName("Kurz") val name: ClassName,
        @SerialName("KlStunden") @XmlChildrenName("KlSt") val lessonTimes: List<ClassLessonTime>,
        @SerialName("Kurse") @XmlChildrenName("Ku") val courses: List<ClassCourseWrapper>,
        @SerialName("Unterricht") @XmlChildrenName("Ue") val subjectInstances: List<MobileStudentBaseData.Class.ClassSubjectInstanceWrapper>,
        @SerialName("Pl") @XmlChildrenName("Std") val lessons: List<ClassLessonStudent>
    ) {
        @Serializable
        @SerialName("Kurz")
        data class ClassName(
            @XmlValue val name: String
        )

        @Serializable
        @SerialName("KlSt")
        data class ClassLessonTime(
            @XmlValue val lessonNumberValue: String? = null,
            @XmlSerialName("ZeitVon") val startTimeValue: String,
            @XmlSerialName("ZeitBis") val endTimeValue: String,
        ) {
            val lessonNumber: Int? = lessonNumberValue?.toIntOrNull()

            val startTime = try {
                LocalDateTime.parse(startTimeValue, LocalDateTime.Format {
                    hour(Padding.ZERO)
                    char(':')
                    minute(Padding.ZERO)
                })
            } catch (_: IllegalArgumentException) {
                null
            }

            val endTime = try {
                LocalDateTime.parse(endTimeValue, LocalDateTime.Format {
                    hour(Padding.ZERO)
                    char(':')
                    minute(Padding.ZERO)
                })
            } catch (_: IllegalArgumentException) {
                null
            }
        }

        @Serializable
        @SerialName("Ku")
        data class ClassCourseWrapper(
            @SerialName("Ku") val course: ClassCourse
        )

        @Serializable
        @SerialName("KKz")
        data class ClassCourse(
            @XmlValue val name: String,
            @XmlSerialName("KLe") val teacher: String? = null,
        )

        @Serializable
        @SerialName("Std")
        data class ClassLessonStudent(
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