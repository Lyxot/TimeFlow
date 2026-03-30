/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.localstorage

import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import xyz.hyli.timeflow.data.Settings

private const val STORAGE_KEY = "timeflow_settings"

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

class LocalStorageSettingsStore {
    val settings: StateFlow<Settings>
        field = MutableStateFlow(load())

    private fun load(): Settings {
        val raw = window.localStorage.getItem(STORAGE_KEY) ?: return Settings()
        return try {
            json.decodeFromString<Settings>(raw)
        } catch (_: Exception) {
            Settings()
        }
    }

    private fun save(settings: Settings) {
        val encoded = json.encodeToString(Settings.serializer(), settings)
        window.localStorage.setItem(STORAGE_KEY, encoded)
    }

    fun update(transform: (Settings) -> Settings) {
        val newSettings = transform(settings.value)
        settings.value = newSettings
        save(newSettings)
    }
}
