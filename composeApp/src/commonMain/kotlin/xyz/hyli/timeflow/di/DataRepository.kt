package xyz.hyli.timeflow.di

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import xyz.hyli.timeflow.datastore.SettingsDataStore

class DataRepository(
    private val settingsDataStore: SettingsDataStore,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val theme: Flow<Int> = settingsDataStore.settings.mapLatest {
        it.theme
    }
    suspend fun setTheme(theme: Int) {
        settingsDataStore.setTheme(theme)
    }
}