import androidx.compose.ui.window.ComposeUIViewController
import xyz.hyli.timeflow.App
import platform.UIKit.UIViewController
import xyz.hyli.timeflow.di.AppContainer
import xyz.hyli.timeflow.di.Factory
import xyz.hyli.timeflow.viewmodel.ViewModelOwner

@Suppress("unused")
fun MainViewController(): UIViewController = ComposeUIViewController {
    val appContainer = AppContainer(Factory())
    App(
        viewModel = ViewModelOwner(appContainer).timeFlowViewModel
    )
}
