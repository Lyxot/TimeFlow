/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server

import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Selects models for AI extraction based on weighted random selection and rate limits.
 * Thread-safe for concurrent use.
 */
class ModelSelector(
    private val providers: Map<String, AiProviderConfig>,
    private val models: List<AiModelConfig>
) {
    private val requestLog = ConcurrentHashMap<String, MutableList<Instant>>()

    /**
     * Returns models ordered by weighted selection, filtered by rate limits.
     * Higher-weight models appear first (with randomness). Rate-limited models are excluded.
     * Returns empty list if all models are rate-limited.
     */
    fun selectModels(): List<Pair<AiModelConfig, AiProviderConfig>> {
        val now = Clock.System.now()
        cleanup(now)

        return models
            .filter { isWithinLimits(it, now) }
            .weightedShuffle()
            .mapNotNull { model ->
                providers[model.provider]?.let { model to it }
            }
    }

    /**
     * Record a successful request for rate limit tracking.
     */
    fun recordRequest(modelId: String) {
        requestLog.getOrPut(modelId) { mutableListOf() }.add(Clock.System.now())
    }

    private fun isWithinLimits(model: AiModelConfig, now: Instant): Boolean {
        val log = requestLog[model.id] ?: return true

        if (model.rpm > 0) {
            val minuteAgo = now - 1.minutes
            val minuteCount = synchronized(log) { log.count { it > minuteAgo } }
            if (minuteCount >= model.rpm) return false
        }

        if (model.rpd > 0) {
            val dayAgo = now - 1.days
            val dayCount = synchronized(log) { log.count { it > dayAgo } }
            if (dayCount >= model.rpd) return false
        }

        return true
    }

    private fun cleanup(now: Instant) {
        val dayAgo = now - 1.days
        requestLog.forEach { (_, log) ->
            synchronized(log) { log.removeAll { it < dayAgo } }
        }
    }
}

/**
 * Weighted shuffle: higher weight = more likely to appear earlier.
 * Uses reservoir-like sampling: each item gets a random score scaled by weight.
 */
private fun List<AiModelConfig>.weightedShuffle(): List<AiModelConfig> {
    if (size <= 1) return this
    return sortedByDescending { model ->
        Math.random() * model.weight
    }
}
