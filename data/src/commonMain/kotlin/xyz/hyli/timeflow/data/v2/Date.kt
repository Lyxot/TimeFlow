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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Date(
    /** 年份 */
    @ProtoNumber(1) val year: Int,
    /** 月份 */
    @ProtoNumber(2) val month: Int,
    /** 日 */
    @ProtoNumber(3) val day: Int
) {
    companion object {
        /**
         * 从 `kotlinx.datetime.LocalDate` 对象创建一个 `Date` 实例。
         *
         * @param localDate `LocalDate` 实例。
         * @return 新的 `Date` 实例。
         */
        fun fromLocalDate(localDate: LocalDate): Date {
            return Date(localDate.year, localDate.month.number, localDate.day)
        }
    }

    constructor(localDate: LocalDate) : this(localDate.year, localDate.month.number, localDate.day)

    /**
     * 返回日期的字符串表示形式，格式为 "YYYY/MM/DD"。
     * 月和日如果小于10，会用 '0' 补齐。
     *
     * @return 格式化后的日期字符串。
     */
    override fun toString(): String =
        "$year/${if (month < 10) "0$month" else month}/${if (day < 10) "0$day" else day}"

    /**
     * 返回日期的字符串表示形式，可以选择是否包含年份。
     *
     * @param withYear 如果为 `true`，则格式为 "YYYY/MM/DD"；如果为 `false`，则格式为 "M/DD"。
     * @return 格式化后的日期字符串。
     */
    fun toString(withYear: Boolean = true): String =
        if (withYear) {
            this.toString()
        } else {
            "${this.month}/${
                if (this.day < 10) "0${this.day}" else {
                    this.day
                }
            }"
        }

    /**
     * 将当前的 `Date` 对象转换为 `kotlinx.datetime.LocalDate` 对象。
     *
     * @return 对应的 `LocalDate` 实例。
     */
    fun toLocalDate(): LocalDate = LocalDate(year, month, day)

    /**
     * 计算从当前日期到目标日期所经过的教学周数。
     * 周数从1开始计数。如果目标日期早于当前日期，则返回0。
     *
     * @param date 目标日期，默认为系统当前日期。
     * @return 经过的教学周数。
     */
    @OptIn(ExperimentalTime::class)
    fun weeksTill(date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): Int {
        val startDate = this.toLocalDate()
        if (date < startDate) return 0

        // 计算每个日期在周中的位置偏移（周一=0，周日=6）
        val startOffset = startDate.dayOfWeek.isoDayNumber - 1
        val endOffset = date.dayOfWeek.isoDayNumber - 1

        // 计算调整后的天数差
        val daysBetween = startDate.daysUntil(date)
        val adjustedDays = daysBetween + startOffset - endOffset

        // 计算周数
        return (adjustedDays / 7) + 1
    }

    /**
     * 计算从当前日期到另一个 `Date` 对象所经过的教学周数。
     *
     * @param date 目标 `Date` 对象。
     * @return 经过的教学周数。
     */
    fun weeksTill(date: Date): Int {
        return weeksTill(date.toLocalDate())
    }

    /**
     * 在当前日期的基础上增加指定的周数。
     *
     * @param weeks 要增加的周数。
     * @return 计算后的新 `Date` 对象。
     */
    fun addWeeks(weeks: Int): Date {
        var current = this.toLocalDate().plus(weeks, DateTimeUnit.WEEK)
        while (this.weeksTill(current) > weeks) {
            current = current.minus(1, DateTimeUnit.DAY)
        }
        return Date(current)
    }
}