import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import xyz.hyli.timeflow.App
import xyz.hyli.timeflow.di.AppContainer
import xyz.hyli.timeflow.di.Factory
import xyz.hyli.timeflow.ui.viewmodel.ViewModelOwner
import java.awt.Dimension

fun main() = application {
    val appContainer = AppContainer(Factory())
    Window(
        title = "TimeFlow",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(480, 540)
        App(
            viewModel = ViewModelOwner(appContainer).timeFlowViewModel
        )
    }
}

