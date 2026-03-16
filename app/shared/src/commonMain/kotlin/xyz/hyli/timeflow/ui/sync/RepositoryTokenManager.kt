/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import xyz.hyli.timeflow.client.TokenManager
import xyz.hyli.timeflow.data.Settings
import xyz.hyli.timeflow.di.IDataRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class RepositoryTokenManager(
    private val repository: IDataRepository,
    private val settingsSnapshot: () -> Settings,
    private val scope: CoroutineScope,
) : TokenManager {

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedRefreshToken: String? = null

    @Volatile
    private var cachedRefreshTokenExpiresAt: Instant? = null

    fun syncFromSettings(settings: Settings) {
        cachedAccessToken = settings.accessToken
        cachedRefreshToken = settings.refreshToken
        cachedRefreshTokenExpiresAt = settings.refreshTokenExpiresAt
    }

    override fun getAccessToken(): String {
        return cachedAccessToken ?: settingsSnapshot().accessToken ?: ""
    }

    override fun getRefreshToken(): String {
        return cachedRefreshToken ?: settingsSnapshot().refreshToken ?: ""
    }

    override fun setAccessToken(accessToken: String) {
        cachedAccessToken = accessToken
        scope.launch { repository.updateAccessToken(accessToken) }
    }

    override fun setRefreshToken(refreshToken: String, expiresAt: Instant) {
        cachedRefreshToken = refreshToken
        cachedRefreshTokenExpiresAt = expiresAt
        scope.launch { repository.updateRefreshToken(refreshToken, expiresAt) }
    }

    override fun isRefreshTokenNeedRotate(): Boolean {
        val expiresAt = cachedRefreshTokenExpiresAt
            ?: settingsSnapshot().refreshTokenExpiresAt
            ?: return false
        return (expiresAt - Clock.System.now()) < 7.days
    }

    override fun isRefreshTokenExpired(): Boolean {
        val expiresAt = cachedRefreshTokenExpiresAt
            ?: settingsSnapshot().refreshTokenExpiresAt
            ?: return true
        return Clock.System.now() > expiresAt
    }

    override fun clearTokens() {
        cachedAccessToken = null
        cachedRefreshToken = null
        cachedRefreshTokenExpiresAt = null
        scope.launch {
            repository.updateAccessToken(null)
            repository.updateRefreshToken(null, null)
        }
    }
}
