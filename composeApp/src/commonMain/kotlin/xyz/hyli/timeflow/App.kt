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
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop

@Preview
@Composable
internal fun App(
    viewModel: TimeFlowViewModel,
    windowSizeClass: WindowSizeClass
) = AppTheme(viewModel) {
    val navController = rememberNavController()
    if (currentPlatform().isDesktop()) {
        MediumScreen(viewModel, navController)
    } else {
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> CompactScreen(viewModel, navController)
            WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> MediumScreen(
                viewModel,
                navController
            )

            else -> MediumScreen(viewModel, navController)
        }
    }
}
