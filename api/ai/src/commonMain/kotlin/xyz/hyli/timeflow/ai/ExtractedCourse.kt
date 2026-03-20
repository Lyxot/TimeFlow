/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ai

import kotlinx.serialization.Serializable
import xyz.hyli.timeflow.data.*
import xyz.hyli.timeflow.utils.CourseColors

/**
 * LLM 提取出的课程表级别信息（可选）。
 * 作为 JSONL 输出的第一行，以 "_schedule" 字段标记。
 */
@Serializable
data class ExtractedScheduleInfo(
    @Suppress("PropertyName")
    val _schedule: Boolean = true,
    /** 课程表名称/标题 */
    val name: String? = null,
    /** 学期开始日期，格式 "YYYY-MM-DD" */
    val termStartDate: String? = null,
    /** 学期结束日期，格式 "YYYY-MM-DD" */
    val termEndDate: String? = null,
    /** 总教学周数 */
    val totalWeeks: Int? = null,
    /** 是否显示周末（图片中是否包含周六/周日列） */
    val displayWeekends: Boolean? = null,
    /** 上午课程节数 */
    val morningLessons: Int? = null,
    /** 下午课程节数 */
    val afternoonLessons: Int? = null,
    /** 晚上课程节数 */
    val eveningLessons: Int? = null,
)

/**
 * LLM 提取出的单个课程信息。
 * 与 Course 数据类不同，此类使用简单的基本类型以便 LLM 直接生成。
 */
@Serializable
data class ExtractedCourse(
    val name: String,
    val teacher: String? = null,
    val classroom: String? = null,
    /** 上课节次范围，例如 [1, 3] 表示第1到第3节 */
    val time: List<Int>,
    /** 星期几，0=星期一，6=星期日 */
    val weekday: Int,
    /** 教学周列表，例如 [1, 2, 3] */
    val week: List<Int>? = null,
    /** 备注信息，如课程类型、学分、课程代码等 */
    val note: String? = null
) {
    /**
     * 转换为 Course 数据类。
     * @param color 课程颜色，默认为随机颜色。
     */
    fun toCourse(color: Int = CourseColors.random()): Course {
        val weekdayEnum = Weekday.entries.getOrElse(weekday) { Weekday.MONDAY }
        return Course(
            name = name,
            teacher = teacher ?: "",
            classroom = classroom ?: "",
            time = Range(
                start = time.getOrElse(0) { 1 },
                end = time.getOrElse(1) { time.getOrElse(0) { 1 } }
            ),
            weekday = weekdayEnum,
            week = WeekList(week ?: emptyList()),
            color = color,
            note = note ?: ""
        )
    }
}

/**
 * 完整的提取结果，包含课程表元数据和课程列表。
 */
data class ExtractionResult(
    val scheduleInfo: ExtractedScheduleInfo?,
    val courses: List<ExtractedCourse>
) {
    /**
     * 转换为 Schedule 数据类。
     * 同名课程使用相同颜色。
     */
    fun toSchedule(): Schedule {
        // Assign same color to courses with same name
        val colorByName = mutableMapOf<String, Int>()
        val courseMap = mutableMapOf<Short, Course>()
        var id: Short = 0

        for (extracted in courses) {
            val color = colorByName.getOrPut(extracted.name) { CourseColors.random() }
            courseMap[id++] = extracted.toCourse(color)
        }

        val info = scheduleInfo

        // Parse term dates if provided
        val termStartDate = info?.termStartDate?.let { parseDate(it) } ?: Schedule.defaultTermStartDate()
        val totalWeeks = info?.totalWeeks ?: 16
        val termEndDate = info?.termEndDate?.let { parseDate(it) } ?: termStartDate.addWeeks(totalWeeks)

        // Build lesson time period info from morning/afternoon/evening counts
        val lessonTimePeriodInfo =
            if (info?.morningLessons != null || info?.afternoonLessons != null || info?.eveningLessons != null) {
                LessonTimePeriodInfo.fromPeriodCounts(
                    morningCount = info.morningLessons ?: 5,
                    afternoonCount = info.afternoonLessons ?: 5,
                    eveningCount = info.eveningLessons ?: 4
                )
            } else {
                LessonTimePeriodInfo.defaultLessonTimePeriodInfo
            }

        return Schedule(
            name = info?.name ?: "",
            courses = courseMap,
            termStartDate = termStartDate,
            termEndDate = termEndDate,
            displayWeekends = info?.displayWeekends ?: false,
            lessonTimePeriodInfo = lessonTimePeriodInfo
        )
    }
}

/**
 * Parse "YYYY-MM-DD" to Date. Returns null on failure.
 */
private fun parseDate(s: String): Date? {
    val parts = s.split("-")
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    return Date(year, month, day)
}
