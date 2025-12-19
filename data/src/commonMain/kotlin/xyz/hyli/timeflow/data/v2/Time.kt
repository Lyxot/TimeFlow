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
    override fun toString(): String {
        return this.hour.toString().padStart(2, '0') +
                ":" + this.minute.toString().padStart(2, '0')
    }

    operator fun compareTo(b: Time): Int {
        return when {
            this.hour != b.hour -> this.hour - b.hour
            else -> this.minute - b.minute
        }
    }

    fun addMinutes(minutes: Int): Time {
        val totalMinutes = this.hour * 60 + this.minute + minutes
        val newHour = (totalMinutes / 60) % 24
        val newMinute = totalMinutes % 60
        return Time(newHour, newMinute)
    }

    fun minutesSince(other: Time): Int {
        return (this.hour * 60 + this.minute) - (other.hour * 60 + other.minute)
    }
}