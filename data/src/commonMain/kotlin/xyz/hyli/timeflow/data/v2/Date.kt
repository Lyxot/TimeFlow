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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Date(
    @ProtoNumber(1) val year: Int,
    @ProtoNumber(2) val month: Int,
    @ProtoNumber(3) val day: Int
) {
    companion object {
        fun fromLocalDate(localDate: LocalDate): Date {
            return Date(localDate.year, localDate.month.number, localDate.day)
        }
    }

    override fun toString(): String =
        "$year/${if (month < 10) "0$month" else month}/${if (day < 10) "0$day" else day}"

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

    fun toLocalDate(): LocalDate = LocalDate(year.toInt(), month.toInt(), day.toInt())

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

    fun weeksTill(date: Date): Int {
        return weeksTill(date.toLocalDate())
    }

    fun addWeeks(weeks: Int): Date {
        var current = this.toLocalDate().plus(weeks, DateTimeUnit.WEEK)
        while (this.weeksTill(current) > weeks) {
            current = current.minus(1, DateTimeUnit.DAY)
        }
        return fromLocalDate(current)
    }
}