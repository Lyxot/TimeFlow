import androidx.compose.ui.window.ComposeUIViewController
import xyz.hyli.timeflow.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
