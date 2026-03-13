/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow

import xyz.hyli.timeflow.client.TokenManager
import kotlin.time.Clock
import kotlin.time.Instant

class FakeTokenManager : TokenManager {
    private var refreshToken: String? = null
    private var refreshTokenExpiresAt: Instant? = null
    private var accessToken: String? = null
    private var tokenUsedTime = 0

    override fun getAccessToken(): String {
        tokenUsedTime += 1
        return accessToken ?: ""
    }

    override fun getRefreshToken(): String {
        return refreshToken ?: ""
    }

    override fun setAccessToken(accessToken: String) {
        tokenUsedTime = 0
        this.accessToken = accessToken
    }

    override fun setRefreshToken(refreshToken: String, expiresAt: Instant) {
        this.refreshToken = refreshToken
        this.refreshTokenExpiresAt = expiresAt
    }

    override fun isRefreshTokenNeedRotate(): Boolean {
        return tokenUsedTime >= 2
    }

    override fun isRefreshTokenExpired(): Boolean {
        return refreshToken == null || (refreshTokenExpiresAt?.let { it < Clock.System.now() } ?: true)
    }

    override fun clearTokens() {
        this.accessToken = null
        this.refreshToken = null
        this.refreshTokenExpiresAt = null
    }
}