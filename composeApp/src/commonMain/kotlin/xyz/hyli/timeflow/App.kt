/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import xyz.hyli.timeflow.ui.navigation.AdaptiveNavigation
import xyz.hyli.timeflow.ui.navigation.NavigationBarType
import xyz.hyli.timeflow.ui.navigation.TimeFlowNavHost
import xyz.hyli.timeflow.ui.theme.AppTheme
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop

@Preview
@Composable
internal fun App(
    viewModel: TimeFlowViewModel
) = AppTheme(viewModel) {
    LaunchedEffect(viewModel.settings.value.initialized) {
        if (viewModel.settings.value.initialized && viewModel.settings.value.firstLaunch < BuildConfig.APP_VERSION_CODE) {
            viewModel.updateFirstLaunch(BuildConfig.APP_VERSION_CODE)
        }
    }
    val navController = rememberNavController()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val customNavSuiteType = with(adaptiveInfo) {
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
    AdaptiveNavigation(
        navHostController = navController,
        navSuiteType = customNavSuiteType
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (currentPlatform().isDesktop()) Modifier
                    else Modifier.windowInsetsPadding(WindowInsets.statusBars)
                )
                .then(
                    if (customNavSuiteType in NavigationBarType) Modifier.padding(horizontal = 8.dp)
                    else Modifier
                )
        ) {
            TimeFlowNavHost(
                viewModel = viewModel,
                navHostController = navController,
                navSuiteType = customNavSuiteType,
            )
        }
    }
}
