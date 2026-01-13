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
import kotlin.time.Instant

interface IDataRepository {
    val settings: Flow<Settings>
    suspend fun updateFirstLaunch(versionCode: Int)
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updateThemeDynamicColor(themeDynamicColor: Boolean)
    suspend fun updateThemeColor(color: Int)
    suspend fun updateSelectedScheduleID(id: Short)
    suspend fun updateSelectedScheduleUpdatedAt(updatedAt: Instant?)
    suspend fun upsertSchedule(id: Short, schedule: Schedule)
    suspend fun deleteSchedule(id: Short)
    suspend fun updateSyncedAt(syncedAt: Instant?)
}
