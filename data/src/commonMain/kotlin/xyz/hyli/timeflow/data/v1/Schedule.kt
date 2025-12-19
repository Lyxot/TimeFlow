/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.data.v1

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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalSerializationApi::class, ExperimentalTime::class)
@Serializable
data class Schedule(
    @ProtoNumber(1) val name: String = "",
    @ProtoNumber(2) val deleted: Boolean = false,
    @ProtoNumber(3) val courses: List<Course> = emptyList(),
    @ProtoNumber(4) val termStartDate: Date = defaultTermStartDate(),
    @ProtoNumber(5) val termEndDate: Date = defaultTermEndDate(),
    @ProtoNumber(6) val lessonTimePeriodInfo: LessonTimePeriodInfo = LessonTimePeriodInfo.fromPeriodCounts(),
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

    fun totalWeeks(): Int {
        return this.termStartDate.weeksTill(this.termEndDate)
    }

    fun dateList(week: Int): List<LocalDate> {
        var startDate = this.termStartDate.toLocalDate()
        while (startDate.dayOfWeek != DayOfWeek.MONDAY) {
            startDate = startDate.minus(1, DateTimeUnit.DAY)
        }
        return List(7) { i ->
            startDate.plus((week - 1) * 7 + i, DateTimeUnit.DAY)
        }
    }
}
