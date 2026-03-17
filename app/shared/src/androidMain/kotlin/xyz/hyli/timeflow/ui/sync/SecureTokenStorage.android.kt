/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.sync

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import kotlin.time.Instant

private const val PREFS_NAME = "timeflow_secure_tokens"
private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_REFRESH_TOKEN_EXPIRES_AT = "refresh_token_expires_at"

@SuppressLint("StaticFieldLeak")
object AndroidSecureTokenStorageHolder {
    lateinit var context: Context
}

class AndroidSecureTokenStorage(context: Context) : SecureTokenStorage {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    override fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    override fun getRefreshTokenExpiresAt(): Instant? {
        val millis = prefs.getLong(KEY_REFRESH_TOKEN_EXPIRES_AT, -1L)
        return if (millis >= 0) Instant.fromEpochMilliseconds(millis) else null
    }

    override fun setTokens(accessToken: String, refreshToken: String?, refreshTokenExpiresAt: Instant?) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_REFRESH_TOKEN_EXPIRES_AT, refreshTokenExpiresAt?.toEpochMilliseconds() ?: -1L)
            .apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }
}

actual fun createSecureTokenStorage(): SecureTokenStorage =
    AndroidSecureTokenStorage(AndroidSecureTokenStorageHolder.context)
