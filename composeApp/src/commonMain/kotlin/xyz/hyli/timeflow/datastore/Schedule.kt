package xyz.hyli.timeflow.datastore

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class DayList(
    @ProtoNumber(1) val monday: List<Course> = emptyList(),
    @ProtoNumber(2) val tuesday: List<Course> = emptyList(),
    @ProtoNumber(3) val wednesday: List<Course> = emptyList(),
    @ProtoNumber(4) val thursday: List<Course> = emptyList(),
    @ProtoNumber(5) val friday: List<Course> = emptyList(),
    @ProtoNumber(6) val saturday: List<Course> = emptyList(),
    @ProtoNumber(7) val sunday: List<Course> = emptyList()
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Date(
    @ProtoNumber(1) val year: Int,
    @ProtoNumber(2) val month: Int,
    @ProtoNumber(3) val day: Int
) {
    override fun toString(): String =
         "$year/${if (month < 10) "0$month" else month}/${if (day < 10) "0$day" else day}"

    fun toString(withYear: Boolean = true): String =
        if (withYear) {
            this.toString()
        } else {
            "${this.month}/${if (this.day < 10) "0${this.day}" else {this.day}}"
        }

    fun addWeeks(weeks: Int): Date {
        var newDate = this
        for (i in 1..weeks * 7) {
            val daysInMonth = when (newDate.month) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if ((newDate.year % 4 == 0 && newDate.year % 100 != 0) || (newDate.year % 400 == 0)) 29 else 28
                else -> throw IllegalArgumentException("Invalid month")
            }
            newDate = if (newDate.day < daysInMonth) {
                Date(newDate.year, newDate.month, newDate.day + 1)
            } else {
                if (newDate.month == 12) {
                    Date(newDate.year + 1, 1, 1)
                } else {
                    Date(newDate.year, newDate.month + 1, 1)
                }
            }
        }
        return newDate
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Time(
    @ProtoNumber(1) val hour: Int,
    @ProtoNumber(2) val minute: Int
) {
    override fun toString(): String {
        return "${this.hour}:${if (this.minute < 10) "0${this.minute}" else {this.minute}}"
    }

    operator fun compareTo(b: Time): Int {
        return when {
            this.hour != b.hour -> this.hour - b.hour
            else -> this.minute - b.minute
        }
    }

    fun addMinutes(minutes: Int): Time {
        val totalMinutes = this.hour * 60 + this.minute + minutes
        val newHour = (totalMinutes / 60) % 24
        val newMinute = totalMinutes % 60
        return Time(newHour, newMinute)
    }

    fun minutesSince(other: Time): Int {
        return (this.hour * 60 + this.minute) - (other.hour * 60 + other.minute)
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Lesson(
    @ProtoNumber(1) val start: Time,
    @ProtoNumber(2) val end: Time,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class LessonsPerDay(
    @ProtoNumber(1) val morning: List<Lesson>,
    @ProtoNumber(2) val afternoon: List<Lesson>,
    @ProtoNumber(3) val evening: List<Lesson>
) {
    companion object {
        fun fromPeriodCounts(
            morningCount: Int = 5,
            afternoonCount: Int = 5,
            eveningCount: Int = 4,
            lessonDuration: Int = 40,
            breakDuration: Int = 10,
            morningStart: Time = Time(8, 0),
            afternoonStart: Time = Time(13, 0),
            eveningStart: Time = Time(18, 0)
        ): LessonsPerDay {
            val start = listOf(morningCount, afternoonCount, eveningCount).indexOfFirst{ it > 0 }
            var time = when (start) {
                0 -> morningStart
                1 -> afternoonStart
                2 -> eveningStart
                else -> Time(0, 0) // Default case, should not happen
            }
            val morning = if (morningCount == 0) {
                emptyList()
            } else {
                generateLessons(
                    morningCount,
                    if (time > morningStart) time else morningStart,
                    lessonDuration,
                    breakDuration
                )
            }
            time = if (morningCount > 0) {
                morning.last().end.addMinutes(breakDuration)
            } else {
                afternoonStart
            }

            val afternoon = if (afternoonCount == 0) {
                emptyList()
            } else {
                generateLessons(
                    afternoonCount,
                    if (time > afternoonStart) time else afternoonStart,
                    lessonDuration,
                    breakDuration
                )
            }
            time = if (afternoonCount > 0) {
                afternoon.last().end.addMinutes(breakDuration)
            } else {
                eveningStart
            }

            val evening = if (eveningCount == 0) {
                emptyList()
            } else {
                generateLessons(
                    eveningCount,
                    if (time > eveningStart) time else eveningStart,
                    lessonDuration,
                    breakDuration
                )
            }

            return LessonsPerDay(
                morning = morning,
                afternoon = afternoon,
                evening = evening
            )
        }

        fun generateLessons(
            count: Int,
            startTime: Time,
            lessonDuration: Int,
            breakDuration: Int
        ): List<Lesson> {
            return List(count) { index ->
                val lessonStart = startTime.addMinutes(index * (lessonDuration + breakDuration))
                Lesson(
                    start = lessonStart,
                    end = lessonStart.addMinutes(lessonDuration)
                )
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Schedule(
    @ProtoNumber(1) val name: String = "",
    @ProtoNumber(2) val deleted: Boolean = false,
    @ProtoNumber(3) val schedule: DayList = DayList(),
    @ProtoNumber(4) val termStartDate: Date,
    @ProtoNumber(5) val termEndDate: Date,
    @ProtoNumber(6) val lessonsPerDay: LessonsPerDay = LessonsPerDay.fromPeriodCounts(),
    @ProtoNumber(7) val displayWeekends: Boolean = false,
)
