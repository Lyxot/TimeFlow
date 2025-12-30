/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import xyz.hyli.timeflow.api.models.ApiV1
import xyz.hyli.timeflow.server.TokenManager

fun Route.authRoutes(tokenManager: TokenManager) {
    rateLimit(RateLimitName("login")) {
        // GET /api/v1/auth/check-email
        get<ApiV1.Auth.CheckEmail> { _ ->
            val payload = call.receive<ApiV1.Auth.CheckEmail.Payload>()
            val email = payload.email
            if (email.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email parameter cannot be blank"))
                return@get
            }

            // TODO: Replace with a real database lookup.
            val exists = (email == "test@test.com")

            call.respond(HttpStatusCode.OK, ApiV1.Auth.CheckEmail.Response(exists = exists))
        }

        // POST /api/v1/auth/register
        post<ApiV1.Auth.Register> { _ ->
            val payload = call.receive<ApiV1.Auth.Register.Payload>()

            // TODO: 1. Validate the verification code `request.code`.
            // TODO: 2. Check if user with this email already exists in the database.
            // TODO: 3. Use a secure hashing algorithm (like bcrypt) to hash the password before saving.
            // TODO: 4. Save the new user to the database.

            call.respond(HttpStatusCode.Created, mapOf("message" to "User created successfully"))
        }

        // POST /api/v1/auth/login
        post<ApiV1.Auth.Login> { _ ->
            val payload = call.receive<ApiV1.Auth.Login.Payload>()
            // TODO: Replace this with a real user lookup and password check from your database.
            val isCredentialsValid = payload.email == "test@test.com" && payload.password == "password"

            if (isCredentialsValid) {
                val userId = "user-id-123" // In a real app, you would fetch this from the database.

                // Generate two types of JWTs
                val accessToken = tokenManager.generateToken(userId, TokenManager.TokenType.ACCESS)
                val refreshToken = tokenManager.generateToken(userId, TokenManager.TokenType.REFRESH)

                // TODO: Save the hash of the refreshToken to the database to allow for revocation.

                call.respond(
                    HttpStatusCode.OK,
                    ApiV1.Auth.Login.Response(accessToken = accessToken, refreshToken = refreshToken)
                )
            } else {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
            }
        }

        // Route protected by REFRESH token validation
        authenticate("refresh-auth") {
            // POST /api/v1/auth/refresh
            post<ApiV1.Auth.Refresh> { request ->
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()

                // TODO: Add a DB check to see if this refresh token (e.g., its JTI) has been revoked.

                val newAccessToken = tokenManager.generateToken(userId, TokenManager.TokenType.ACCESS)
                val newRefreshToken = if (request.rotate == true) {
                    // TODO: Invalidate old refresh token and save the new one in the DB.
                    tokenManager.generateToken(userId, TokenManager.TokenType.REFRESH)
                } else {
                    null
                }
                val response = ApiV1.Auth.Refresh.Response(accessToken = newAccessToken, refreshToken = newRefreshToken)
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }

    // POST /api/v1/auth/send-verification-code
    rateLimit(RateLimitName("send_verification_code")) {
        post<ApiV1.Auth.SendVerificationCode> { _ ->
            val payload = call.receive<ApiV1.Auth.SendVerificationCode.Payload>()
            // TODO: Implement rate limiting and email sending logic.
            if (payload.email == "test@test.com") {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "Email already exists"))
                return@post
            }
            call.respond(HttpStatusCode.Accepted)
        }
    }
}
