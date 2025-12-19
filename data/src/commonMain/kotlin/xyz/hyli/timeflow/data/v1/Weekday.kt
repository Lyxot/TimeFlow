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
enum class Weekday {
    @ProtoNumber(1)
    MONDAY,

    @ProtoNumber(2)
    TUESDAY,

    @ProtoNumber(3)
    WEDNESDAY,

    @ProtoNumber(4)
    THURSDAY,

    @ProtoNumber(5)
    FRIDAY,

    @ProtoNumber(6)
    SATURDAY,

    @ProtoNumber(7)
    SUNDAY,
}
