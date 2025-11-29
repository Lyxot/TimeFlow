/*
 * Copyright (c) 2025 Lyxot and contributors.
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.hyli.timeflow.datastore.Schedule
import xyz.hyli.timeflow.datastore.Settings
import xyz.hyli.timeflow.di.AppContainer
import xyz.hyli.timeflow.di.IDataRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TimeFlowViewModel(
    private val repository: IDataRepository
): ViewModel() {
    val settings: StateFlow<Settings> =
        repository.settings
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = Settings(initialized = false)
            )

    fun updateFirstLaunch(versionCode: Int) {
        viewModelScope.launch {
            repository.updateFirstLaunch(versionCode)
        }
    }

    fun updateTheme(theme: Int) {
        viewModelScope.launch {
            repository.updateTheme(theme)
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

    fun updateSelectedSchedule(uuid: String) {
        viewModelScope.launch {
            repository.updateSelectedSchedule(uuid)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun createSchedule(schedule: Schedule) {
        var uuid = Uuid.random().toString()
        while (settings.value.schedule.containsKey(uuid)) {
            uuid = Uuid.random().toString()
        }
        viewModelScope.launch {
            repository.createSchedule(uuid, schedule)
            repository.updateSelectedSchedule(uuid)
        }
    }

    fun updateSchedule(uuid: String = settings.value.selectedSchedule, schedule: Schedule) {
        viewModelScope.launch {
            repository.updateSchedule(uuid, schedule)
        }
    }

    companion object {
        val APP_CONTAINER_KEY = CreationExtras.Key<AppContainer>()
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContainer = this[APP_CONTAINER_KEY] as AppContainer
                val repository = appContainer.dataRepository
                TimeFlowViewModel(repository = repository)
            }
        }
        fun newCreationExtras(appContainer: AppContainer): CreationExtras =
            MutableCreationExtras().apply {
                set(APP_CONTAINER_KEY, appContainer)
            }
    }
}

private const val TIMEOUT_MILLIS = 5_000L