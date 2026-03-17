/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.di

import kotlinx.coroutines.flow.Flow
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.Settings
import xyz.hyli.timeflow.data.ThemeMode
import xyz.hyli.timeflow.localstorage.LocalStorageSettingsStore
import kotlin.time.Instant

class DataRepository(
    private val store: LocalStorageSettingsStore,
) : IDataRepository {
    override val settings: Flow<Settings> = store.settings

    override suspend fun updateFirstLaunch(versionCode: Int) {
        store.update { it.copy(firstLaunch = versionCode) }
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        store.update { it.copy(themeMode = themeMode) }
    }

    override suspend fun updateThemeDynamicColor(themeDynamicColor: Boolean) {
        store.update { it.copy(themeDynamicColor = themeDynamicColor) }
    }

    override suspend fun updateThemeColor(color: Int) {
        store.update { it.copy(themeColor = color) }
    }

    override suspend fun updateSelectedScheduleID(id: Short) {
        store.update { it.copy(selectedScheduleID = id) }
    }

    override suspend fun updateSelectedScheduleUpdatedAt(updatedAt: Instant?) {
        store.update { it.copy(selectedScheduleUpdatedAt = updatedAt) }
    }

    override suspend fun upsertSchedule(id: Short, schedule: Schedule) {
        store.update { settings ->
            settings.copy(
                schedules = settings.schedules.toMutableMap().apply { set(id, schedule) }
            )
        }
    }

    override suspend fun deleteSchedule(id: Short) {
        store.update { settings ->
            val updated = settings.schedules.toMutableMap().apply { remove(id) }
            settings.copy(
                schedules = updated,
                selectedScheduleID = if (settings.selectedScheduleID == id) {
                    updated.keys.firstOrNull() ?: -1
                } else {
                    settings.selectedScheduleID
                }
            )
        }
    }

    override suspend fun updateSyncedAt(syncedAt: Instant?) {
        store.update { it.copy(syncedAt = syncedAt) }
    }

    override suspend fun updateTokens(tokens: xyz.hyli.timeflow.data.Tokens?) {
        store.update { it.copy(tokens = tokens) }
    }

    override suspend fun updateApiEndpoint(endpoint: String?) {
        store.update { it.copy(apiEndpoint = endpoint) }
    }

    override suspend fun updateCachedUserInfo(user: xyz.hyli.timeflow.data.User?) {
        store.update { it.copy(cachedUserInfo = user) }
    }
}
