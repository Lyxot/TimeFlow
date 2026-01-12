/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.callid.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.kotlinx.protobuf.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import xyz.hyli.timeflow.api.models.ApiV1
import xyz.hyli.timeflow.api.models.Ping
import xyz.hyli.timeflow.api.models.Version
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ApiClient(
    private val tokenManager: TokenManager,
    val endpoint: String? = null,
    client: HttpClient? = null,
) : AutoCloseable {
    @OptIn(ExperimentalUuidApi::class, ExperimentalSerializationApi::class)
    private fun HttpClientConfig<*>.configureBase() {
        install(Logging) {
            sanitizeHeader { header ->
                header == HttpHeaders.Authorization
            }
            logger = object : Logger {
                override fun log(message: String) {
                    co.touchlab.kermit.Logger.i(message)
                }
            }
        }
        install(HttpRequestRetry)
        install(HttpCache)
        install(Resources)
        install(CallId) {
            generate {
                // time-ordered UUID
                Uuid.generateV7().toString()
            }
            addToHeader(HttpHeaders.XRequestId)
        }
        install(ContentEncoding) {
            gzip()
            deflate()
        }
        install(ContentNegotiation) {
            // highest priority for protobuf
            protobuf(
                ProtoBuf {
                    encodeDefaults = false
                }
            )
            json()
        }
        defaultRequest {
            endpoint?.let { url(it) }
        }
    }

    private val httpClient = client?.config { configureBase() } ?: HttpClient(HttpEngine) { configureBase() }
    private val authenticatedClient: HttpClient = httpClient.config {
        install(Auth) {
            bearer {
                loadTokens {
                    val accessToken = tokenManager.getAccessToken()
                    val refreshToken = tokenManager.getRefreshToken()
                    BearerTokens(accessToken, refreshToken)
                }
                refreshTokens {
                    refresh(tokenManager.isRefreshTokenNeedRotate())
                    val accessToken = tokenManager.getAccessToken()
                    val refreshToken = tokenManager.getRefreshToken()
                    BearerTokens(accessToken, refreshToken)
                }
            }
        }
    }

    private var contentType = ContentType.Application.Json

    typealias Version = Version.Response

    lateinit var apiVersion: Version

    override fun close() {
        httpClient.close()
        authenticatedClient.close()
    }

    suspend fun ping() =
        httpClient.get(Ping())
            .apply {
                contentType()?.let {
                    if (it.match(ContentType.Application.ProtoBuf)) {
                        contentType = ContentType.Application.ProtoBuf
                    }
                }
            }

    suspend fun version() =
        httpClient.get(Version())
            .apply {
                val response = body<Version.Response>()
                apiVersion = response
            }

    suspend fun login(payload: ApiV1.Auth.Login.Payload) =
        httpClient.post(ApiV1.Auth.Login(), payloadBuilder(payload))
            .apply {
                if (status == HttpStatusCode.OK) {
                    val response = body<ApiV1.Auth.Login.Response>()
                    setTokens(BearerTokens(response.accessToken, response.refreshToken))
                }
            }

    suspend fun register(payload: ApiV1.Auth.Register.Payload) = // TODO: implement verification code
        httpClient.post(ApiV1.Auth.Register(), payloadBuilder(payload))

    suspend fun checkEmail(email: String) =
        httpClient.get(ApiV1.Auth.CheckEmail(email = email))

    suspend fun sendVerificationCode(payload: ApiV1.Auth.SendVerificationCode.Payload) =
        httpClient.post(ApiV1.Auth.SendVerificationCode(), payloadBuilder(payload))

    suspend fun refresh(rotate: Boolean) =
        httpClient.post(ApiV1.Auth.Refresh(rotate = rotate.takeIf { it })) {
            header(HttpHeaders.Authorization, "Bearer ${tokenManager.getRefreshToken()}")
        }.apply {
            if (status == HttpStatusCode.OK) {
                val response = body<ApiV1.Auth.Refresh.Response>()
                setTokens(BearerTokens(response.accessToken, response.refreshToken))
            }
        }

    private fun setTokens(tokens: BearerTokens) {
        tokenManager.setAccessToken(tokens.accessToken)
        tokens.refreshToken?.let { tokenManager.setRefreshToken(it) }
    }

    private inline fun <reified T> payloadBuilder(payload: T): HttpRequestBuilder.() -> Unit = {
        contentType(contentType)
        setBody(payload)
    }

    suspend fun logout() {
        if (!tokenManager.isRefreshTokenExpired()) {
            TODO("Inform the server to invalidate the refresh token")
        }
        tokenManager.clearTokens()
    }

    suspend fun me() = authenticatedClient.get(ApiV1.Users.Me())

    suspend fun getSelectedSchedule() = authenticatedClient.get(ApiV1.Users.Me.SelectedSchedule())

    suspend fun setSelectedSchedule(scheduleId: Short?) =
        authenticatedClient.put(
            ApiV1.Users.Me.SelectedSchedule(),
            payloadBuilder(ApiV1.Users.Me.SelectedSchedule.Payload(scheduleId))
        )

    suspend fun schedules(deleted: Boolean? = null) = authenticatedClient.get(ApiV1.Schedules(deleted = deleted))

    suspend fun getSchedule(scheduleId: Short) =
        authenticatedClient.get(ApiV1.Schedules.ScheduleId(scheduleId = scheduleId))

    suspend fun upsertSchedule(scheduleId: Short, payload: ApiV1.Schedules.ScheduleId.Payload) =
        authenticatedClient.put(
            ApiV1.Schedules.ScheduleId(scheduleId = scheduleId),
            payloadBuilder(payload)
        )

    suspend fun deleteSchedule(scheduleId: Short, permanent: Boolean = false) =
        authenticatedClient.delete(
            ApiV1.Schedules.ScheduleId(
                scheduleId = scheduleId,
                permanent = permanent.takeIf { it }
            )
        )

    suspend fun courses(scheduleId: Short) =
        authenticatedClient.get(
            ApiV1.Schedules.ScheduleId.Courses(
                parent = ApiV1.Schedules.ScheduleId(scheduleId = scheduleId)
            )
        )

    suspend fun getCourse(scheduleId: Short, courseId: Short) =
        authenticatedClient.get(
            ApiV1.Schedules.ScheduleId.Courses.CourseId(
                parent = ApiV1.Schedules.ScheduleId.Courses(
                    parent = ApiV1.Schedules.ScheduleId(scheduleId = scheduleId)
                ),
                courseId = courseId
            )
        )

    suspend fun upsertCourse(
        scheduleId: Short,
        courseId: Short,
        payload: ApiV1.Schedules.ScheduleId.Courses.CourseId.Payload
    ) = authenticatedClient.put(
        ApiV1.Schedules.ScheduleId.Courses.CourseId(
            parent = ApiV1.Schedules.ScheduleId.Courses(
                parent = ApiV1.Schedules.ScheduleId(scheduleId = scheduleId)
            ),
            courseId = courseId
        ),
        payloadBuilder(payload)
    )

    suspend fun deleteCourse(scheduleId: Short, courseId: Short) =
        authenticatedClient.delete(
            ApiV1.Schedules.ScheduleId.Courses.CourseId(
                parent = ApiV1.Schedules.ScheduleId.Courses(
                    parent = ApiV1.Schedules.ScheduleId(scheduleId = scheduleId)
                ),
                courseId = courseId
            )
        )
}