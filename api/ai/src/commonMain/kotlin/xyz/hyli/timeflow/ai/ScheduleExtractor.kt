/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ai

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.AttachmentContent
import ai.koog.prompt.message.ContentPart
import ai.koog.prompt.streaming.StreamFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * 使用 Koog 从课程表图片中提取课程信息。
 * 支持 OpenAI、Google、OpenRouter、Anthropic 及任何 OpenAI 兼容 API。
 */
class ScheduleExtractor(
    private val provider: String,
    private val apiKey: String,
    private val model: String,
    private val endpoint: String? = null
) : AutoCloseable {

    private val executor: SingleLLMPromptExecutor = createExecutor()
    private val json = Json { ignoreUnknownKeys = true }

    private fun createExecutor(): SingleLLMPromptExecutor {
        // If a custom endpoint is provided, use OpenAI-compatible client with that base URL
        if (!endpoint.isNullOrBlank()) {
            // Parse endpoint: e.g. "https://integrate.api.nvidia.com/v1/chat/completions"
            // Extract base URL and chat completions path
            val (baseUrl, chatPath) = parseEndpoint(endpoint)
            val settings = OpenAIClientSettings(baseUrl, chatCompletionsPath = chatPath)
            return SingleLLMPromptExecutor(OpenAILLMClient(apiKey, settings))
        }

        return when (provider.lowercase()) {
            "openrouter" -> simpleOpenRouterExecutor(apiKey)
            "google" -> simpleGoogleAIExecutor(apiKey)
            "anthropic" -> simpleAnthropicExecutor(apiKey)
            else -> simpleOpenAIExecutor(apiKey)
        }
    }

    private fun resolveModel(): LLModel {
        val llmProvider = when {
            !endpoint.isNullOrBlank() -> LLMProvider.OpenAI // custom endpoint uses OpenAI-compatible protocol
            provider.lowercase() == "openrouter" -> LLMProvider.OpenRouter
            provider.lowercase() == "google" -> LLMProvider.Google
            provider.lowercase() == "anthropic" -> LLMProvider.Anthropic
            else -> LLMProvider.OpenAI
        }
        return LLModel(
            provider = llmProvider,
            id = model,
            capabilities = listOf(
                LLMCapability.Vision.Image,
                LLMCapability.Completion,
                LLMCapability.OpenAIEndpoint.Completions
            ),
        )
    }

    private fun buildPrompt(imageBase64: String, format: String = "png") = prompt("schedule-extraction") {
        system(ScheduleExtractorPrompt.SYSTEM_PROMPT)
        user {
            image(
                ContentPart.Image(
                    content = AttachmentContent.Binary.Base64(imageBase64),
                    format = format,
                    fileName = "schedule.$format"
                )
            )
        }
    }

    /**
     * 从图片中提取课程信息（非流式）。
     * @param imageBase64 Base64 编码的图片数据
     * @param format 图片格式，如 "png"、"jpeg"，默认自动检测
     * @return 提取出的课程列表
     */
    suspend fun extract(imageBase64: String, format: String? = null): List<ExtractedCourse> {
        val prompt = buildPrompt(imageBase64, format ?: detectImageFormatFromBase64(imageBase64))
        val llmModel = resolveModel()
        val responses = executor.execute(prompt, llmModel, emptyList())
        val text = responses.joinToString("") { it.content }
        return parseJsonLines(text)
    }

    /**
     * 从图片中提取课程信息（流式），返回 LLM 的文本增量。
     * @param imageBase64 Base64 编码的图片数据
     * @param format 图片格式，如 "png"、"jpeg"，默认自动检测
     * @return 文本增量的 Flow
     */
    fun extractStreaming(imageBase64: String, format: String? = null): Flow<String> {
        val prompt = buildPrompt(imageBase64, format ?: detectImageFormatFromBase64(imageBase64))
        val llmModel = resolveModel()
        return executor.executeStreaming(prompt, llmModel, emptyList())
            .filterIsInstance<StreamFrame.TextDelta>()
            .map { it.text }
    }

    /**
     * 解析 JSONL 格式的 LLM 输出为 ExtractedCourse 列表。
     */
    fun parseJsonLines(text: String): List<ExtractedCourse> {
        return text.lines()
            .map { it.trim() }
            .filter { it.startsWith("{") && it.endsWith("}") }
            .mapNotNull { line ->
                runCatching {
                    json.decodeFromString(ExtractedCourse.serializer(), line)
                }.getOrNull()
            }
    }

    override fun close() {
        executor.close()
    }

    companion object {
        /**
         * Parse a full endpoint URL into (baseUrl, chatCompletionsPath).
         * e.g. "https://integrate.api.nvidia.com/v1/chat/completions"
         *   -> ("https://integrate.api.nvidia.com", "v1/chat/completions")
         */
        internal fun parseEndpoint(endpoint: String): Pair<String, String> {
            val url = endpoint.trimEnd('/')
            // Find the path after the host
            val protocolEnd = url.indexOf("://")
            if (protocolEnd == -1) {
                return url to "v1/chat/completions"
            }
            val pathStart = url.indexOf('/', protocolEnd + 3)
            if (pathStart == -1) {
                return url to "v1/chat/completions"
            }
            val baseUrl = url.substring(0, pathStart)
            val path = url.substring(pathStart + 1)
            return baseUrl to path
        }
    }
}
