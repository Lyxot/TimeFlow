/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.routes

import de.mkammerer.argon2.Argon2Factory
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
import xyz.hyli.timeflow.server.database.DataRepository
import xyz.hyli.timeflow.utils.toUuid
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 32, 64)

@OptIn(ExperimentalUuidApi::class)
fun Route.authRoutes(tokenManager: TokenManager, repository: DataRepository) {

    rateLimit(RateLimitName("login")) {
        // GET /api/v1/auth/check-email
        get<ApiV1.Auth.CheckEmail> { _ ->
            val payload = call.receive<ApiV1.Auth.CheckEmail.Payload>()
            val email = payload.email
            if (email.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email parameter cannot be blank"))
                return@get
            }

            val user = repository.findUserByEmail(email)
            call.respond(HttpStatusCode.OK, ApiV1.Auth.CheckEmail.Response(exists = user != null))
        }

        // POST /api/v1/auth/register
        post<ApiV1.Auth.Register> { _ ->
            val payload = call.receive<ApiV1.Auth.Register.Payload>()

            // 1. Check if user with this email already exists
            if (repository.findUserByEmail(payload.email) != null) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "User with this email already exists."))
                return@post
            }

            // TODO: 2. Validate the verification code `payload.code`.

            // 3. Hash the password with Argon2
            val passwordHash = argon2.hash(10, 65536, 1, payload.password.toCharArray())

            // 4. Create a unique authId and save the new user
            val authId = Uuid.generateV7()
            repository.createUser(authId, payload.username, payload.email, passwordHash)

            call.respond(HttpStatusCode.Created, mapOf("message" to "User created successfully"))
        }

        // POST /api/v1/auth/login
        post<ApiV1.Auth.Login> { _ ->
            val payload = call.receive<ApiV1.Auth.Login.Payload>()

            val (storedHash, user) = repository.findPasswordHashByEmail(payload.email)
                ?: (null to null)

            // Verify password using Argon2
            if (storedHash != null && user != null &&
                argon2.verify(storedHash, payload.password.toCharArray())
            ) {
                // Password is correct, generate tokens
                val (accessToken, _, _) = tokenManager.generateToken(user.authId, TokenManager.TokenType.ACCESS)
                val (refreshToken, jti, expiresAt) = tokenManager.generateToken(
                    user.authId,
                    TokenManager.TokenType.REFRESH
                )

                // Save the refresh token's JTI to the database
                repository.addRefreshToken(user.id, jti, expiresAt)

                call.respond(
                    HttpStatusCode.OK,
                    ApiV1.Auth.Login.Response(accessToken = accessToken, refreshToken = refreshToken)
                )
            } else {
                // Password is incorrect
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid Email or Password"))
            }
        }

        // Route protected by REFRESH token validation
        authenticate("refresh-auth") {
            // POST /api/v1/auth/refresh
            post<ApiV1.Auth.Refresh> { request ->
                val principal = call.principal<JWTPrincipal>()
                val authId = principal!!.payload.getClaim("authId").asString().toUuid()
                val user = repository.findUserByAuthId(authId)!!
                val jti = principal.jwtId!!.toUuid()

                val (newAccessToken, _, _) = tokenManager.generateToken(user.authId, TokenManager.TokenType.ACCESS)
                val newRefreshToken = if (request.rotate == true) {
                    // Generate new one and save it
                    val (newRefreshTokenString, newJti, expiresAt) = tokenManager.generateToken(
                        authId,
                        TokenManager.TokenType.REFRESH
                    )
                    repository.addRefreshToken(user.id, newJti, expiresAt)
                    repository.revokeRefreshToken(jti)
                    newRefreshTokenString
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
            // TODO: Implement real email sending logic.
            if (repository.findUserByEmail(payload.email) != null) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "User with this email already exists."))
            } else {
                call.respond(HttpStatusCode.Accepted)
            }
        }
    }
}
