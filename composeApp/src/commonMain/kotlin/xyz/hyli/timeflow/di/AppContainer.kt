package xyz.hyli.timeflow.di

class AppContainer(
    private val factory: Factory,
) {
    val dataRepository: DataRepository by lazy {
        DataRepository(
            settingsDataStore = factory.createDataStore(),
        )
    }
}