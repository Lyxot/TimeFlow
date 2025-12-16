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
data class Settings(
    @ProtoNumber(999) val initialized: Boolean = true, // Whether the settings have been initialized, always true
    @ProtoNumber(1) val firstLaunch: Int = 0, // 0: Not launched, Version Code: Last launched version code
    @ProtoNumber(2) val theme: Int = 0, // 0: System, 1: Light, 2: Dark
    @ProtoNumber(3) val themeDynamicColor: Boolean = false, // Whether dynamic color is enabled, only applicable for Android 12 and above
    @ProtoNumber(4) val themeColor: Int = (0xFFCBDDEE).toInt(), // Default seed color in RGB format
    @ProtoNumber(5) val schedule: Map<String, Schedule> = emptyMap(), // A map of schedules, where the key is the schedule name and the value is the Schedule object
    @ProtoNumber(6) val selectedSchedule: String = "", // The name of the selected schedule, empty if no schedule is selected
    @ProtoNumber(7) val reserved7: String? = null,
    @ProtoNumber(8) val reserved8: String? = null,
    @ProtoNumber(9) val reserved9: String? = null,
    @ProtoNumber(10) val reserved10: String? = null,
    @ProtoNumber(11) val reserved11: String? = null,
)