import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        // TODO: Remove this workaround when CMP-8323 resolved
        // https://youtrack.jetbrains.com/issue/CMP-8323
        var showContent: Boolean by remember { mutableStateOf(false) }
        if (showContent) {
            App(
                viewModel = ViewModelOwner(appContainer).timeFlowViewModel
            )
        }
        LaunchedEffect(Unit) {
            showContent = true
        }
    }
}

