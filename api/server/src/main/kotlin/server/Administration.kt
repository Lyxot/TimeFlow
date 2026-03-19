/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun Application.configureAdministration() {
    val testing = isTestMode
    install(RateLimit) {
        global {
            rateLimiter(limit = 100, refillPeriod = 10.seconds)
        }
        // limiter for login/register
        register(RateLimitName("login")) {
            val limit = if (testing) 12 else 6
            rateLimiter(limit = limit, refillPeriod = 1.minutes)
        }
        // stricter limiter for email existence checks to prevent account enumeration
        register(RateLimitName("check_email")) {
            val limit = if (testing) 10 else 5
            rateLimiter(limit = limit, refillPeriod = 1.minutes)
        }
        // limiter for email verification code sending
        register(RateLimitName("send_verification_code")) {
            val limit = if (testing) 2 else 1
            rateLimiter(limit = limit, refillPeriod = 1.minutes)
        }
        // burst protection for AI extraction (per-user quota is handled in application logic)
        register(RateLimitName("ai")) {
            val limit = if (testing) 4 else 2
            rateLimiter(limit = limit, refillPeriod = 1.minutes)
        }
    }
}
