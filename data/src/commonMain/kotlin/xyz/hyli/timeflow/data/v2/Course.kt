/*
 * Copyright (c) 2025 Lyxot and contributors.
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
    @ProtoNumber(1) val name: String,
    @ProtoNumber(2) val teacher: String = "",
    @ProtoNumber(3) val classroom: String = "",
    @ProtoNumber(4) val time: Range,
    @ProtoNumber(5) val weekday: Weekday,
    @ProtoNumber(6) val week: WeekList,
    @ProtoNumber(7) val color: Int,
    @ProtoNumber(8) val note: String = "",
    @ProtoNumber(9) val reserved9: String? = null,
    @ProtoNumber(10) val reserved10: String? = null,
    @ProtoNumber(11) val reserved11: String? = null,
    @ProtoNumber(12) val reserved12: String? = null,
) {
    fun isInWeek(week: Int) =
        this.week.weeks.contains(week)
}