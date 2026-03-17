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
import xyz.hyli.timeflow.data.Tokens
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

    private var cachedTokens: Tokens? = null

    fun syncFromSettings(settings: Settings) {
        cachedTokens = settings.tokens
    }

    override fun getAccessToken(): String {
        return cachedTokens?.accessToken ?: settingsSnapshot().tokens?.accessToken ?: ""
    }

    override fun getRefreshToken(): String {
        return cachedTokens?.refreshToken ?: settingsSnapshot().tokens?.refreshToken ?: ""
    }

    override fun setAccessToken(accessToken: String) {
        cachedTokens = (cachedTokens ?: Tokens(accessToken = "")).copy(accessToken = accessToken)
        scope.launch { repository.updateTokens(cachedTokens) }
    }

    override fun setRefreshToken(refreshToken: String, expiresAt: Instant) {
        cachedTokens = (cachedTokens ?: Tokens(accessToken = "")).copy(
            refreshToken = refreshToken,
            refreshTokenExpiresAt = expiresAt
        )
        scope.launch { repository.updateTokens(cachedTokens) }
    }

    override fun isRefreshTokenNeedRotate(): Boolean {
        val expiresAt = cachedTokens?.refreshTokenExpiresAt
            ?: settingsSnapshot().tokens?.refreshTokenExpiresAt
            ?: return false
        return (expiresAt - Clock.System.now()) < 7.days
    }

    override fun isRefreshTokenExpired(): Boolean {
        val expiresAt = cachedTokens?.refreshTokenExpiresAt
            ?: settingsSnapshot().tokens?.refreshTokenExpiresAt
            ?: return true
        return Clock.System.now() > expiresAt
    }

    override fun clearTokens() {
        cachedTokens = null
        scope.launch { repository.updateTokens(null) }
    }
}
