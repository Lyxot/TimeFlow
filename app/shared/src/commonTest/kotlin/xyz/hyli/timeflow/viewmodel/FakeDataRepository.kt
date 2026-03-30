/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.viewmodel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.Settings
import xyz.hyli.timeflow.data.ThemeMode
import xyz.hyli.timeflow.di.IDataRepository
import kotlin.time.Instant

class FakeDataRepository : IDataRepository {
    final override val settings: Flow<Settings>
        field = MutableStateFlow(Settings())

    override suspend fun updateFirstLaunch(versionCode: Int) {
        settings.value = settings.value.copy(firstLaunch = versionCode)
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        settings.value = settings.value.copy(themeMode = themeMode)
    }

    override suspend fun updateThemeDynamicColor(themeDynamicColor: Boolean) {
        settings.value = settings.value.copy(themeDynamicColor = themeDynamicColor)
    }

    override suspend fun updateThemeColor(color: Int) {
        settings.value = settings.value.copy(themeColor = color)
    }

    override suspend fun updateSelectedScheduleID(id: Short) {
        settings.value = settings.value.copy(selectedScheduleID = id)
    }

    override suspend fun updateSelectedScheduleUpdatedAt(updatedAt: Instant?) {
        settings.value = settings.value.copy(selectedScheduleUpdatedAt = updatedAt)
    }

    override suspend fun upsertSchedule(id: Short, schedule: Schedule) {
        val newMap = settings.value.schedules.toMutableMap()
        newMap[id] = schedule
        settings.value = settings.value.copy(schedules = newMap)
    }

    override suspend fun deleteSchedule(id: Short) {
        val newMap = settings.value.schedules.toMutableMap()
        newMap.remove(id)
        settings.value = settings.value.copy(schedules = newMap)
    }

    override suspend fun updateSyncedAt(syncedAt: Instant?) {
        settings.value = settings.value.copy(syncedAt = syncedAt)
    }

    override suspend fun updateApiEndpoint(endpoint: String?) {
        settings.value = settings.value.copy(apiEndpoint = endpoint)
    }

    override suspend fun updateCachedUserInfo(user: xyz.hyli.timeflow.data.User?) {
        settings.value = settings.value.copy(cachedUserInfo = user)
    }

    override suspend fun updateAiConfig(config: xyz.hyli.timeflow.data.AiProviderConfig?) {
        settings.value = settings.value.copy(aiConfig = config)
    }

    override suspend fun resetAll() {
        settings.value = Settings()
    }

    // Helper function for tests to manually set the settings
    fun setSettings(settings: Settings) {
        this.settings.value = settings
    }
}
