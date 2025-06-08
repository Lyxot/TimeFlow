package xyz.hyli.timeflow

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import xyz.hyli.timeflow.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import xyz.hyli.timeflow.ui.navigation.CompactScreen
import xyz.hyli.timeflow.ui.navigation.MediumScreen
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@Preview
@Composable
internal fun App(
    viewModel: TimeFlowViewModel,
    windowSizeClass: WindowSizeClass
) = AppTheme(viewModel) {
    val navController = rememberNavController()
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> CompactScreen(viewModel, navController)
        WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> MediumScreen(viewModel, navController)
        else -> MediumScreen(viewModel, navController)
    }
}
