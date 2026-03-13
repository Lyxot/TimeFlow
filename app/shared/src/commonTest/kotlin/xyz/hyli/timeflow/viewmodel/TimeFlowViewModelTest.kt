/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.Settings
import xyz.hyli.timeflow.data.ThemeMode
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class TimeFlowViewModelTest {

    private lateinit var viewModel: TimeFlowViewModel
    private lateinit var fakeRepository: FakeDataRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeDataRepository()
        viewModel = TimeFlowViewModel(fakeRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial settings are loaded`() = runTest {
        val initialSettings = Settings(themeMode = ThemeMode.LIGHT, themeDynamicColor = true)
        fakeRepository.setSettings(initialSettings)

        val loadedSettings = viewModel.settings.first { it.initialized }

        assertEquals(initialSettings.themeMode, loadedSettings.themeMode)
        assertEquals(initialSettings.themeDynamicColor, loadedSettings.themeDynamicColor)
    }

    @Test
    fun `test updateTheme updates settings`() = runTest {
        val newTheme = ThemeMode.DARK
        viewModel.updateTheme(newTheme)

        val updatedSettings = viewModel.settings.first { it.themeMode == newTheme }
        assertEquals(newTheme, updatedSettings.themeMode)
    }

    @Test
    fun `test createSchedule creates and selects a new schedule`() = runTest {
        val newSchedule = Schedule(name = "New Semester")
        viewModel.createSchedule(newSchedule)

        val updatedSettings = viewModel.settings.first { it.schedules.containsValue(newSchedule) }

        assertEquals(newSchedule, updatedSettings.schedules[updatedSettings.selectedScheduleID])
        assertEquals(1, updatedSettings.schedules.size)
        assertEquals(newSchedule.updatedAt, updatedSettings.selectedScheduleUpdatedAt)
    }

    @Test
    fun `test updateSchedule updates an existing schedule`() = runTest {
        // First, create a schedule to update
        val initialSchedule = Schedule(name = "Initial Schedule")
        viewModel.createSchedule(initialSchedule)
        val initialSettings =
            viewModel.settings.first { it.schedules.containsValue(initialSchedule) }
        val scheduleId = initialSettings.selectedScheduleID

        // Now, update it
        val updatedSchedule = initialSchedule.copy(name = "Updated Name", updatedAt = Clock.System.now())
        viewModel.updateSchedule(scheduleId, updatedSchedule, updatedSchedule.updatedAt)

        val finalSettings =
            viewModel.settings.first { it.schedules[scheduleId]?.name == "Updated Name" }
        assertEquals(updatedSchedule, finalSettings.schedules[scheduleId])
    }

    @Test
    fun `test updateFirstLaunch updates settings`() = runTest {
        val newVersionCode = 114514
        viewModel.updateFirstLaunch(newVersionCode)

        val updatedSettings = viewModel.settings.first { it.firstLaunch == newVersionCode }
        assertEquals(newVersionCode, updatedSettings.firstLaunch)
    }

    @Test
    fun `test updateThemeDynamicColor updates settings`() = runTest {
        val newDynamicColor = true
        viewModel.updateThemeDynamicColor(newDynamicColor)

        val updatedSettings = viewModel.settings.first { it.themeDynamicColor == newDynamicColor }
        assertEquals(newDynamicColor, updatedSettings.themeDynamicColor)
    }

    @Test
    fun `test updateThemeColor updates settings`() = runTest {
        val newColor = 0xFF123456.toInt()
        viewModel.updateThemeColor(newColor)

        val updatedSettings = viewModel.settings.first { it.themeColor == newColor }
        assertEquals(newColor, updatedSettings.themeColor)
    }

    @Test
    fun `test updateSelectedSchedule updates settings`() = runTest {
        val newID: Short = 1145
        val updatedAt = Clock.System.now()
        viewModel.updateSelectedSchedule(newID, updatedAt)

        val updatedSettings = viewModel.settings.first { it.selectedScheduleID == newID }
        assertEquals(newID, updatedSettings.selectedScheduleID)
        assertEquals(updatedAt, updatedSettings.selectedScheduleUpdatedAt)
    }

    @Test
    fun `test updateSyncedAt updates settings`() = runTest {
        val syncedAt = Clock.System.now()
        viewModel.updateSyncedAt(syncedAt)

        val updatedSettings = viewModel.settings.first { it.syncedAt == syncedAt }
        assertEquals(syncedAt, updatedSettings.syncedAt)
    }

    @Test
    fun `test deleteSchedule soft delete marks schedule as deleted`() = runTest {
        // Create a schedule first
        val schedule = Schedule(name = "Test Schedule")
        viewModel.createSchedule(schedule)
        val settingsWithSchedule = viewModel.settings.first { it.schedules.isNotEmpty() }
        val scheduleId = settingsWithSchedule.selectedScheduleID

        // Soft delete it
        viewModel.deleteSchedule(scheduleId, permanently = false)

        val updatedSettings = viewModel.settings.first { it.schedules[scheduleId]?.deleted == true }
        assertEquals(true, updatedSettings.schedules[scheduleId]?.deleted)
        assertEquals(Settings.ZERO_ID, updatedSettings.selectedScheduleID)
    }

    @Test
    fun `test deleteSchedule permanent delete removes schedule`() = runTest {
        // Create a schedule first
        val schedule = Schedule(name = "Test Schedule")
        viewModel.createSchedule(schedule)
        val settingsWithSchedule = viewModel.settings.first { it.schedules.isNotEmpty() }
        val scheduleId = settingsWithSchedule.selectedScheduleID

        // Permanently delete it
        viewModel.deleteSchedule(scheduleId, permanently = true)

        val updatedSettings = viewModel.settings.first { !it.schedules.containsKey(scheduleId) }
        assertEquals(false, updatedSettings.schedules.containsKey(scheduleId))
        assertEquals(Settings.ZERO_ID, updatedSettings.selectedScheduleID)
    }

    @Test
    fun `test deleteSchedule does not change selectedScheduleID if deleting different schedule`() = runTest {
        // Create two schedules
        val schedule1 = Schedule(name = "Schedule 1")
        viewModel.createSchedule(schedule1)
        val settingsWithFirst = viewModel.settings.first { it.schedules.isNotEmpty() }
        val scheduleId1 = settingsWithFirst.selectedScheduleID

        val schedule2 = Schedule(name = "Schedule 2")
        viewModel.createSchedule(schedule2)
        val settingsWithBoth = viewModel.settings.first { it.schedules.size == 2 }
        val scheduleId2 = settingsWithBoth.selectedScheduleID

        // Delete schedule1 (not currently selected)
        viewModel.deleteSchedule(scheduleId1, permanently = true)

        val updatedSettings = viewModel.settings.first { !it.schedules.containsKey(scheduleId1) }
        assertEquals(scheduleId2, updatedSettings.selectedScheduleID) // Should still be scheduleId2
    }
}
