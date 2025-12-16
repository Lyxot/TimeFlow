/*
 * Copyright (c) 2025 Lyxot and contributors.
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
import xyz.hyli.timeflow.di.IDataRepository

class FakeDataRepository : IDataRepository {
    private val _settings = MutableStateFlow(Settings())
    override val settings: Flow<Settings> = _settings

    override suspend fun updateFirstLaunch(versionCode: Int) {
        _settings.value = _settings.value.copy(firstLaunch = versionCode)
    }

    override suspend fun updateTheme(theme: Int) {
        _settings.value = _settings.value.copy(theme = theme)
    }

    override suspend fun updateThemeDynamicColor(themeDynamicColor: Boolean) {
        _settings.value = _settings.value.copy(themeDynamicColor = themeDynamicColor)
    }

    override suspend fun updateThemeColor(color: Int) {
        _settings.value = _settings.value.copy(themeColor = color)
    }

    override suspend fun updateSelectedSchedule(uuid: String) {
        _settings.value = _settings.value.copy(selectedSchedule = uuid)
    }

    override suspend fun createSchedule(uuid: String, schedule: Schedule) {
        val newMap = _settings.value.schedule.toMutableMap()
        newMap[uuid] = schedule
        _settings.value = _settings.value.copy(schedule = newMap)
    }

    override suspend fun updateSchedule(uuid: String, schedule: Schedule) {
        val newMap = _settings.value.schedule.toMutableMap()
        newMap[uuid] = schedule
        _settings.value = _settings.value.copy(schedule = newMap)
    }

    // Helper function for tests to manually set the settings
    fun setSettings(settings: Settings) {
        _settings.value = settings
    }
}
