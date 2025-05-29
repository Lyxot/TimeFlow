package xyz.hyli.timeflow.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import xyz.hyli.timeflow.di.AppContainer

class ViewModelOwner(
    appContainer: AppContainer
): ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
    val timeFlowViewModel: TimeFlowViewModel = ViewModelProvider.create(
        owner = this as ViewModelStoreOwner,
        factory = TimeFlowViewModel.Factory,
        extras = TimeFlowViewModel.newCreationExtras(appContainer),
    )[TimeFlowViewModel::class]

    @Suppress("unused")
    fun clear() {
        viewModelStore.clear()
    }

}