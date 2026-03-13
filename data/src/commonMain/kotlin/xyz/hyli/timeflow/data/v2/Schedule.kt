/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.data.v2

import kotlinx.datetime.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import xyz.hyli.timeflow.data.Weekday
import xyz.hyli.timeflow.data.newShortId
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalSerializationApi::class, ExperimentalTime::class)
@Serializable
data class Schedule(
    /** 课程表名称 */
    @ProtoNumber(1) val name: String = "",
    /** 课程表是否已被删除(进入回收站) */
    @ProtoNumber(2) val deleted: Boolean = false,
    /** 课程映射，键为课程 ID，值为 Course 对象 */
    @ProtoNumber(3) val courses: Map<Short, Course> = emptyMap(),
    /** 学期开始日期 */
    @ProtoNumber(4) val termStartDate: Date = defaultTermStartDate(),
    /** 学期结束日期 */
    @ProtoNumber(5) val termEndDate: Date = defaultTermEndDate(),
    /** 课程时间段信息 */
    @ProtoNumber(6) val lessonTimePeriodInfo: LessonTimePeriodInfo = LessonTimePeriodInfo.defaultLessonTimePeriodInfo,
    /** 是否显示周末 */
    @ProtoNumber(7) val displayWeekends: Boolean = false,
    /** 创建时间 */
    @ProtoNumber(8) val createdAt: Instant = Clock.System.now(),
    /** 最后更新时间 */
    @ProtoNumber(9) val updatedAt: Instant = Clock.System.now(),
    @ProtoNumber(10) val reserved10: String? = null,
    @ProtoNumber(11) val reserved11: String? = null,
    @ProtoNumber(12) val reserved12: String? = null,
) {
    companion object {
        /**
         * 获取默认的学期开始日期。
         * 规则：如果当前月份在3月到8月之间，则为当年3月1日；否则为当年9月1日。
         * @return 一个表示默认开始日期的 [Date] 对象。
         */
        fun defaultTermStartDate(): Date {
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val year = currentDate.year
            val month = if (currentDate.month.number in 3..8) 3 else 9
            val day = 1
            return Date(year, month, day)
        }

        /**
         * 获取默认的学期结束日期，即默认开始日期后的16周。
         * @return 一个表示默认结束日期的 [Date] 对象。
         */
        fun defaultTermEndDate(): Date = defaultTermStartDate().addWeeks(16)
    }

    /**
     * 课程表的摘要信息。
     */
    val summary = ScheduleSummary(
        name = this.name,
        deleted = this.deleted,
        termStartDate = this.termStartDate,
        termEndDate = this.termEndDate,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )

    /**
     * 根据学期起止日期计算总教学周数。
     */
    val totalWeeks: Int = this.termStartDate.weeksTill(this.termEndDate)

    /**
     * 获取指定教学周对应的日期列表（从周一到周日）。
     * @param week 教学周（1-based）。
     * @return 包含7个 [LocalDate] 对象的列表。
     */
    fun dateList(week: Int): List<LocalDate> {
        var startDate = this.termStartDate.toLocalDate()
        while (startDate.dayOfWeek != DayOfWeek.MONDAY) {
            startDate = startDate.minus(1, DateTimeUnit.DAY)
        }
        return List(7) { i ->
            startDate.plus((week - 1) * 7 + i, DateTimeUnit.DAY)
        }
    }

    /**
     * 检查给定的周次是否在学期范围内。
     * @param week 要检查的周次。
     * @return 如果在范围内则为 `true`，否则为 `false`。
     */
    fun isInTerm(week: Int) = week in 1..totalWeeks

    /**
     * 根据给定的日期计算其属于哪个教学周。
     * @param date 要计算的日期。
     * @return 对应的教学周（1-based）。
     */
    fun getWeekOfDate(date: LocalDate): Int =
        this.termStartDate.weeksTill(date)

    /**
     * 获取指定日期的所有课程，并按开始时间排序。
     * @param date 指定的日期。
     * @return 符合条件的 [Course] 列表。
     */
    fun getCoursesOfDate(date: LocalDate): List<Course> =
        this.courses.values.filter { course ->
            course.week.weeks.contains(
                this.termStartDate.weeksTill(date)
            ) &&
                    course.weekday.ordinal == date.dayOfWeek.ordinal
        }
            .sortedBy { it.time.start }

    /**
     * 获取指定星期的所有课程。
     * @param weekday 指定的星期。
     * @return 一个 Map，键为课程ID，值为 [Course] 对象。
     */
    fun getCoursesOfWeekday(weekday: Weekday): Map<Short, Course> =
        this.courses.filterValues { course ->
            course.weekday == weekday
        }

    /**
     * 获取指定星期的所有时间段，并区分为当前周和非当前周。
     * @param weekday 指定的星期。
     * @param currentWeek 当前所在的教学周。
     * @return 一个 Triple，第一个元素为当前周的时间段集合，第二个元素为非当前周的、已排序的时间段列表，第三个元素为所有时间段的集合。
     */
    fun getTimeSlotsFor(
        weekday: Weekday,
        currentWeek: Int
    ): Triple<Set<Range>, List<Range>, Set<Range>> {
        val coursesForDay = getCoursesOfWeekday(weekday)
        val currentWeekTimes = mutableSetOf<Range>()
        val otherWeekTimes = mutableSetOf<Range>()

        coursesForDay.values.forEach { course ->
            if (course.isInWeek(currentWeek) && isInTerm(currentWeek)) {
                currentWeekTimes.add(course.time)
            } else {
                otherWeekTimes.add(course.time)
            }
        }
        return Triple(
            currentWeekTimes,
            otherWeekTimes.sortedBy { it.end - it.start },
            currentWeekTimes + otherWeekTimes
        )
    }

    /**
     * 获取在特定时间、星期和周次的课程。
     * @param time 指定的时间范围。
     * @param weekday 指定的星期。
     * @param week 指定的周次。
     * @return 符合条件的课程 Map。
     */
    fun getCoursesAt(time: Range, weekday: Weekday, week: Int): Map<Short, Course> {
        return getCoursesOfWeekday(weekday).filterValues { course ->
            course.time == time && course.isInWeek(week)
        }
    }

    /**
     * 获取在特定时间、星期有重叠的所有课程。
     * @param time 指定的时间范围。
     * @param weekday 指定的星期。
     * @return 符合条件的课程 Map。
     */
    fun getCoursesOverlapping(time: Range, weekday: Weekday): Map<Short, Course> {
        return getCoursesOfWeekday(weekday).filterValues { course ->
            course.time.end >= time.start && course.time.start <= time.end
        }
    }

    /**
     * 获取在特定时间、星期，但**不包括**指定周次的所有课程。
     * 如果当前周是假期，则返回所有匹配的课程。
     * @param time 指定的时间范围。
     * @param weekday 指定的星期。
     * @param currentWeek 当前周次，用于排除。
     * @return 符合条件的课程 Map。
     */
    fun getOtherWeekCoursesAt(time: Range, weekday: Weekday, currentWeek: Int): Map<Short, Course> {
        return getCoursesOfWeekday(weekday).filterValues { course ->
            course.time == time && (!course.isInWeek(currentWeek) || !isInTerm(currentWeek))
        }
    }

    /**
     * 检查给定的课程是否与课程表中任何现有课程存在冲突。
     * @param course 要检查的课程。
     * @param courseID 如果是编辑现有课程，请提供其ID以在检查中排除自身。对于新课程，此项为 null。
     * @return 如果存在冲突则返回 `true`，否则返回 `false`。
     */
    fun hasConflict(course: Course, courseID: Short? = null): Boolean {
        return courses.any { (id, existingCourse) ->
            id != courseID &&
                    existingCourse.conflictsWith(course.time, course.weekday) &&
                    existingCourse.week.intersects(course.week)
        }
    }

    /**
     * 获取在给定时间段和星期的前提下，所有可用的、不会产生冲突的周次。
     * @param time 指定的时间范围。
     * @param weekday 指定的星期。
     * @param courseID 如果是编辑现有课程，请提供其ID以在检查中排除自身。
     * @return 可用的周次列表。
     */
    fun getValidWeeksFor(time: Range, weekday: Weekday, courseID: Short? = null): List<Int> {
        val conflictingWeeks = courses.filter { (id, existingCourse) ->
            id != courseID && existingCourse.conflictsWith(time, weekday)
        }.flatMapTo(mutableSetOf()) { it.value.week.weeks }
        return (1..totalWeeks).toList() - conflictingWeeks
    }

    /**
     * 生成一个新的、不重复的课程ID。
     * @return 新的课程ID ([Short])。
     */
    fun newCourseId(): Short =
        newShortId(this.courses.keys)
}
