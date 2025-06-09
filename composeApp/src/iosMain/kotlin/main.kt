import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import xyz.hyli.timeflow.App
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@Suppress("unused", "FunctionName")
fun MainViewController(viewModel: TimeFlowViewModel): UIViewController = ComposeUIViewController {
    App(
        viewModel = viewModel
    )
}
