package xyz.hyli.timeflow.di

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import xyz.hyli.timeflow.datastore.Schedule
import xyz.hyli.timeflow.datastore.Settings
import xyz.hyli.timeflow.datastore.SettingsDataStore

class DataRepository(
    private val settingsDataStore: SettingsDataStore,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val settings: Flow<Settings> = settingsDataStore.settings
    suspend fun updateTheme(theme: Int) {
        settingsDataStore.updateTheme(theme)
    }
    suspend fun updateThemeDynamicColor(themeDynamicColor: Boolean) {
        settingsDataStore.updateThemeDynamicColor(themeDynamicColor)
    }
    suspend fun updateThemeColor(color: Long) {
        settingsDataStore.updateThemeColor(color)
    }
    suspend fun updateSchedule(uuid: String, schedule: Schedule) {
        settingsDataStore.updateSchedule(uuid, schedule)
    }
}