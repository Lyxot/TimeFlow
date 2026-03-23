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
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Tokens(
    val accessToken: String,
    val refreshToken: String? = null,
    val refreshTokenExpiresAt: Instant? = null
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class User(
    val id: Int = 0,
    val authId: Uuid = Uuid.NIL,
    val username: String = "",
    val email: String = ""
)

@Serializable
data class SelectedSchedule(
    val scheduleId: Short?,
    val updatedAt: Instant?
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
enum class AiProviderFormat {
    @ProtoNumber(1)
    OPENAI,
    @ProtoNumber(2)
    GOOGLE,
    @ProtoNumber(3)
    ANTHROPIC,
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AiProviderConfig(
    @ProtoNumber(1) val enabled: Boolean = false,
    @ProtoNumber(2) val provider: AiProviderFormat = AiProviderFormat.OPENAI,
    @ProtoNumber(3) val endpoint: String = "",
    @ProtoNumber(4) val apiKey: String = "",
    @ProtoNumber(5) val model: String = ""
)
