/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.data.v2

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

// Docs: https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/formats.md

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Course(
    /** 课程名称 */
    @ProtoNumber(1) val name: String,
    /** 授课教师 */
    @ProtoNumber(2) val teacher: String = "",
    /** 上课教室 */
    @ProtoNumber(3) val classroom: String = "",
    /** 上课时间，为一个节次范围 */
    @ProtoNumber(4) val time: Range,
    /** 课程所在的星期 */
    @ProtoNumber(5) val weekday: Weekday,
    /** 课程所在的教学周 */
    @ProtoNumber(6) val week: WeekList,
    /** 课程在课程表上显示的颜色 */
    @ProtoNumber(7) val color: Int,
    /** 课程备注 */
    @ProtoNumber(8) val note: String = "",
    @ProtoNumber(9) val reserved9: String? = null,
    @ProtoNumber(10) val reserved10: String? = null,
    @ProtoNumber(11) val reserved11: String? = null,
    @ProtoNumber(12) val reserved12: String? = null,
) {
    val summary = CourseSummary(
        name = name,
        teacher = teacher,
        classroom = classroom
    )

    /**
     * 检查此课程是否安排在指定的教学周。
     *
     * @param week 要检查的教学周。
     * @return 如果课程安排在指定周，则返回 `true`，否则返回 `false`。
     */
    fun isInWeek(week: Int) =
        this.week.weeks.contains(week)

    /**
     * 检查此课程的时间和星期是否与另一个给定的时间范围和星期冲突。
     * “冲突”定义为在同一星期几（Weekday）有时间上的重叠。
     *
     * @param otherTime 要比较的其他时间范围。
     * @param otherWeekday 要比较的其他星期。
     * @return 如果存在时间冲突，则返回 `true`，否则返回 `false`。
     */
    fun conflictsWith(otherTime: Range, otherWeekday: Weekday): Boolean {
        return this.weekday == otherWeekday &&
                this.time.end >= otherTime.start &&
                this.time.start <= otherTime.end
    }
}