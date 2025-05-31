package xyz.hyli.timeflow.di

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import xyz.hyli.timeflow.datastore.Settings
import xyz.hyli.timeflow.datastore.SettingsDataStore

class DataRepository(
    private val settingsDataStore: SettingsDataStore,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val settings: Flow<Settings> = settingsDataStore.settings
    suspend fun setTheme(theme: Int) {
        settingsDataStore.setTheme(theme)
    }
}