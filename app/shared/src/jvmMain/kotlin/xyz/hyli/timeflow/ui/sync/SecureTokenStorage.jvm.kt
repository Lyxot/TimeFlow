/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.sync

import java.util.prefs.Preferences
import kotlin.time.Instant

private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_REFRESH_TOKEN_EXPIRES_AT = "refresh_token_expires_at"

class JvmSecureTokenStorage : SecureTokenStorage {
    private val prefs: Preferences = Preferences.userNodeForPackage(JvmSecureTokenStorage::class.java)

    override fun getAccessToken(): String? = prefs.get(KEY_ACCESS_TOKEN, null)

    override fun getRefreshToken(): String? = prefs.get(KEY_REFRESH_TOKEN, null)

    override fun getRefreshTokenExpiresAt(): Instant? {
        val millis = prefs.getLong(KEY_REFRESH_TOKEN_EXPIRES_AT, -1L)
        return if (millis >= 0) Instant.fromEpochMilliseconds(millis) else null
    }

    override fun setTokens(accessToken: String, refreshToken: String?, refreshTokenExpiresAt: Instant?) {
        prefs.put(KEY_ACCESS_TOKEN, accessToken)
        if (refreshToken != null) prefs.put(KEY_REFRESH_TOKEN, refreshToken) else prefs.remove(KEY_REFRESH_TOKEN)
        if (refreshTokenExpiresAt != null) prefs.putLong(
            KEY_REFRESH_TOKEN_EXPIRES_AT,
            refreshTokenExpiresAt.toEpochMilliseconds()
        )
        else prefs.remove(KEY_REFRESH_TOKEN_EXPIRES_AT)
        prefs.flush()
    }

    override fun clear() {
        prefs.remove(KEY_ACCESS_TOKEN)
        prefs.remove(KEY_REFRESH_TOKEN)
        prefs.remove(KEY_REFRESH_TOKEN_EXPIRES_AT)
        prefs.flush()
    }
}

actual fun createSecureTokenStorage(): SecureTokenStorage = JvmSecureTokenStorage()
