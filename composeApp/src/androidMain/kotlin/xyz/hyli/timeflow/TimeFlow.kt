package xyz.hyli.timeflow

import android.app.Application
import xyz.hyli.timeflow.di.AppContainer
import xyz.hyli.timeflow.di.Factory

class TimeFlow: Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(Factory(this))
    }
}