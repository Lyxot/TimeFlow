/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.sync

import xyz.hyli.timeflow.client.TokenManager
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class RepositoryTokenManager(
    private val secureStorage: SecureTokenStorage,
) : TokenManager {

    override fun getAccessToken(): String {
        return secureStorage.getAccessToken() ?: ""
    }

    override fun getRefreshToken(): String {
        return secureStorage.getRefreshToken() ?: ""
    }

    override fun setAccessToken(accessToken: String) {
        secureStorage.setTokens(
            accessToken = accessToken,
            refreshToken = secureStorage.getRefreshToken(),
            refreshTokenExpiresAt = secureStorage.getRefreshTokenExpiresAt()
        )
    }

    override fun setRefreshToken(refreshToken: String, expiresAt: Instant) {
        secureStorage.setTokens(
            accessToken = secureStorage.getAccessToken() ?: "",
            refreshToken = refreshToken,
            refreshTokenExpiresAt = expiresAt
        )
    }

    override fun isRefreshTokenNeedRotate(): Boolean {
        val expiresAt = secureStorage.getRefreshTokenExpiresAt() ?: return false
        return (expiresAt - Clock.System.now()) < 7.days
    }

    override fun isRefreshTokenExpired(): Boolean {
        val expiresAt = secureStorage.getRefreshTokenExpiresAt() ?: return true
        return Clock.System.now() > expiresAt
    }

    override fun clearTokens() {
        secureStorage.clear()
    }

    fun hasTokens(): Boolean {
        return !secureStorage.getAccessToken().isNullOrBlank() && !secureStorage.getRefreshToken().isNullOrBlank()
    }
}
