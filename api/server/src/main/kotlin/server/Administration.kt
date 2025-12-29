/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun Application.configureAdministration() {
    routing {
        route("/") {
            install(RateLimit) {
                global {
                    rateLimiter(limit = 100, refillPeriod = 10.seconds)
                }
                // limiter for login/register
                register(RateLimitName("login")) {
                    rateLimiter(limit = 6, refillPeriod = 1.minutes)
                }
                // limiter for email verification code sending
                register(RateLimitName("send_verification_code")) {
                    rateLimiter(limit = 1, refillPeriod = 1.minutes)
                }
            }
        }
    }
}
