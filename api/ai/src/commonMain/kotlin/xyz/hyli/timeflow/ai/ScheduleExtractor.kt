/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ai

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.*
import xyz.hyli.timeflow.api.models.ExtractionResult
import xyz.hyli.timeflow.api.models.detectImageFormatFromBase64
import xyz.hyli.timeflow.api.models.parseExtractionResult
import xyz.hyli.timeflow.client.HttpEngine

/**
 * 使用 Ktor HTTP 客户端直接调用 LLM API，从课程表图片中提取课程信息。
 * 支持 OpenAI、OpenRouter、Google、Anthropic、Mistral、Ollama 及任何 OpenAI 兼容 API。
 *
 * @param provider 协议格式: "openai", "openrouter", "google", "anthropic", "ollama"
 * @param apiKey API 密钥（ollama 不需要）
 * @param model 模型 ID
 * @param endpoint 自定义端点 URL（可选，每个 provider 都支持）
 * @param httpClient 可选的共享 HttpClient，未提供时会自动创建
 */
class ScheduleExtractor(
    private val provider: String,
    private val apiKey: String,
    private val model: String,
    private val endpoint: String? = null,
    httpClient: HttpClient? = null
) : AutoCloseable {

    private val lazyClient = lazy {
        // Derive from the provided client's engine with timeout config.
        // config {} sets manageEngine=false, so closing this won't affect the original.
        httpClient?.config {
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
                socketTimeoutMillis = REQUEST_TIMEOUT_MS
            }
            }
            ?: HttpClient(HttpEngine) {
                install(HttpTimeout) {
                    requestTimeoutMillis = REQUEST_TIMEOUT_MS
                    socketTimeoutMillis = REQUEST_TIMEOUT_MS
                }
            }
    }
    private val client: HttpClient by lazyClient
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 从图片中提取完整信息（非流式），包括课程表元数据和课程列表。
     * @param imageBase64 Base64 编码的图片数据
     * @param format 图片格式，如 "png"、"jpeg"，默认自动检测
     * @return 提取结果，包含课程表信息和课程列表
     */
    suspend fun extractFull(imageBase64: String, format: String? = null): ExtractionResult {
        val fmt = format ?: detectImageFormatFromBase64(imageBase64)
        val text = when (provider.lowercase()) {
            "anthropic" -> requestAnthropic(imageBase64, fmt)
            "google" -> requestGoogle(imageBase64, fmt)
            else -> requestOpenAICompatible(imageBase64, fmt)
        }
        return parseResult(text)
    }

    /**
     * 从图片中提取课程信息（流式），返回 LLM 的文本增量。
     * @param imageBase64 Base64 编码的图片数据
     * @param format 图片格式，如 "png"、"jpeg"，默认自动检测
     * @return 文本增量的 Flow
     */
    fun extractStreaming(imageBase64: String, format: String? = null): Flow<String> {
        val fmt = format ?: detectImageFormatFromBase64(imageBase64)
        return when (provider.lowercase()) {
            "anthropic" -> streamAnthropic(imageBase64, fmt)
            "google" -> streamGoogle(imageBase64, fmt)
            else -> streamOpenAICompatible(imageBase64, fmt)
        }
    }

    /**
     * 解析 JSONL 格式的 LLM 输出为完整结果，包含课程表信息和课程列表。
     */
    fun parseResult(text: String): ExtractionResult = parseExtractionResult(text)

    override fun close() {
        if (lazyClient.isInitialized()) client.close()
    }

    // ---- OpenAI-compatible (OpenAI, OpenRouter, Mistral, Ollama, custom) ----

    private fun openAICompatibleUrl(): String {
        endpoint?.takeIf { it.isNotBlank() }?.let { return it.trimEnd('/') }
        return when (provider.lowercase()) {
            "openrouter" -> OPENROUTER_URL
            "ollama" -> OLLAMA_URL
            else -> OPENAI_URL
        }
    }

    private fun openAICompatibleBody(imageBase64: String, format: String, stream: Boolean) =
        buildJsonObject {
            put("model", model)
            put("stream", stream)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "system")
                    put("content", ScheduleExtractorPrompt.SYSTEM_PROMPT)
                }
                addJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        addJsonObject {
                            put("type", "image_url")
                            putJsonObject("image_url") {
                                put("url", "data:image/$format;base64,$imageBase64")
                            }
                        }
                    }
                }
            }
        }.toString()

    private suspend fun requestOpenAICompatible(imageBase64: String, format: String): String {
        val response = client.post(openAICompatibleUrl()) {
            if (apiKey.isNotBlank()) header(HttpHeaders.Authorization, "Bearer $apiKey")
            setBody(
                TextContent(
                    openAICompatibleBody(imageBase64, format, stream = false),
                    ContentType.Application.Json
                )
            )
        }
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        checkError(body, "API")
        return body["choices"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("message")?.jsonObject
            ?.get("content")?.jsonPrimitive?.content
            ?: error("No content in response")
    }

    private fun streamOpenAICompatible(imageBase64: String, format: String): Flow<String> = channelFlow {
        client.preparePost(openAICompatibleUrl()) {
            if (apiKey.isNotBlank()) header(HttpHeaders.Authorization, "Bearer $apiKey")
            setBody(TextContent(openAICompatibleBody(imageBase64, format, stream = true), ContentType.Application.Json))
        }.execute { response ->
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readLine() ?: break
                if (!line.startsWith("data: ")) continue
                val data = line.removePrefix("data: ").trim()
                if (data == "[DONE]") break
                val text = runCatching {
                    json.parseToJsonElement(data).jsonObject["choices"]?.jsonArray
                        ?.firstOrNull()?.jsonObject
                        ?.get("delta")?.jsonObject
                        ?.get("content")?.jsonPrimitive?.content
                }.getOrNull()
                if (text != null) send(text)
            }
        }
    }

    // ---- Anthropic ----

    private fun anthropicUrl(): String {
        endpoint?.takeIf { it.isNotBlank() }?.let { return it.trimEnd('/') }
        return ANTHROPIC_URL
    }

    private fun anthropicBody(imageBase64: String, format: String, stream: Boolean) =
        buildJsonObject {
            put("model", model)
            put("max_tokens", 8192)
            put("stream", stream)
            put("system", ScheduleExtractorPrompt.SYSTEM_PROMPT)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        addJsonObject {
                            put("type", "image")
                            putJsonObject("source") {
                                put("type", "base64")
                                put("media_type", "image/$format")
                                put("data", imageBase64)
                            }
                        }
                    }
                }
            }
        }.toString()

    private suspend fun requestAnthropic(imageBase64: String, format: String): String {
        val response = client.post(anthropicUrl()) {
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            setBody(TextContent(anthropicBody(imageBase64, format, stream = false), ContentType.Application.Json))
        }
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        checkError(body, "Anthropic")
        return body["content"]?.jsonArray
            ?.filter { it.jsonObject["type"]?.jsonPrimitive?.content == "text" }
            ?.joinToString("") { it.jsonObject["text"]!!.jsonPrimitive.content }
            ?: error("No content in response")
    }

    private fun streamAnthropic(imageBase64: String, format: String): Flow<String> = channelFlow {
        client.preparePost(anthropicUrl()) {
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            setBody(TextContent(anthropicBody(imageBase64, format, stream = true), ContentType.Application.Json))
        }.execute { response ->
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readLine() ?: break
                if (!line.startsWith("data: ")) continue
                val data = line.removePrefix("data: ").trim()
                val text = runCatching {
                    val obj = json.parseToJsonElement(data).jsonObject
                    if (obj["type"]?.jsonPrimitive?.content == "content_block_delta") {
                        obj["delta"]?.jsonObject?.get("text")?.jsonPrimitive?.content
                    } else null
                }.getOrNull()
                if (text != null) send(text)
            }
        }
    }

    // ---- Google Gemini ----

    private fun googleUrl(): String {
        endpoint?.takeIf { it.isNotBlank() }?.let { return it.trimEnd('/') }
        return GOOGLE_URL
    }

    private fun googleBody(imageBase64: String, format: String) =
        buildJsonObject {
            putJsonObject("system_instruction") {
                putJsonArray("parts") {
                    addJsonObject { put("text", ScheduleExtractorPrompt.SYSTEM_PROMPT) }
                }
            }
            putJsonArray("contents") {
                addJsonObject {
                    putJsonArray("parts") {
                        addJsonObject {
                            putJsonObject("inline_data") {
                                put("mime_type", "image/$format")
                                put("data", imageBase64)
                            }
                        }
                    }
                }
            }
        }.toString()

    private suspend fun requestGoogle(imageBase64: String, format: String): String {
        val response = client.post("${googleUrl()}/$model:generateContent?key=$apiKey") {
            setBody(TextContent(googleBody(imageBase64, format), ContentType.Application.Json))
        }
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        checkError(body, "Google")
        return body["candidates"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("content")?.jsonObject
            ?.get("parts")?.jsonArray
            ?.filter { it.jsonObject.containsKey("text") }
            ?.joinToString("") { it.jsonObject["text"]!!.jsonPrimitive.content }
            ?: error("No content in response")
    }

    private fun streamGoogle(imageBase64: String, format: String): Flow<String> = channelFlow {
        val baseUrl = googleUrl()
        client.preparePost("$baseUrl/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey") {
            setBody(TextContent(googleBody(imageBase64, format), ContentType.Application.Json))
        }.execute { response ->
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readLine() ?: break
                if (!line.startsWith("data: ")) continue
                val data = line.removePrefix("data: ").trim()
                val text = runCatching {
                    json.parseToJsonElement(data).jsonObject["candidates"]?.jsonArray
                        ?.firstOrNull()?.jsonObject
                        ?.get("content")?.jsonObject
                        ?.get("parts")?.jsonArray
                        ?.filter { it.jsonObject.containsKey("text") }
                        ?.joinToString("") { it.jsonObject["text"]!!.jsonPrimitive.content }
                }.getOrNull()
                if (!text.isNullOrEmpty()) send(text)
            }
        }
    }

    // ---- Helpers ----

    private fun checkError(body: JsonObject, provider: String) {
        body["error"]?.let { err ->
            val msg = when (err) {
                is JsonObject -> err["message"]?.jsonPrimitive?.content ?: err.toString()
                else -> err.jsonPrimitive.content
            }
            error("$provider error: $msg")
        }
    }

    companion object {
        // 10 minutes timeout
        private const val REQUEST_TIMEOUT_MS = 600_000L
        const val OPENAI_URL = "https://api.openai.com/v1/chat/completions"
        const val OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
        const val OLLAMA_URL = "http://localhost:11434/v1/chat/completions"
        const val ANTHROPIC_URL = "https://api.anthropic.com/v1/messages"
        const val GOOGLE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    }
}
