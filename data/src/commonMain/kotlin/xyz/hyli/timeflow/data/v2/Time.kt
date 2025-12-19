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
data class Time(
    @ProtoNumber(1) val hour: Int,
    @ProtoNumber(2) val minute: Int
) {
    /**
     * 返回时间的字符串表示形式，格式为 "HH:mm"。
     * 小时和分钟如果小于10，会用 '0' 补齐。
     *
     * @return 格式化后的时间字符串。
     */
    override fun toString(): String {
        return this.hour.toString().padStart(2, '0') +
                ":" + this.minute.toString().padStart(2, '0')
    }

    /**
     * 比较此时间与另一个时间对象。
     *
     * @param b 要比较的另一个 [Time] 对象。
     * @return 如果此时间晚于另一个时间，则返回正数；如果早于，则返回负数；如果相同，则返回0。
     */
    operator fun compareTo(b: Time): Int {
        return when {
            this.hour != b.hour -> this.hour - b.hour
            else -> this.minute - b.minute
        }
    }

    /**
     * 在当前时间上增加指定的分钟数。
     * 会正确处理小时和天的进位（时间会回绕到第二天）。
     *
     * @param minutes 要增加的分钟数。
     * @return 计算后的新 [Time] 对象。
     */
    fun addMinutes(minutes: Int): Time {
        val totalMinutes = this.hour * 60 + this.minute + minutes
        val newHour = (totalMinutes / 60) % 24
        val newMinute = totalMinutes % 60
        return Time(newHour, newMinute)
    }

    /**
     * 计算当前时间与另一个时间之间的分钟差。
     *
     * @param other 要比较的另一个 [Time] 对象。
     * @return 两个时间之间的分钟差（`this` - `other`）。
     */
    fun minutesSince(other: Time): Int {
        return (this.hour * 60 + this.minute) - (other.hour * 60 + other.minute)
    }
}