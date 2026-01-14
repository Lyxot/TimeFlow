/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import xyz.hyli.timeflow.server.database.DataRepository
import xyz.hyli.timeflow.utils.toUuid
import java.time.Instant
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Application.configureSecurity(repository: DataRepository) {

    val jwtSecret = environment.config.property("jwt.secret").getString()
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()

    install(Authentication) {
        // Provider for validating Access Tokens
        jwt("access-auth") {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("type").asInt() == TokenManager.TokenType.ACCESS.ordinal &&
                    credential.payload.getClaim("authId").asString() != "" &&
                    credential.payload.getClaim("refreshJti").asString() != ""
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }

        // Provider for validating Refresh Tokens
        jwt("refresh-auth") {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                val authId = credential.payload.getClaim("authId").asString().toUuid()
                val jti = credential.jwtId?.toUuid()
                val user = repository.findUserByAuthId(authId)
                if (credential.payload.getClaim("type").asInt() == TokenManager.TokenType.REFRESH.ordinal &&
                    user != null && jti != null && repository.isRefreshTokenValid(user.id, jti)
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

class TokenManager(config: io.ktor.server.config.ApplicationConfig) {
    private val audience = config.property("jwt.audience").getString()
    private val secret = config.property("jwt.secret").getString()
    private val issuer = config.property("jwt.issuer").getString()

    enum class TokenType(val validityInMs: Long) {
        ACCESS(600_000L), // 10 minutes
        REFRESH(3_600_000L * 24 * 21) // 21 days
    }

    /**
     * Generates a JWT for the given user ID and token type.
     * @param authId The user's authentication ID.
     * @param type The type of token to generate (ACCESS or REFRESH).
     * @param refreshJti For ACCESS tokens only: the JTI of the linked refresh token.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun generateToken(authId: Uuid, type: TokenType, refreshJti: Uuid? = null): Triple<String, Uuid, Instant> {
        val jti = Uuid.generateV7()
        val expiresAt = Date(System.currentTimeMillis() + type.validityInMs).toInstant()
        val token = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withJWTId(jti.toString())
            .withClaim("authId", authId.toString())
            .withClaim("type", type.ordinal)
            .withExpiresAt(expiresAt)
            .apply {
                if (type == TokenType.ACCESS) {
                    if (refreshJti == null) {
                        throw IllegalArgumentException("refreshJti must be provided for ACCESS tokens")
                    } else {
                        withClaim("refreshJti", refreshJti.toString())
                    }
                }
            }
            .sign(Algorithm.HMAC256(secret))
        return Triple(token, jti, expiresAt)
    }
}
