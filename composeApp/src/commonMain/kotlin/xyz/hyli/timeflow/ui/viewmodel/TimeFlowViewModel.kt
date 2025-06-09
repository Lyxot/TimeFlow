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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.hyli.timeflow.datastore.Schedule
import xyz.hyli.timeflow.datastore.Settings
import xyz.hyli.timeflow.di.AppContainer
import xyz.hyli.timeflow.di.DataRepository

class TimeFlowViewModel(
    private val repository: DataRepository
): ViewModel() {
    val settings: StateFlow<Settings> =
        repository.settings
            .map { Settings(
                firstLaunch = it.firstLaunch,
                theme = it.theme,
                themeDynamicColor = it.themeDynamicColor,
                themeColor = it.themeColor,
                schedule = it.schedule,
                selectedSchedule = it.selectedSchedule
            ) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = Settings()
            )

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

    fun updateThemeColor(color: Long) {
        viewModelScope.launch {
            repository.updateThemeColor(color)
        }
    }

    fun updateSelectedSchedule(uuid: String) {
        viewModelScope.launch {
            repository.updateSelectedSchedule(uuid)
        }
    }

    fun createSchedule(uuid: String, schedule: Schedule) {
        viewModelScope.launch {
            repository.createSchedule(uuid, schedule)
        }
    }

    fun updateSchedule(uuid: String, schedule: Schedule) {
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