package xyz.hyli.timeflow.di

import xyz.hyli.timeflow.datastore.SettingsDataStore

expect class Factory {
    fun createDataStore(): SettingsDataStore
}