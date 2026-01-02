/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.api.models

import kotlinx.serialization.Serializable

// 这个 data class 作为 Service 和 Repository 之间传输用户数据的标准模型
@Serializable
data class User(
    val id: Int,
    val authId: String,
    val username: String,
    val email: String
)