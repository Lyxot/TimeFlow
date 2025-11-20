package xyz.hyli.timeflow.di

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import xyz.hyli.timeflow.datastore.Schedule
import xyz.hyli.timeflow.datastore.Settings
import xyz.hyli.timeflow.datastore.SettingsDataStore

interface IDataRepository {
    val settings: Flow<Settings>
    suspend fun updateFirstLaunch(versionCode: Int)
    suspend fun updateTheme(theme: Int)
    suspend fun updateThemeDynamicColor(themeDynamicColor: Boolean)
    suspend fun updateThemeColor(color: Int)
    suspend fun updateSelectedSchedule(uuid: String)
    suspend fun createSchedule(uuid: String, schedule: Schedule)
    suspend fun updateSchedule(uuid: String, schedule: Schedule)
}

class DataRepository(
    private val settingsDataStore: SettingsDataStore,
) : IDataRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val settings: Flow<Settings> = settingsDataStore.settings
    override suspend fun updateFirstLaunch(versionCode: Int) {
        settingsDataStore.updateFirstLaunch(versionCode)
    }

    override suspend fun updateTheme(theme: Int) {
        settingsDataStore.updateTheme(theme)
    }

    override suspend fun updateThemeDynamicColor(themeDynamicColor: Boolean) {
        settingsDataStore.updateThemeDynamicColor(themeDynamicColor)
    }

    override suspend fun updateThemeColor(color: Int) {
        settingsDataStore.updateThemeColor(color)
    }

    override suspend fun updateSelectedSchedule(uuid: String) {
        settingsDataStore.updateSelectedSchedule(uuid)
    }

    override suspend fun createSchedule(uuid: String, schedule: Schedule) {
        settingsDataStore.createSchedule(uuid, schedule)
    }

    override suspend fun updateSchedule(uuid: String, schedule: Schedule) {
        settingsDataStore.updateSchedule(uuid, schedule)
    }
}