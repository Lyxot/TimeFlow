/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.sync

import kotlinx.browser.window
import kotlin.time.Instant

private const val KEY_ACCESS_TOKEN = "timeflow_token_access"
private const val KEY_REFRESH_TOKEN = "timeflow_token_refresh"
private const val KEY_REFRESH_TOKEN_EXPIRES_AT = "timeflow_token_refresh_expires"

class WebSecureTokenStorage : SecureTokenStorage {
    override fun getAccessToken(): String? = window.localStorage.getItem(KEY_ACCESS_TOKEN)

    override fun getRefreshToken(): String? = window.localStorage.getItem(KEY_REFRESH_TOKEN)

    override fun getRefreshTokenExpiresAt(): Instant? {
        val value = window.localStorage.getItem(KEY_REFRESH_TOKEN_EXPIRES_AT) ?: return null
        return value.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) }
    }

    override fun setTokens(accessToken: String, refreshToken: String?, refreshTokenExpiresAt: Instant?) {
        window.localStorage.setItem(KEY_ACCESS_TOKEN, accessToken)
        if (refreshToken != null) window.localStorage.setItem(KEY_REFRESH_TOKEN, refreshToken)
        else window.localStorage.removeItem(KEY_REFRESH_TOKEN)
        if (refreshTokenExpiresAt != null) window.localStorage.setItem(
            KEY_REFRESH_TOKEN_EXPIRES_AT,
            refreshTokenExpiresAt.toEpochMilliseconds().toString()
        )
        else window.localStorage.removeItem(KEY_REFRESH_TOKEN_EXPIRES_AT)
    }

    override fun clear() {
        window.localStorage.removeItem(KEY_ACCESS_TOKEN)
        window.localStorage.removeItem(KEY_REFRESH_TOKEN)
        window.localStorage.removeItem(KEY_REFRESH_TOKEN_EXPIRES_AT)
    }
}

actual fun createSecureTokenStorage(): SecureTokenStorage = WebSecureTokenStorage()
