/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server

import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * In-memory blacklist for revoked access tokens.
 * Entries are automatically evicted after the access token TTL expires,
 * since expired tokens are already rejected by JWT verification.
 */
class AccessTokenBlacklist {
    private val blacklist = ConcurrentHashMap<String, Instant>()

    /**
     * Blacklist an access token JTI until it naturally expires.
     * @param jti The JWT ID of the access token to blacklist.
     * @param expiresAt When the token expires (entries are cleaned up after this time).
     */
    fun add(jti: String, expiresAt: Instant) {
        cleanup()
        blacklist[jti] = expiresAt
    }

    /**
     * Check if a token JTI has been blacklisted.
     */
    fun isBlacklisted(jti: String): Boolean {
        return blacklist.containsKey(jti)
    }

    private fun cleanup() {
        val now = Clock.System.now()
        blacklist.entries.removeIf { it.value <= now }
    }
}
