/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.Settings
import xyz.hyli.timeflow.data.ThemeMode

// TODO: Use persistent storage
private val webSettings = MutableStateFlow(Settings())

class DataRepository : IDataRepository {
    override val settings = webSettings.asStateFlow()

    override suspend fun updateFirstLaunch(versionCode: Int) {
        webSettings.tryEmit(
            webSettings.value.copy(
                firstLaunch = versionCode
            )
        )
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        webSettings.tryEmit(
            webSettings.value.copy(
                themeMode = themeMode
            )
        )
    }

    override suspend fun updateThemeDynamicColor(themeDynamicColor: Boolean) {
        webSettings.tryEmit(
            webSettings.value.copy(
                themeDynamicColor = themeDynamicColor
            )
        )
    }

    override suspend fun updateThemeColor(color: Int) {
        webSettings.tryEmit(
            webSettings.value.copy(
                themeColor = color
            )
        )
    }

    override suspend fun updateSelectedSchedule(id: Short) {
        webSettings.tryEmit(
            webSettings.value.copy(
                selectedScheduleID = id
            )
        )
    }

    override suspend fun createSchedule(id: Short, schedule: Schedule) {
        val currentSchedules = webSettings.value.schedules.toMutableMap().apply {
            set(id, schedule)
        }
        webSettings.tryEmit(
            webSettings.value.copy(
                schedules = currentSchedules,
                selectedScheduleID = id
            )
        )
    }

    override suspend fun updateSchedule(id: Short, schedule: Schedule) {
        val currentSchedules = webSettings.value.schedules.toMutableMap().apply {
            set(id, schedule)
        }
        webSettings.tryEmit(
            webSettings.value.copy(
                schedules = currentSchedules
            )
        )
    }

    override suspend fun deleteSchedule(id: Short, permanently: Boolean) {
        val currentSchedules = webSettings.value.schedules.toMutableMap().apply {
            remove(id)
        }
        webSettings.tryEmit(
            webSettings.value.copy(
                schedules = currentSchedules,
                selectedScheduleID = currentSchedules.keys.firstOrNull() ?: -1
            )
        )
    }
}