package xyz.hyli.timeflow.di

import net.harawata.appdirs.AppDirsFactory
import xyz.hyli.timeflow.BuildConfig
import xyz.hyli.timeflow.datastore.SettingsDataStore
import xyz.hyli.timeflow.datastore.dataStoreFileName

actual class Factory {
    actual fun createDataStore(): SettingsDataStore =
        SettingsDataStore {
            "${fileDirectory()}/$dataStoreFileName"
        }

    private fun fileDirectory(): String =
        AppDirsFactory.getInstance().getUserDataDir(BuildConfig.APP_NAME, null, BuildConfig.AUTHOR)
}