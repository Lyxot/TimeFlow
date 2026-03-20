/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import xyz.hyli.timeflow.ai.ScheduleExtractor
import xyz.hyli.timeflow.ai.resizeImage
import xyz.hyli.timeflow.api.models.ApiV1
import xyz.hyli.timeflow.server.AiConfig
import xyz.hyli.timeflow.server.ModelSelector
import xyz.hyli.timeflow.server.database.DataRepository
import xyz.hyli.timeflow.server.utils.authedGet
import xyz.hyli.timeflow.server.utils.authedPost
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalEncodingApi::class)
fun Route.aiRoutes(aiConfig: AiConfig, modelSelector: ModelSelector, repository: DataRepository, log: Logger) {
    if (!aiConfig.enabled) {
        log.info("AI schedule extraction is disabled")
        return
    }

    if (aiConfig.models.isEmpty()) {
        log.warn("AI schedule extraction is enabled but no models are configured")
        return
    }

    log.info(
        "AI schedule extraction enabled with {} provider(s), {} model(s): {}",
        aiConfig.providers.size,
        aiConfig.models.size,
        aiConfig.models.joinToString { "${it.id} (${it.provider})" }
    )

    val quotaWindow = 182.days

    authenticate("access-auth") {
        authedGet<ApiV1.Ai.Info>(repository) { _, user ->
            val since = Clock.System.now() - quotaWindow
            val used = repository.countAiUsage(user.id, since)
            val unlimited = repository.isAiUnlimited(user.id)

            val refreshAt = if (!unlimited && used >= aiConfig.quotaPerHalfYear) {
                repository.earliestAiUsage(user.id, since)?.let { it + quotaWindow }
            } else null

            call.respond(
                HttpStatusCode.OK,
                ApiV1.Ai.Info.Response(
                    enabled = true,
                    quotaUsed = used,
                    quotaLimit = if (unlimited) null else aiConfig.quotaPerHalfYear,
                    quotaRefreshAt = refreshAt,
                    maxImageSizeBytes = aiConfig.maxImageSizeBytes,
                    maxImageResolution = aiConfig.maxImageResolution
                )
            )
        }

        rateLimit(RateLimitName("ai")) {
            authedPost<ApiV1.Ai.ExtractSchedule>(repository) { _, user ->
                // 1. Check quota
                val since = Clock.System.now() - quotaWindow
                val used = repository.countAiUsage(user.id, since)
                val unlimited = repository.isAiUnlimited(user.id)
                if (!unlimited && used >= aiConfig.quotaPerHalfYear) {
                    call.respond(
                        HttpStatusCode.TooManyRequests,
                        mapOf(
                            "error" to "AI extraction quota exceeded",
                            "used" to used,
                            "limit" to aiConfig.quotaPerHalfYear
                        )
                    )
                    return@authedPost
                }

                // 2. Receive and validate image
                val payload = call.receive<ApiV1.Ai.ExtractSchedule.Payload>()
                val imageBytes = try {
                    Base64.decode(payload.image)
                } catch (_: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid base64 image data"))
                    return@authedPost
                }

                if (imageBytes.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Empty image data"))
                    return@authedPost
                }

                // 3. Resize image if needed
                val resized = resizeImage(imageBytes, aiConfig.maxImageSizeBytes, aiConfig.maxImageResolution)
                val imageBase64 = if (resized.wasResized) Base64.encode(resized.data) else payload.image

                // 4. Set quota headers
                call.response.header("X-Ai-Quota-Used", (used + 1).toString())
                call.response.header(
                    "X-Ai-Quota-Limit",
                    if (unlimited) "unlimited" else aiConfig.quotaPerHalfYear.toString()
                )

                // 5. Select models and try with fallback
                val candidates = modelSelector.selectModels()
                if (candidates.isEmpty()) {
                    call.respond(
                        HttpStatusCode.ServiceUnavailable,
                        mapOf("error" to "All AI models are currently rate-limited, try again later")
                    )
                    return@authedPost
                }

                val errors = mutableListOf<Pair<String, Exception>>()

                for ((model, provider) in candidates) {
                    val extractor = ScheduleExtractor(
                        provider = provider.format,
                        apiKey = provider.apiKey,
                        model = model.id,
                        endpoint = provider.endpoint
                    )

                    try {
                        call.response.header("X-Ai-Model", model.id)
                        if (payload.stream) {
                            call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                                extractor.extractStreaming(imageBase64, resized.format).collect { chunk ->
                                    withContext(Dispatchers.IO) {
                                        write("data: $chunk\n\n")
                                        flush()
                                    }
                                }
                                withContext(Dispatchers.IO) {
                                    write("data: [DONE]\n\n")
                                    flush()
                                }
                            }
                        } else {
                            val result = extractor.extractFull(imageBase64, resized.format)
                            call.respond(HttpStatusCode.OK, result.toSchedule())
                        }

                        // Success — record usage and rate limit hit
                        modelSelector.recordRequest(model.id)
                        repository.recordAiUsage(user.id)
                        return@authedPost
                    } catch (e: Exception) {
                        log.warn("AI model '{}' failed, trying next: {}", model.id, e.message)
                        errors.add(model.id to e)
                    } finally {
                        extractor.close()
                    }
                }

                // All models failed
                log.error(
                    "All AI models failed for user {}: {}",
                    user.id,
                    errors.joinToString { "${it.first}: ${it.second.message}" }
                )
                if (!call.response.isCommitted) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "All AI models failed: ${errors.last().second.message}")
                    )
                }
            }
        }
    }
}
