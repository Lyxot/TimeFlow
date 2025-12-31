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
import xyz.hyli.timeflow.api.models.Version
import xyz.hyli.timeflow.client.ApiClient
import kotlin.test.Test
import kotlin.test.assertEquals

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
                assertEquals(HttpStatusCode.OK, status)
                assertEquals(Ping.Response(), body<Ping.Response>())
                contentType()?.let { it1 -> assertEquals(true, it1.match(ContentType.Application.ProtoBuf)) }
            }
            client.version().apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals(
                    Version.Response(
                        version = BuildConfig.APP_VERSION_NAME,
                        buildTime = BuildConfig.BUILD_TIME,
                        commitHash = BuildConfig.GIT_COMMIT_HASH
                    ), body<Version.Response>()
                )
            }
            client.checkEmail(
                ApiV1.Auth.CheckEmail.Payload(
                    email = "test@test.com"
                )
            ).apply {
                val body = body<ApiV1.Auth.CheckEmail.Response>()
                assertEquals(HttpStatusCode.OK, status)
                assertEquals(true, body.exists)
            }
            client.sendVerificationCode(
                ApiV1.Auth.SendVerificationCode.Payload(
                    email = "1@2.com"
                )
            ).apply {
                assertEquals(HttpStatusCode.Accepted, status)
            }
            client.register(
                ApiV1.Auth.Register.Payload(
                    email = "1@2.com",
                    password = "123",
                    code = "000000"
                )
            ).apply {
                assertEquals(HttpStatusCode.Created, status)
            }
            client.login(
                ApiV1.Auth.Login.Payload(
                    email = "1@2.com",
                    password = "123",
                )
            ).apply {
                assertEquals(HttpStatusCode.Unauthorized, status)
            }
            client.login(
                ApiV1.Auth.Login.Payload(
                    email = "test@test.com",
                    password = "password",
                )
            ).apply {
                assertEquals(HttpStatusCode.OK, status)
            }
            client.me()
            tokenManager.setAccessToken("invalid_token")
            client.me().apply {
                assertEquals(HttpStatusCode.OK, status)
            }
        }
    }

}
