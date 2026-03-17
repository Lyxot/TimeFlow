/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.sync

import kotlin.time.Instant

interface SecureTokenStorage {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun getRefreshTokenExpiresAt(): Instant?
    fun setTokens(accessToken: String, refreshToken: String?, refreshTokenExpiresAt: Instant?)
    fun clear()
}

expect fun createSecureTokenStorage(): SecureTokenStorage
