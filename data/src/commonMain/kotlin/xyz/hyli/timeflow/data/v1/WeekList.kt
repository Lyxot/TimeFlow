/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.data.v1

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
data class WeekList(
    @ProtoNumber(1) val week: List<Int>
) {
    constructor(
        weekDescription: WeekDescriptionEnum,
        totalWeeks: Int,
        validWeeks: List<Int> = (1..totalWeeks).toList()
    ) : this(
        week = when (weekDescription) {
            WeekDescriptionEnum.ALL -> (1..totalWeeks).toList().filter { it in validWeeks }
            WeekDescriptionEnum.ODD -> (1..totalWeeks step 2).toList().filter { it in validWeeks }
            WeekDescriptionEnum.EVEN -> (2..totalWeeks step 2).toList().filter { it in validWeeks }
        }
    )

    constructor(weekRange: WeekRange) : this(
        week = weekRange.range.flatMap { it.start..it.end }.distinct()
    )

    fun getString(): String {
        if (week.isEmpty()) return ""

        val sortedWeeks = week.sorted()
        val result = mutableListOf<String>()
        var start = sortedWeeks[0]
        var end = sortedWeeks[0]

        for (i in 1 until sortedWeeks.size) {
            if (sortedWeeks[i] == end + 1) {
                end = sortedWeeks[i]
            } else {
                if (start == end) {
                    result.add(start.toString())
                } else {
                    result.add("$start-$end")
                }
                start = sortedWeeks[i]
                end = sortedWeeks[i]
            }
        }

        if (start == end) {
            result.add(start.toString())
        } else {
            result.add("$start-$end")
        }

        return result.joinToString(", ")
    }
}