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
    @ProtoNumber(1) val weeks: List<Int>
) {
    /**
     * 根据周的描述（全部、单/双周）创建一个 [WeekList] 实例。
     *
     * @param weekDescription 周的描述类型（[WeekDescriptionEnum.ALL], [WeekDescriptionEnum.ODD], [WeekDescriptionEnum.EVEN]）。
     * @param totalWeeks 总教学周数。
     * @param validWeeks 一个可选的列表，用于限定生成的周次必须在此列表范围内。
     */
    constructor(
        weekDescription: WeekDescriptionEnum,
        totalWeeks: Int,
        validWeeks: List<Int> = (1..totalWeeks).toList()
    ) : this(
        weeks = when (weekDescription) {
            WeekDescriptionEnum.ALL -> validWeeks.filter { it in 1..totalWeeks }
            WeekDescriptionEnum.ODD -> validWeeks.filter { it % 2 != 0 && it in 1..totalWeeks }
            WeekDescriptionEnum.EVEN -> validWeeks.filter { it % 2 == 0 && it in 1..totalWeeks }
        }
    )

    /**
     * 根据周的范围（例如 1-8周, 10-16周）创建一个 [WeekList] 实例。
     *
     * @param weekRange 包含一个或多个 [Range] 对象的 [WeekRange]。
     */
    constructor(weekRange: WeekRange) : this(
        weeks = weekRange.range.flatMap { it.start..it.end }.distinct()
    )

    /**
     * 将周次列表转换为一个易于阅读的、合并了连续周的字符串。
     * 例如: [1, 2, 3, 5, 8, 9] -> "1-3, 5, 8-9"
     *
     * @return 格式化后的周次字符串。
     */
    override fun toString(): String {
        if (weeks.isEmpty()) return ""

        val sortedWeeks = weeks.sorted()
        val result = mutableListOf<String>()
        var start = sortedWeeks[0]
        var end = sortedWeeks[0]

        for (i in 1 until sortedWeeks.size) {
            if (sortedWeeks[i] == end + 1) {
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

    /**
     * 检查当前周列表是否与另一个周列表存在交集。
     *
     * @param other 要比较的另一个 [WeekList] 对象。
     * @return 如果存在任何相同的周次，则返回 `true`，否则返回 `false`。
     */
    fun intersects(other: WeekList): Boolean {
        val smallerSet =
            if (this.weeks.size < other.weeks.size) this.weeks.toSet() else other.weeks.toSet()
        val largerList = if (this.weeks.size < other.weeks.size) other.weeks else this.weeks
        return largerList.any { it in smallerSet }
    }
}