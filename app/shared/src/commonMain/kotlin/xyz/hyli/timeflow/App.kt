/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import xyz.hyli.timeflow.ui.components.drawRoundedCornerBackground
import xyz.hyli.timeflow.ui.components.ifThen
import xyz.hyli.timeflow.ui.navigation.AdaptiveNavigation
import xyz.hyli.timeflow.ui.navigation.NavigationBarType
import xyz.hyli.timeflow.ui.navigation.TimeFlowNavHost
import xyz.hyli.timeflow.ui.theme.AppTheme
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

internal val LocalNavSuiteType = compositionLocalOf { mutableStateOf(NavigationSuiteType.None) }

@Preview
@Composable
fun App(
    viewModel: TimeFlowViewModel,
    typography: Typography? = null
) = AppTheme(
    viewModel = viewModel,
    typography = typography,
    content = AppContent(viewModel)
)

@Suppress("ComposableNaming")
@Composable
fun AppContent(viewModel: TimeFlowViewModel): @Composable (() -> Unit) = {
    LaunchedEffect(viewModel.settings.value.initialized) {
        if (viewModel.settings.value.initialized && viewModel.settings.value.firstLaunch < BuildConfig.APP_VERSION_CODE) {
            viewModel.updateFirstLaunch(BuildConfig.APP_VERSION_CODE)
        }
    }
    val navController = rememberNavController()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val customNavSuiteType = remember(adaptiveInfo) {
        mutableStateOf(
            with(adaptiveInfo) {
                // use NavigationRail on landscape phone
                NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo).let {
                    if (!windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
                        && windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
                    ) {
                        NavigationSuiteType.NavigationRail
                    } else {
                        it
                    }
                }
            }
        )
    }
    CompositionLocalProvider(
        LocalNavSuiteType provides customNavSuiteType
    ) {
        val navSuiteType by customNavSuiteType
        AdaptiveNavigation(
            navHostController = navController,
            navSuiteType = navSuiteType
        ) {
            TimeFlowNavHost(
                modifier = Modifier.ifThen(navSuiteType !in NavigationBarType) {
                    Modifier
                        .drawRoundedCornerBackground(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            cornerRadius = 24.dp,
                            topStart = true,
                            topEnd = false,
                            bottomStart = true,
                            bottomEnd = false
                        )
                },
                viewModel = viewModel,
                navHostController = navController,
            )
        }
    }
}
