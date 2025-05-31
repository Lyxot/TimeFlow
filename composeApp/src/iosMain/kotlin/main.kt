import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import xyz.hyli.timeflow.App
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Suppress("unused", "FunctionName")
fun MainViewController(viewModel: TimeFlowViewModel): UIViewController = ComposeUIViewController {
    val size = calculateWindowSizeClass()
    App(
        viewModel = viewModel,
        windowSizeClass = size
    )
}
