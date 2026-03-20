/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server

import io.ktor.server.config.*

data class AiProviderConfig(
    val name: String,
    /** Format: "openai", "openrouter", "google", "anthropic", "ollama" */
    val format: String,
    val apiKey: String,
    val endpoint: String? = null
)

data class AiModelConfig(
    val id: String,
    val provider: String,
    val weight: Int = 1,
    /** Requests per minute limit, 0 = unlimited */
    val rpm: Int = 0,
    /** Requests per day limit, 0 = unlimited */
    val rpd: Int = 0
)

data class AiConfig(
    val enabled: Boolean,
    val providers: Map<String, AiProviderConfig>,
    val models: List<AiModelConfig>,
    val maxImageSizeBytes: Long,
    val maxImageResolution: Int,
    val quotaPerHalfYear: Int
) {
    companion object {
        fun parse(config: ApplicationConfig): AiConfig {
            val enabled = config.propertyOrNull("ai.enabled")?.getString()?.toBoolean() ?: false
            if (!enabled) return AiConfig(false, emptyMap(), emptyList(),
                config.propertyOrNull("ai.maxImageSizeBytes")?.getString()?.toLongOrNull() ?: 2_097_152L,
                config.propertyOrNull("ai.maxImageResolution")?.getString()?.toIntOrNull() ?: 2048,
                config.propertyOrNull("ai.quotaPerHalfYear")?.getString()?.toIntOrNull() ?: 4
            )

            val providers = parseProviders(config)
            val models = parseModels(config, providers)

            return AiConfig(
                enabled = enabled,
                providers = providers,
                models = models,
                maxImageSizeBytes = config.propertyOrNull("ai.maxImageSizeBytes")?.getString()?.toLongOrNull()
                    ?: 2_097_152L,
                maxImageResolution = config.propertyOrNull("ai.maxImageResolution")?.getString()?.toIntOrNull() ?: 2048,
                quotaPerHalfYear = config.propertyOrNull("ai.quotaPerHalfYear")?.getString()?.toIntOrNull() ?: 4
            )
        }

        private fun parseProviders(config: ApplicationConfig): Map<String, AiProviderConfig> {
            val providers = mutableMapOf<String, AiProviderConfig>()
            val providersConfig = runCatching { config.config("ai.providers") }.getOrNull() ?: return providers

            // keys() may return dotted paths like "nvidia.format", "nvidia.apiKey" — extract unique top-level names
            val names = providersConfig.keys().map { it.substringBefore('.') }.toSet()
            for (name in names) {
                val pConfig = runCatching { providersConfig.config(name) }.getOrNull() ?: continue
                val apiKey = pConfig.propertyOrNull("apiKey")?.getString() ?: ""
                // ollama doesn't require apiKey
                val format = pConfig.propertyOrNull("format")?.getString() ?: "openai"
                if (apiKey.isBlank() && format != "ollama") continue
                providers[name] = AiProviderConfig(
                    name = name,
                    format = format,
                    apiKey = apiKey,
                    endpoint = pConfig.propertyOrNull("endpoint")?.getString()?.takeIf { it.isNotBlank() }
                )
            }
            return providers
        }

        private fun parseModels(
            config: ApplicationConfig,
            providers: Map<String, AiProviderConfig>
        ): List<AiModelConfig> {
            val models = mutableListOf<AiModelConfig>()

            // Try configList first (YAML list syntax)
            val modelConfigs = runCatching { config.configList("ai.models") }.getOrNull()
            if (modelConfigs != null) {
                for (mConfig in modelConfigs) {
                    parseModel(mConfig, providers)?.let { models.add(it) }
                }
                return models
            }

            // Fallback: indexed access (ai.models.0, ai.models.1, ...)
            var i = 0
            while (true) {
                val mConfig = runCatching { config.config("ai.models.$i") }.getOrNull() ?: break
                parseModel(mConfig, providers)?.let { models.add(it) }
                i++
            }
            return models
        }

        private fun parseModel(mConfig: ApplicationConfig, providers: Map<String, AiProviderConfig>): AiModelConfig? {
            val id = mConfig.propertyOrNull("id")?.getString() ?: return null
            val providerName = mConfig.propertyOrNull("provider")?.getString() ?: return null
            if (providerName !in providers) return null
            return AiModelConfig(
                id = id,
                provider = providerName,
                weight = mConfig.propertyOrNull("weight")?.getString()?.toIntOrNull() ?: 1,
                rpm = mConfig.propertyOrNull("rpm")?.getString()?.toIntOrNull() ?: 0,
                rpd = mConfig.propertyOrNull("rpd")?.getString()?.toIntOrNull() ?: 0
            )
        }
    }
}
