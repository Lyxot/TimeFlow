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
import io.ktor.server.config.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import xyz.hyli.timeflow.ai.ScheduleExtractor
import xyz.hyli.timeflow.api.models.ApiV1
import xyz.hyli.timeflow.server.database.DataRepository
import xyz.hyli.timeflow.server.utils.authedPost
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.sqrt
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalEncodingApi::class)
fun Route.aiRoutes(config: ApplicationConfig, repository: DataRepository, log: Logger) {
    val enabled = config.property("ai.enabled").getString().toBoolean()
    if (!enabled) {
        log.info("AI schedule extraction is disabled")
        return
    }

    val provider = config.property("ai.provider").getString()
    val apiKey = config.property("ai.apiKey").getString()
    val model = config.property("ai.model").getString()
    val endpoint = config.property("ai.endpoint").getString().takeIf { it.isNotBlank() }
    val maxImageSizeBytes = config.property("ai.maxImageSizeBytes").getString().toLong()
    val maxImageResolution = config.property("ai.maxImageResolution").getString().toInt()
    val quotaPerHalfYear = config.property("ai.quotaPerHalfYear").getString().toInt()

    if (apiKey.isBlank()) {
        log.warn("AI schedule extraction is enabled but no API key is configured")
        return
    }

    log.info("AI schedule extraction enabled with provider '{}', model '{}'", provider, model)

    authenticate("access-auth") {
        rateLimit(RateLimitName("ai")) {
            authedPost<ApiV1.Ai.ExtractSchedule>(repository) { _, user ->
                // 1. Check quota
                val since = Clock.System.now() - 182.days
                val used = repository.countAiUsage(user.id, since)
                val unlimited = repository.isAiUnlimited(user.id)
                if (!unlimited && used >= quotaPerHalfYear) {
                    call.respond(
                        HttpStatusCode.TooManyRequests,
                        mapOf(
                            "error" to "AI extraction quota exceeded",
                            "used" to used,
                            "limit" to quotaPerHalfYear
                        )
                    )
                    return@authedPost
                }

                // 2. Receive and validate image
                val payload = call.receive<ApiV1.Ai.ExtractSchedule.Payload>()
                val imageBytes = try {
                    Base64.decode(payload.image)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid base64 image data"))
                    return@authedPost
                }

                if (imageBytes.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Empty image data"))
                    return@authedPost
                }

                // 3. Resize image if needed (by file size or resolution)
                val resized = resizeImage(imageBytes, maxImageSizeBytes, maxImageResolution)
                val wasResized = resized !== imageBytes
                val imageBase64 = if (wasResized) Base64.encode(resized) else payload.image
                val imageFormat = if (wasResized) "jpeg" else null // resized images are always JPEG

                // 4. Set quota headers before sending the response
                call.response.header("X-Ai-Quota-Used", (used + 1).toString())
                call.response.header(
                    "X-Ai-Quota-Limit",
                    if (unlimited) "unlimited" else quotaPerHalfYear.toString()
                )

                // 5. Call LLM
                val extractor = ScheduleExtractor(
                    provider = provider,
                    apiKey = apiKey,
                    model = model,
                    endpoint = endpoint
                )

                try {
                    if (payload.stream) {
                        call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                            extractor.extractStreaming(imageBase64, imageFormat).collect { chunk ->
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
                        val courses = extractor.extract(imageBase64, imageFormat)
                        call.respond(HttpStatusCode.OK, courses)
                    }

                    // 6. Record usage only on success
                    repository.recordAiUsage(user.id)
                } catch (e: Exception) {
                    log.error("AI extraction failed for user {}", user.id, e)
                    if (!call.response.isCommitted) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "AI extraction failed: ${e.message}")
                        )
                    }
                } finally {
                    extractor.close()
                }
            }
        }
    }
}

/**
 * Resize an image if it exceeds the byte size limit or max resolution.
 * Returns the original byte array (same reference) if no resize is needed.
 */
private fun resizeImage(imageBytes: ByteArray, maxSizeBytes: Long, maxResolution: Int): ByteArray {
    val originalImage = ImageIO.read(ByteArrayInputStream(imageBytes)) ?: return imageBytes

    val w = originalImage.width
    val h = originalImage.height
    val maxDim = maxOf(w, h)

    val resolutionScale = if (maxDim > maxResolution) maxResolution.toDouble() / maxDim else 1.0
    val sizeScale = if (imageBytes.size > maxSizeBytes) {
        sqrt(maxSizeBytes.toDouble() / imageBytes.size)
    } else 1.0

    val scale = minOf(resolutionScale, sizeScale)
    if (scale >= 1.0) return imageBytes

    val newWidth = (w * scale).toInt().coerceAtLeast(1)
    val newHeight = (h * scale).toInt().coerceAtLeast(1)

    val resized = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
    val g2d = resized.createGraphics()
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null)
    g2d.dispose()

    val outputStream = ByteArrayOutputStream()
    ImageIO.write(resized, "jpeg", outputStream)
    return outputStream.toByteArray()
}
