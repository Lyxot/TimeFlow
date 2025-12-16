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


@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class WeekRange(
    @ProtoNumber(1) val range: List<Range>
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
enum class WeekDescriptionEnum {
    @ProtoNumber(1)
    ALL,

    @ProtoNumber(2)
    ODD,

    @ProtoNumber(3)
    EVEN
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class WeekList(
    @ProtoNumber(1) val weeks: List<Byte>
) {
    companion object {
        fun fromIntList(weekList: List<Int>): WeekList = WeekList(weekList.map { it.toByte() })
    }

    constructor(
        weekDescription: WeekDescriptionEnum,
        totalWeeks: Int,
        validWeeks: List<Int> = (1..totalWeeks).toList()
    ) : this(
        weeks = when (weekDescription) {
            WeekDescriptionEnum.ALL -> validWeeks.filter { it in 1..totalWeeks }.map { it.toByte() }
            WeekDescriptionEnum.ODD -> validWeeks.filter { it % 2 != 0 && it in 1..totalWeeks }
                .map { it.toByte() }

            WeekDescriptionEnum.EVEN -> validWeeks.filter { it % 2 == 0 && it in 1..totalWeeks }
                .map { it.toByte() }
        }
    )

    constructor(weekRange: WeekRange) : this(
        weeks = weekRange.range.flatMap { it.start..it.end }.distinct().map { it.toByte() }
    )

    fun getString(): String {
        if (weeks.isEmpty()) return ""

        val sortedWeeks = weeks.sorted()
        val result = mutableListOf<String>()
        var start = sortedWeeks[0]
        var end = sortedWeeks[0]

        for (i in 1 until sortedWeeks.size) {
            if (sortedWeeks[i] == (end + 1).toByte()) {
                end = sortedWeeks[i]
            } else {
                result.add(if (start == end) "$start" else "$start-$end")
                start = sortedWeeks[i]
                end = sortedWeeks[i]
            }
        }

        result.add(if (start == end) "$start" else "$start-$end")
        return result.joinToString(", ")
    }
}