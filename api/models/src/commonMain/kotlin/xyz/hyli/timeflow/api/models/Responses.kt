/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.api.models

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(val accessToken: String, val refreshToken: String? = null)

@Serializable
data class UserInfoResponse(val id: String, val email: String)

@Serializable
data class CheckEmailResponse(val exists: Boolean)

@Resource("/users/{id}")
data class UserById(val id: Long, val properties: List<String>)