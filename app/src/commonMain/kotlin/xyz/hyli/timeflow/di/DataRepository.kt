/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.di

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.Settings
import xyz.hyli.timeflow.data.ThemeMode
import xyz.hyli.timeflow.datastore.SettingsDataStore

interface IDataRepository {
    val settings: Flow<Settings>
    suspend fun updateFirstLaunch(versionCode: Int)
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updateThemeDynamicColor(themeDynamicColor: Boolean)
    suspend fun updateThemeColor(color: Int)
    suspend fun updateSelectedSchedule(id: Short)
    suspend fun createSchedule(id: Short, schedule: Schedule)
    suspend fun updateSchedule(id: Short, schedule: Schedule)
    suspend fun deleteSchedule(id: Short, permanently: Boolean)
}

class DataRepository(
    private val settingsDataStore: SettingsDataStore,
) : IDataRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val settings: Flow<Settings> = settingsDataStore.settings
    override suspend fun updateFirstLaunch(versionCode: Int) {
        settingsDataStore.updateFirstLaunch(versionCode)
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        settingsDataStore.updateTheme(themeMode)
    }

    override suspend fun updateThemeDynamicColor(themeDynamicColor: Boolean) {
        settingsDataStore.updateThemeDynamicColor(themeDynamicColor)
    }

    override suspend fun updateThemeColor(color: Int) {
        settingsDataStore.updateThemeColor(color)
    }

    override suspend fun updateSelectedSchedule(id: Short) {
        settingsDataStore.updateSelectedSchedule(id)
    }

    override suspend fun createSchedule(id: Short, schedule: Schedule) {
        settingsDataStore.createSchedule(id, schedule)
    }

    override suspend fun updateSchedule(id: Short, schedule: Schedule) {
        settingsDataStore.updateSchedule(id, schedule)
    }

    override suspend fun deleteSchedule(id: Short, permanently: Boolean) {
        settingsDataStore.deleteSchedule(id, permanently)
    }
}