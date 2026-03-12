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
import org.slf4j.LoggerFactory
import xyz.hyli.timeflow.api.models.ApiV1
import xyz.hyli.timeflow.server.AccessTokenBlacklist
import xyz.hyli.timeflow.server.EmailService
import xyz.hyli.timeflow.server.TokenManager
import xyz.hyli.timeflow.server.TurnstileService
import xyz.hyli.timeflow.server.database.DataRepository
import xyz.hyli.timeflow.utils.InputValidation
import xyz.hyli.timeflow.utils.toUuid
import kotlin.collections.mapOf
import kotlin.text.isNullOrBlank
import kotlin.text.toCharArray
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toKotlinInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val authLogger = LoggerFactory.getLogger("xyz.hyli.timeflow.server.Auth")
private val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 32, 64)

@OptIn(ExperimentalUuidApi::class)
fun Route.authRoutes(
    tokenManager: TokenManager,
    repository: DataRepository,
    emailService: EmailService,
    turnstileService: TurnstileService,
    accessTokenBlacklist: AccessTokenBlacklist
) {
    val verificationEnabled = emailService.verificationEnabled

    rateLimit(RateLimitName("login")) {
        // GET /api/v1/auth/email-verification
        get<ApiV1.Auth.EmailVerification> {
            call.respond(HttpStatusCode.OK, ApiV1.Auth.EmailVerification.Response(enabled = verificationEnabled))
        }

        // GET /api/v1/auth/check-email (stricter rate limit to prevent account enumeration)
        rateLimit(RateLimitName("check_email")) {
            get<ApiV1.Auth.CheckEmail> { request ->
                InputValidation.validateEmail(request.email)?.let { error ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
                    return@get
                }

                val user = repository.findUserByEmail(request.email)
                call.respond(HttpStatusCode.OK, ApiV1.Auth.CheckEmail.Response(exists = user != null))
            }
        }

        // POST /api/v1/auth/register
        post<ApiV1.Auth.Register> { _ ->
            val payload = call.receive<ApiV1.Auth.Register.Payload>()

            // 1. Validate input fields
            InputValidation.validateEmail(payload.email)?.let { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
                return@post
            }
            InputValidation.validateUsername(payload.username)?.let { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
                return@post
            }
            InputValidation.validatePassword(payload.password)?.let { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
                return@post
            }

            // 2. Check if user with this email already exists
            if (repository.findUserByEmail(payload.email) != null) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "User with this email already exists."))
                return@post
            }

            // 3. Validate the verification code when email verification is enabled
            if (verificationEnabled) {
                val code = payload.code
                if (code.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Verification code is required."))
                    return@post
                }
                if (!repository.validateVerificationCode(payload.email, code)) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid or expired verification code."))
                    return@post
                }
            }

            // 4. Hash the password with Argon2
            val passwordHash = argon2.hash(10, 65536, 1, payload.password.toCharArray())

            // 5. Create a unique authId and save the new user
            val authId = Uuid.generateV7()
            repository.createUser(authId, payload.username, payload.email, passwordHash)

            // 6. Delete the verification code after successful registration
            repository.deleteVerificationCodes(payload.email)

            authLogger.info("User registered: email={}, remoteHost={}", payload.email, call.request.local.remoteHost)
            call.respond(HttpStatusCode.Created, mapOf("message" to "User created successfully"))
        }

        // POST /api/v1/auth/login
        post<ApiV1.Auth.Login> { _ ->
            val payload = call.receive<ApiV1.Auth.Login.Payload>()

            // Validate email format
            InputValidation.validateEmail(payload.email)?.let { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
                return@post
            }

            val result = repository.findPasswordHashByEmail(payload.email)
            if (result == null) {
                authLogger.warn("Login failed (unknown email): email={}, remoteHost={}", payload.email, call.request.local.remoteHost)
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid Email or Password"))
                return@post
            }

            val (storedHash, user) = result

            // Verify password using Argon2
            val passwordMatches = try {
                argon2.verify(storedHash, payload.password.toCharArray())
            } catch (e: Exception) {
                authLogger.error("Argon2 verification error: email={}, error={}", payload.email, e.message)
                false
            }
            if (passwordMatches) {
                // Password is correct, generate tokens
                // Generate refresh token first to get its JTI
                val (refreshToken, refreshJti, expiresAt) = tokenManager.generateToken(
                    user.authId,
                    TokenManager.TokenType.REFRESH
                )

                // Save the refresh token's JTI to the database
                repository.addRefreshToken(user.id, refreshJti, expiresAt)
                // Generate access token with linked refreshJti
                val (accessToken, _, _) = tokenManager.generateToken(
                    user.authId,
                    TokenManager.TokenType.ACCESS,
                    refreshJti
                )

                authLogger.info("Login successful: email={}, remoteHost={}", payload.email, call.request.local.remoteHost)
                call.respond(
                    HttpStatusCode.OK,
                    ApiV1.Auth.Login.Response(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        refreshTokenExpiresAt = expiresAt.toKotlinInstant()
                    )
                )
            } else {
                // Password is incorrect
                authLogger.warn("Login failed (wrong password): email={}, remoteHost={}", payload.email, call.request.local.remoteHost)
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid Email or Password"))
            }
        }

        // Route protected by REFRESH token validation
        authenticate("refresh-auth") {
            // POST /api/v1/auth/refresh
            post<ApiV1.Auth.Refresh> { request ->
                val principal = call.principal<JWTPrincipal>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Missing or invalid token"))
                    return@post
                }
                val authId = principal.payload.getClaim("authId")?.asString()
                    ?.runCatching { toUuid() }?.getOrNull()
                if (authId == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                    return@post
                }
                val user = repository.findUserByAuthId(authId)
                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not found"))
                    return@post
                }
                val jti = principal.jwtId?.runCatching { toUuid() }?.getOrNull()
                if (jti == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                    return@post
                }

                val (newRefreshToken, activeRefreshJti, refreshExpiresAt) = if (request.rotate == true) {
                    // Generate new refresh token and save it
                    val (newRefreshTokenString, newJti, expiresAt) = tokenManager.generateToken(
                        authId,
                        TokenManager.TokenType.REFRESH
                    )
                    repository.addRefreshToken(user.id, newJti, expiresAt)
                    repository.revokeRefreshToken(jti)
                    Triple(newRefreshTokenString, newJti, expiresAt.toKotlinInstant())
                } else {
                    Triple(null, jti, null)
                }
                // Generate access token linked to the active refresh token
                val (newAccessToken, _, _) = tokenManager.generateToken(
                    user.authId,
                    TokenManager.TokenType.ACCESS,
                    activeRefreshJti
                )

                val response = ApiV1.Auth.Refresh.Response(
                    accessToken = newAccessToken,
                    refreshToken = newRefreshToken,
                    refreshTokenExpiresAt = refreshExpiresAt
                )
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }

    // POST /api/v1/auth/send-verification-code
    rateLimit(RateLimitName("send_verification_code")) {
        post<ApiV1.Auth.SendVerificationCode> { _ ->
            val payload = call.receive<ApiV1.Auth.SendVerificationCode.Payload>()

            if (!verificationEnabled) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email verification is disabled."))
                return@post
            }

            if (turnstileService.enabled) {
                val turnstileToken = payload.turnstileToken
                if (turnstileToken.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Turnstile token is required."))
                    return@post
                }

                when (
                    val result = turnstileService.verify(
                        token = turnstileToken,
                        remoteIp = call.request.local.remoteHost
                    )
                ) {
                    TurnstileService.VerificationResult.Success -> Unit
                    is TurnstileService.VerificationResult.Rejected -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Turnstile verification failed.")
                        )
                        return@post
                    }

                    is TurnstileService.VerificationResult.Failure -> {
                        call.respond(
                            HttpStatusCode.ServiceUnavailable,
                            mapOf("error" to "Turnstile verification is unavailable.")
                        )
                        return@post
                    }
                }
            }

            // Validate email format
            InputValidation.validateEmail(payload.email)?.let { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
                return@post
            }

            // Check if user with this email already exists
            // Return 202 regardless to prevent account enumeration
            if (repository.findUserByEmail(payload.email) != null) {
                call.respond(HttpStatusCode.Accepted)
                return@post
            }

            // Generate and store verification code (with 1-minute rate limit)
            val code = emailService.generateCode()
            val expiresAt = Clock.System.now() + emailService.codeExpirationMinutes.minutes
            val created = repository.createVerificationCode(payload.email, code, expiresAt)

            if (!created) {
                call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "Please wait 1 minute before requesting a new code."))
                return@post
            }

            // Send verification email
            try {
                emailService.sendVerificationCode(payload.email, code)
                call.respond(HttpStatusCode.Accepted)
            } catch (e: Exception) {
                authLogger.error("Failed to send verification email: email={}, error={}", payload.email, e.message)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to send verification email."))
            }
        }
    }

    // Route protected by ACCESS token validation
    authenticate("access-auth") {
        // POST /api/v1/auth/logout
        post<ApiV1.Auth.Logout> { request ->
            val principal = call.principal<JWTPrincipal>()
            if (principal == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Missing or invalid token"))
                return@post
            }

            // Blacklist the current access token so it can't be reused after logout
            principal.jwtId?.let { jti ->
                principal.expiresAt?.toInstant()?.toKotlinInstant()?.let { expiresAt ->
                    accessTokenBlacklist.add(jti, expiresAt)
                }
            }

            val authIdStr = principal.payload.getClaim("authId")?.asString()
            val authId = authIdStr?.runCatching { toUuid() }?.getOrNull()
            if (request.all == true) {
                if (authId == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                    return@post
                }
                val user = repository.findUserByAuthId(authId)
                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not found"))
                    return@post
                }
                repository.revokeAllRefreshTokens(user.id)
                authLogger.info("Logout (all sessions): authId={}, remoteHost={}", authIdStr, call.request.local.remoteHost)
            } else {
                val refreshJti = principal.payload.getClaim("refreshJti")?.asString()
                    ?.runCatching { toUuid() }?.getOrNull()
                if (refreshJti != null) {
                    repository.revokeRefreshToken(refreshJti)
                }
                authLogger.info("Logout: authId={}, remoteHost={}", authIdStr, call.request.local.remoteHost)
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}
