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
import xyz.hyli.timeflow.data.Course
import xyz.hyli.timeflow.data.Range
import xyz.hyli.timeflow.data.WeekList
import xyz.hyli.timeflow.data.Weekday

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
     * @param color 课程颜色，默认为0。
     */
    fun toCourse(color: Int = 0): Course {
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
