package xyz.hyli.timeflow

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import xyz.hyli.timeflow.ui.navigation.navHost
import xyz.hyli.timeflow.ui.theme.AppTheme
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@Preview
@Composable
internal fun App(
    viewModel: TimeFlowViewModel
) = AppTheme(viewModel) {
    val navController = rememberNavController()
    navHost(
        viewModel = viewModel,
        navHostController = navController
    )
}
