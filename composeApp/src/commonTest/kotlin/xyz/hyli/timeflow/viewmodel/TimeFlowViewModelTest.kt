package xyz.hyli.timeflow.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import xyz.hyli.timeflow.datastore.Schedule
import xyz.hyli.timeflow.datastore.Settings
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
        val initialSettings = Settings(theme = 1, themeDynamicColor = true)
        fakeRepository.setSettings(initialSettings)

        val loadedSettings = viewModel.settings.first { it.initialized }

        assertEquals(initialSettings.theme, loadedSettings.theme)
        assertEquals(initialSettings.themeDynamicColor, loadedSettings.themeDynamicColor)
    }

    @Test
    fun `test updateTheme updates settings`() = runTest {
        val newTheme = 2
        viewModel.updateTheme(newTheme)

        val updatedSettings = viewModel.settings.first { it.theme == newTheme }
        assertEquals(newTheme, updatedSettings.theme)
    }

    @Test
    fun `test createSchedule creates and selects a new schedule`() = runTest {
        val newSchedule = Schedule(name = "New Semester")
        viewModel.createSchedule(newSchedule)

        val updatedSettings = viewModel.settings.first { it.schedule.containsValue(newSchedule) }

        assertEquals(newSchedule, updatedSettings.schedule[updatedSettings.selectedSchedule])
        assertEquals(1, updatedSettings.schedule.size)
    }

    @Test
    fun `test updateSchedule updates an existing schedule`() = runTest {
        // First, create a schedule to update
        val initialSchedule = Schedule(name = "Initial Schedule")
        viewModel.createSchedule(initialSchedule)
        val initialSettings =
            viewModel.settings.first { it.schedule.containsValue(initialSchedule) }
        val scheduleId = initialSettings.selectedSchedule

        // Now, update it
        val updatedSchedule = initialSchedule.copy(name = "Updated Name")
        viewModel.updateSchedule(scheduleId, updatedSchedule)

        val finalSettings =
            viewModel.settings.first { it.schedule[scheduleId]?.name == "Updated Name" }
        assertEquals(updatedSchedule, finalSettings.schedule[scheduleId])
    }

    @Test
    fun `test updateFirstLaunch updates settings`() = runTest {
        val newVersionCode = 123
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
        val newUuid = "new-uuid"
        viewModel.updateSelectedSchedule(newUuid)

        val updatedSettings = viewModel.settings.first { it.selectedSchedule == newUuid }
        assertEquals(newUuid, updatedSettings.selectedSchedule)
    }
}
