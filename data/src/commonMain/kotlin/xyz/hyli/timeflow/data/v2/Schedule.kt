/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.data.v2

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import xyz.hyli.timeflow.data.Weekday
import xyz.hyli.timeflow.data.newShortId
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalSerializationApi::class, ExperimentalTime::class)
@Serializable
data class Schedule(
    /**
     * 课程表名称
     */
    @ProtoNumber(1) val name: String = "",
    /**
     * 课程表是否已被删除(进入回收站)
     */
    @ProtoNumber(2) val deleted: Boolean = false,
    /**
     * 课程映射，键为课程 ID，值为 Course 对象
     */
    @ProtoNumber(3) val courses: Map<Short, Course> = emptyMap(),
    /**
     * 学期开始日期
     */
    @ProtoNumber(4) val termStartDate: Date = defaultTermStartDate(),
    /**
     * 学期结束日期
     */
    @ProtoNumber(5) val termEndDate: Date = defaultTermEndDate(),
    /**
     * 课程时间段信息
     */
    @ProtoNumber(6) val lessonTimePeriodInfo: LessonTimePeriodInfo = LessonTimePeriodInfo.defaultLessonTimePeriodInfo,
    /**
     * 是否显示周末
     */
    @ProtoNumber(7) val displayWeekends: Boolean = false,
    @ProtoNumber(8) val reserved8: String? = null,
    @ProtoNumber(9) val reserved9: String? = null,
    @ProtoNumber(10) val reserved10: String? = null,
    @ProtoNumber(11) val reserved11: String? = null,
    @ProtoNumber(12) val reserved12: String? = null,
) {
    companion object {
        fun defaultTermStartDate(): Date {
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val year = currentDate.year
            val month = if (currentDate.month.number in 3..8) 3 else 9
            val day = 1
            return Date(year, month, day)
        }

        fun defaultTermEndDate(): Date = defaultTermStartDate().addWeeks(16)
    }

    val totalWeeks: Int = this.termStartDate.weeksTill(this.termEndDate)

    fun dateList(week: Int): List<LocalDate> {
        var startDate = this.termStartDate.toLocalDate()
        while (startDate.dayOfWeek != DayOfWeek.MONDAY) {
            startDate = startDate.minus(1, DateTimeUnit.DAY)
        }
        return List(7) { i ->
            startDate.plus((week - 1) * 7 + i, DateTimeUnit.DAY)
        }
    }

    fun isInTerm(week: Int) = week in 1..totalWeeks

    fun getWeekOfDate(date: LocalDate): Int =
        this.termStartDate.weeksTill(date)

    fun getCoursesOfDate(date: LocalDate): List<Course> =
        this.courses.values.filter { course ->
            course.week.weeks.contains(
                this.termStartDate.weeksTill(date)
            ) &&
                    course.weekday.ordinal == date.dayOfWeek.ordinal
        }
            .sortedBy { it.time.start }

    fun getCoursesOfWeekday(weekday: Weekday): Map<Short, Course> =
        this.courses.filterValues { course ->
            course.weekday == weekday
        }

    fun newCourseId(): Short =
        newShortId(this.courses.keys)
}
