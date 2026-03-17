/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.sync

import kotlinx.cinterop.*
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.*
import platform.Security.*
import kotlin.time.Instant

private const val SERVICE = "xyz.hyli.timeflow.tokens"
private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_REFRESH_TOKEN_EXPIRES_AT = "refresh_token_expires_at"

class IosSecureTokenStorage : SecureTokenStorage {
    override fun getAccessToken(): String? = keychainGet(KEY_ACCESS_TOKEN)

    override fun getRefreshToken(): String? = keychainGet(KEY_REFRESH_TOKEN)

    override fun getRefreshTokenExpiresAt(): Instant? {
        val value = keychainGet(KEY_REFRESH_TOKEN_EXPIRES_AT) ?: return null
        return value.toLongOrNull()?.let { Instant.fromEpochMilliseconds(it) }
    }

    override fun setTokens(accessToken: String, refreshToken: String?, refreshTokenExpiresAt: Instant?) {
        keychainSet(KEY_ACCESS_TOKEN, accessToken)
        keychainSet(KEY_REFRESH_TOKEN, refreshToken)
        keychainSet(KEY_REFRESH_TOKEN_EXPIRES_AT, refreshTokenExpiresAt?.toEpochMilliseconds()?.toString())
    }

    override fun clear() {
        keychainDelete(KEY_ACCESS_TOKEN)
        keychainDelete(KEY_REFRESH_TOKEN)
        keychainDelete(KEY_REFRESH_TOKEN_EXPIRES_AT)
    }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    private fun keychainGet(key: String): String? = memScoped {
        val query = CFDictionaryCreateMutable(null, 4, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetain(SERVICE))
        CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(key))
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

        val result = alloc<platform.CoreFoundation.CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        if (status == errSecSuccess) {
            val data = CFBridgingRelease(result.value) as? NSData
            data?.let { NSString.create(it, NSUTF8StringEncoding)?.toString() }
        } else null
    }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    private fun keychainSet(key: String, value: String?) {
        keychainDelete(key)
        if (value == null) return

        val nsString = NSString.create(string = value)
        val data = nsString.dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val query = CFDictionaryCreateMutable(null, 4, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetain(SERVICE))
        CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(key))
        CFDictionaryAddValue(query, kSecValueData, CFBridgingRetain(data))

        SecItemAdd(query, null)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun keychainDelete(key: String) {
        val query = CFDictionaryCreateMutable(null, 3, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetain(SERVICE))
        CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(key))

        SecItemDelete(query)
    }
}

actual fun createSecureTokenStorage(): SecureTokenStorage = IosSecureTokenStorage()
