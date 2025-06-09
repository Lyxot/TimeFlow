package xyz.hyli.timeflow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import xyz.hyli.timeflow.ui.navigation.AdaptiveNavigation
import xyz.hyli.timeflow.ui.navigation.navHost
import xyz.hyli.timeflow.ui.theme.AppTheme
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop

@Preview
@Composable
internal fun App(
    viewModel: TimeFlowViewModel
) = AppTheme(viewModel) {
    val navController = rememberNavController()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val customNavSuiteType = with(adaptiveInfo) {
        if (currentPlatform().isDesktop()) {
            NavigationSuiteType.NavigationRail
        } else {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
        }
    }
    AdaptiveNavigation(
        navHostController = navController,
        navSuiteType = customNavSuiteType
    ) {
        Box(
            modifier = if (currentPlatform().isDesktop()) Modifier
                else Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp)
        ) {
            navHost(
                viewModel = viewModel,
                navHostController = navController
            )
        }
    }
}
