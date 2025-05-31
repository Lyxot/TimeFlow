import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.window.ComposeUIViewController
import xyz.hyli.timeflow.App
import platform.UIKit.UIViewController
import xyz.hyli.timeflow.di.AppContainer
import xyz.hyli.timeflow.di.Factory
import xyz.hyli.timeflow.ui.viewmodel.ViewModelOwner

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Suppress("unused")
fun MainViewController(): UIViewController = ComposeUIViewController {
    val appContainer = AppContainer(Factory())
    val size = calculateWindowSizeClass()
    App(
        viewModel = ViewModelOwner(appContainer).timeFlowViewModel,
        windowSizeClass = size
    )
}
