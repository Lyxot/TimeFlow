/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow

import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import xyz.hyli.timeflow.api.models.ApiV1
import xyz.hyli.timeflow.api.models.Ping
import xyz.hyli.timeflow.api.models.User
import xyz.hyli.timeflow.api.models.Version
import xyz.hyli.timeflow.client.ApiClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun `test api`() = testApplication {
        environment {
            config = ApplicationConfig("application.yaml")
        }

        val tokenManager = FakeTokenManager()
        val apiClient = object : ApiClient(
            tokenManager = tokenManager,
            client = client,
        ) {}

        apiClient.use { client ->
            client.ping().apply {
                val message = "Ping response expected"
                assertEquals(HttpStatusCode.OK, status, message)
                assertEquals(Ping.Response(), body<Ping.Response>(), message)
                contentType()?.let { it1 -> assertTrue(it1.match(ContentType.Application.ProtoBuf), message) }
            }

            client.version().apply {
                val message = "Version response expected"
                assertEquals(HttpStatusCode.OK, status, message)
                assertEquals(
                    Version.Response(
                        version = BuildConfig.APP_VERSION_NAME,
                        buildTime = BuildConfig.BUILD_TIME,
                        commitHash = BuildConfig.GIT_COMMIT_HASH
                    ), body<Version.Response>(), message
                )
            }

            client.checkEmail(
                ApiV1.Auth.CheckEmail.Payload(
                    email = "test@test.com"
                )
            ).apply {
                val message = "None-existing email check"
                val body = body<ApiV1.Auth.CheckEmail.Response>()
                assertEquals(HttpStatusCode.OK, status, message)
                assertEquals(false, body.exists, message)
            }

            client.sendVerificationCode(
                ApiV1.Auth.SendVerificationCode.Payload(
                    email = "1@2.com"
                )
            ).apply {
                val message = "Verification code"
                assertEquals(HttpStatusCode.Accepted, status, message)
            }

            client.register(
                ApiV1.Auth.Register.Payload(
                    username = "testuser",
                    email = "test@test.com",
                    password = "password",
                    code = "000000"
                )
            ).apply {
                val message = "User registration"
                assertEquals(HttpStatusCode.Created, status, message)
            }

            client.login(
                ApiV1.Auth.Login.Payload(
                    email = "1@2.com",
                    password = "123",
                )
            ).apply {
                val message = "Invalid login attempt"
                assertEquals(HttpStatusCode.Unauthorized, status, message)
            }

            client.login(
                ApiV1.Auth.Login.Payload(
                    email = "test@test.com",
                    password = "password",
                )
            ).apply {
                val message = "Valid login attempt"
                assertEquals(HttpStatusCode.OK, status, message)
            }

            client.checkEmail(
                ApiV1.Auth.CheckEmail.Payload(
                    email = "test@test.com"
                )
            ).apply {
                val message = "Existing email check"
                val body = body<ApiV1.Auth.CheckEmail.Response>()
                assertEquals(HttpStatusCode.OK, status, message)
                assertEquals(true, body.exists, message)
            }

            client.me().apply {
                val message = "User Info"
                assertEquals(HttpStatusCode.OK, status, message)
                val body = body<User>()
                assertEquals("testuser", body.username, message)
                assertEquals("test@test.com", body.email, message)
            }

            tokenManager.setAccessToken("invalid_token")
            client.me().apply {
                val message = "Auto refresh access token"
                assertEquals(HttpStatusCode.OK, status, message)
            }
        }
    }

}
