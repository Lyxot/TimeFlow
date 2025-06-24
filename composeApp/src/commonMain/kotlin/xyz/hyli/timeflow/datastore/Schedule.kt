package xyz.hyli.timeflow.datastore

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Date(
    @ProtoNumber(1) val year: Int,
    @ProtoNumber(2) val month: Int,
    @ProtoNumber(3) val day: Int
) {
    companion object {
        fun fromLocalDate(localDate: LocalDate): Date {
            return Date(localDate.year, localDate.monthNumber, localDate.dayOfMonth)
        }
    }
    override fun toString(): String =
         "$year/${if (month < 10) "0$month" else month}/${if (day < 10) "0$day" else day}"

    fun toString(withYear: Boolean = true): String =
        if (withYear) {
            this.toString()
        } else {
            "${this.month}/${if (this.day < 10) "0${this.day}" else {this.day}}"
        }

    fun toLocalDate(): LocalDate = LocalDate(year, month, day)

    fun addWeeks(weeks: Int): Date =
        fromLocalDate(this.toLocalDate().plus(weeks, DateTimeUnit.WEEK))
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Time(
    @ProtoNumber(1) val hour: Int,
    @ProtoNumber(2) val minute: Int
) {
    override fun toString(): String {
        return this.hour.toString().padStart(2, '0') +
                ":" + this.minute.toString().padStart(2, '0')
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
data class LessonTimePeriodInfo(
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
        ): LessonTimePeriodInfo {
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

            return LessonTimePeriodInfo(
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

    fun getTotalLessons(): Int {
        return morning.size + afternoon.size + evening.size
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Schedule(
    @ProtoNumber(1) val name: String = "",
    @ProtoNumber(2) val deleted: Boolean = false,
    @ProtoNumber(3) val courses: List<Course> = emptyList(),
    @ProtoNumber(4) val termStartDate: Date = defaultTermStartDate(),
    @ProtoNumber(5) val termEndDate: Date = defaultTermEndDate(),
    @ProtoNumber(6) val lessonTimePeriodInfo: LessonTimePeriodInfo = LessonTimePeriodInfo.fromPeriodCounts(),
    @ProtoNumber(7) val displayWeekends: Boolean = false,
) {
    companion object {
        fun defaultTermStartDate(): Date {
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val year = currentDate.year
            val month = if (currentDate.monthNumber in 3..8) 3 else 9
            val day = 1
            return Date(year, month, day)
        }

        fun defaultTermEndDate(): Date = defaultTermStartDate().addWeeks(16)
    }

    fun totalWeeks(): Int {
        return weeksTill(this.termEndDate)
    }

    fun weeksTill(date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): Int {
        val startDate = this.termStartDate.toLocalDate()
        if (date < startDate) return 0

        // 计算每个日期在周中的位置偏移（周一=0，周日=6）
        val startOffset = startDate.dayOfWeek.ordinal - 1
        val endOffset = date.dayOfWeek.ordinal - 1

        // 计算调整后的天数差
        val daysBetween = startDate.daysUntil(date)
        val adjustedDays = daysBetween + startOffset - endOffset

        // 计算周数
        return (adjustedDays / 7) + 1
    }

    fun weeksTill(date: Date): Int {
        return weeksTill(date.toLocalDate())
    }

    fun dateList(week: Int): List<LocalDate> {
        var startDate = this.termStartDate.toLocalDate()
        while (startDate.dayOfWeek != kotlinx.datetime.DayOfWeek.MONDAY) {
            startDate = startDate.minus(1, DateTimeUnit.DAY)
        }
        return List(7) { i ->
            startDate.plus((week - 1) * 7 + i, DateTimeUnit.DAY)
        }
    }
}
