/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class TurnstileService(
    config: ApplicationConfig,
    private val client: HttpClient = HttpClient(Apache5) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }
) : AutoCloseable {
    private val testing = config.propertyOrNull("testing")?.getString()?.toBoolean() ?: false
    val enabled = config.propertyOrNull("turnstile.enabled")?.getString()?.toBoolean() ?: false
    private val secretKey = config.propertyOrNull("turnstile.secretKey")?.getString()?.takeIf { it.isNotBlank() }
    private val siteVerifyUrl =
        config.propertyOrNull("turnstile.siteVerifyUrl")?.getString() ?: DEFAULT_SITE_VERIFY_URL

    init {
        if (enabled && !testing && secretKey == null) {
            throw IllegalStateException("turnstile.secretKey must be configured when turnstile.enabled is true.")
        }
    }

    suspend fun verify(token: String, remoteIp: String? = null): VerificationResult {
        if (!enabled) {
            return VerificationResult.Success
        }

        if (testing) {
            return if (token == TESTING_TOKEN) {
                VerificationResult.Success
            } else {
                VerificationResult.Rejected(listOf("invalid-input-response"))
            }
        }

        val response = client.submitForm(
            url = siteVerifyUrl,
            formParameters = Parameters.build {
                append("secret", checkNotNull(secretKey))
                append("response", token)
                remoteIp?.takeIf { it.isNotBlank() }?.let { append("remoteip", it) }
            }
        )

        if (response.status.value !in 200..299) {
            return VerificationResult.Failure(response.status)
        }

        val body = response.body<SiteVerifyResponse>()
        return if (body.success) {
            VerificationResult.Success
        } else {
            VerificationResult.Rejected(body.errorCodes)
        }
    }

    override fun close() {
        client.close()
    }

    @Serializable
    private data class SiteVerifyResponse(
        val success: Boolean,
        @SerialName("error-codes")
        val errorCodes: List<String> = emptyList(),
    )

    sealed interface VerificationResult {
        data object Success : VerificationResult
        data class Rejected(val errorCodes: List<String>) : VerificationResult
        data class Failure(val status: HttpStatusCode) : VerificationResult
    }

    companion object {
        const val DEFAULT_SITE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify"
        const val TESTING_TOKEN = "turnstile-testing-pass"
    }
}
