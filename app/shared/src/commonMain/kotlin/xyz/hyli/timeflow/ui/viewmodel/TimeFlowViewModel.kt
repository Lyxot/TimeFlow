/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import org.jetbrains.compose.resources.getString
import xyz.hyli.timeflow.data.*
import xyz.hyli.timeflow.di.IAppContainer
import xyz.hyli.timeflow.di.IDataRepository
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.utils.writeBytesToFile
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isMobile
import kotlin.uuid.ExperimentalUuidApi

class TimeFlowViewModel(
    private val repository: IDataRepository
) : ViewModel() {
    val settings: StateFlow<Settings> =
        repository.settings
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = Settings(initialized = false)
            )

    val selectedSchedule: StateFlow<Schedule?> =
        settings.map { settings -> settings.selectedSchedule }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = null
            )

    fun updateFirstLaunch(versionCode: Int) {
        viewModelScope.launch {
            repository.updateFirstLaunch(versionCode)
        }
    }

    fun updateTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            repository.updateThemeMode(themeMode)
        }
    }

    fun updateThemeDynamicColor(themeDynamicColor: Boolean) {
        viewModelScope.launch {
            repository.updateThemeDynamicColor(themeDynamicColor)
        }
    }

    fun updateThemeColor(color: Int) {
        viewModelScope.launch {
            repository.updateThemeColor(color)
        }
    }

    fun updateSelectedSchedule(id: Short) {
        viewModelScope.launch {
            repository.updateSelectedSchedule(id)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun createSchedule(schedule: Schedule) {
        val id = settings.value.newScheduleId()
        viewModelScope.launch {
            repository.createSchedule(id, schedule)
            repository.updateSelectedSchedule(id)
        }
    }

    fun updateSchedule(id: Short = settings.value.selectedScheduleID, schedule: Schedule) {
        viewModelScope.launch {
            repository.updateSchedule(id, schedule)
        }
    }

    fun deleteSchedule(id: Short, permanently: Boolean = false) {
        viewModelScope.launch {
            repository.deleteSchedule(id, permanently)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun exportScheduleToFile(
        id: Short = settings.value.selectedScheduleID,
        file: PlatformFile,
        showMessage: (String) -> Unit
    ) {
        viewModelScope.launch {
            val schedule = settings.value.schedules[id]
            if (schedule == null) {
                showMessage(
                    getString(
                        Res.string.schedule_value_export_schedule_failed,
                        "Schedule not found"
                    )
                )
                return@launch
            }
            try {
                writeBytesToFile(schedule.toProtoBufByteArray(), file)
                showMessage(
                    getString(
                        Res.string.schedule_value_export_schedule_success,
                        file.name
                    )
                )
            } catch (e: Exception) {
                showMessage(
                    getString(
                        Res.string.schedule_value_export_schedule_failed,
                        e.message ?: ""
                    )
                )
                e.printStackTrace()
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun importScheduleFromFile(
        file: PlatformFile,
        showMessage: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val bytes = file.readBytes()
                val importedSchedule = readScheduleFromByteArray(bytes)
                createSchedule(importedSchedule!!)
                showMessage(
                    getString(
                        Res.string.schedule_value_import_schedule_success,
                        importedSchedule.name
                    )
                )
            } catch (e: Exception) {
                showMessage(
                    getString(
                        Res.string.schedule_value_import_schedule_failed,
                        e.message ?: ""
                    )
                )
                e.printStackTrace()
            }
        }
    }

    companion object {
        val APP_CONTAINER_KEY = CreationExtras.Key<IAppContainer>()
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContainer = this[APP_CONTAINER_KEY] as IAppContainer
                val repository = appContainer.dataRepository
                TimeFlowViewModel(repository = repository)
            }
        }

        fun newCreationExtras(appContainer: IAppContainer): CreationExtras =
            MutableCreationExtras().apply {
                set(APP_CONTAINER_KEY, appContainer)
            }
    }
}

private const val TIMEOUT_MILLIS = 5_000L