package xyz.hyli.timeflow.di

import android.app.Application
import xyz.hyli.timeflow.datastore.SettingsDataStore
import xyz.hyli.timeflow.datastore.dataStoreFileName

actual class Factory(
    private val app: Application,
) {
    actual fun createDataStore(): SettingsDataStore =
        SettingsDataStore {
            app.filesDir
                .resolve(dataStoreFileName)
                .absolutePath
        }
}