package xyz.hyli.timeflow.di

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import xyz.hyli.timeflow.datastore.SettingsDataStore
import xyz.hyli.timeflow.datastore.dataStoreFileName

actual class Factory {
    actual fun createDataStore(): SettingsDataStore =
        SettingsDataStore {
            "${fileDirectory()}/$dataStoreFileName"
        }

    @OptIn(ExperimentalForeignApi::class)
    private fun fileDirectory(): String {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(documentDirectory).path!!
    }
}