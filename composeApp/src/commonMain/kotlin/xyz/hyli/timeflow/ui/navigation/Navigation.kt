package xyz.hyli.timeflow.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.page_events
import timeflow.composeapp.generated.resources.page_schedule
import timeflow.composeapp.generated.resources.page_settings
import xyz.hyli.timeflow.ui.icons.Events
import xyz.hyli.timeflow.ui.icons.EventsFilled
import xyz.hyli.timeflow.ui.icons.Schedule
import xyz.hyli.timeflow.ui.icons.ScheduleFilled
import xyz.hyli.timeflow.ui.icons.Settings
import xyz.hyli.timeflow.ui.icons.SettingsFilled
import xyz.hyli.timeflow.ui.pages.EventsScreen
import xyz.hyli.timeflow.ui.pages.ScheduleScreen
import xyz.hyli.timeflow.ui.pages.SettingsLessonsPerDayScreen
import xyz.hyli.timeflow.ui.pages.SettingsScreen
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop

enum class Destination {
    Schedule,
    Events,
    Settings,
    SettingsLessonsPerDay
}

@Composable
fun AdaptiveNavigation(
    navHostController: NavHostController,
    navSuiteType: NavigationSuiteType,
    content: @Composable () -> Unit
) {
    val currentPage = navHostController.currentDestination?.route
    NavigationSuiteScaffold(
        layoutType = navSuiteType,
        modifier = Modifier.fillMaxSize(),
        navigationSuiteItems = {
            item(
                icon = { Icon(
                    if (currentPage == Destination.Schedule.name) ScheduleFilled else Schedule,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_schedule)) },
                selected = currentPage == Destination.Schedule.name,
                onClick = { switchPageSingleTop(navHostController, Destination.Schedule) }
            )
            item(
                icon = { Icon(
                    if (currentPage == Destination.Events.name) EventsFilled else Events,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_events)) },
                selected = currentPage == Destination.Events.name,
                onClick = { switchPageSingleTop(navHostController, Destination.Events) }
            )
            item(
                icon = { Icon(
                    if (currentPage == Destination.Settings.name ||
                        currentPage == Destination.SettingsLessonsPerDay.name)
                        SettingsFilled
                    else
                        Settings,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_settings)) },
                selected = currentPage == Destination.Settings.name ||
                        currentPage == Destination.SettingsLessonsPerDay.name,
                onClick = { switchPageSingleTop(navHostController, Destination.Settings) }
            )
        }
    ) {
        content()
    }
}

@Composable
fun navHost(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val customNavSuiteType = with(adaptiveInfo) {
        if (currentPlatform().isDesktop()) {
            NavigationSuiteType.NavigationRail
        } else {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
        }
    }
    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navHostController,
        startDestination = Destination.Schedule.name,
        enterTransition = NavigationAnimation.enterFadeIn,
        exitTransition = NavigationAnimation.exitFadeOut,
        popEnterTransition = NavigationAnimation.enterFadeIn,
        popExitTransition = NavigationAnimation.exitFadeOut
    ) {
        composable(Destination.Events.name) { AdaptiveNavigation(navHostController, customNavSuiteType) { EventsScreen(viewModel, navHostController) } }
        composable(Destination.Schedule.name) { AdaptiveNavigation(navHostController, customNavSuiteType) { ScheduleScreen(viewModel, navHostController) } }
        composable(Destination.Settings.name) { AdaptiveNavigation(navHostController, customNavSuiteType) { SettingsScreen(viewModel, navHostController) } }
        composable(
            Destination.SettingsLessonsPerDay.name,
            enterTransition = NavigationAnimation.enterSlideIn,
            exitTransition = NavigationAnimation.exitSlideOut,
            popEnterTransition = NavigationAnimation.enterSlideIn,
            popExitTransition = NavigationAnimation.exitSlideOut
        ) {
            if (customNavSuiteType == NavigationSuiteType.NavigationBar) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    SettingsLessonsPerDayScreen(viewModel, navHostController)
                }
            } else {
                AdaptiveNavigation(navHostController, customNavSuiteType) { SettingsLessonsPerDayScreen(viewModel, navHostController) }
            }
        }
    }
}

fun switchPageSingleTop(
    navHostController: NavHostController,
    destination: Destination
) {
    navHostController.navigate(destination.name) {
        popUpTo(navHostController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private class NavigationAnimation {
    companion object {
        val enterSlideIn: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
            slideIn(initialOffset = { IntOffset(it.width / 2, 0) }, animationSpec = tween(300)) +
                    fadeIn(animationSpec = tween(300))
        }

        val exitSlideOut: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
            slideOut(targetOffset = { IntOffset(it.width, 0) }, animationSpec = tween(300)) +
                    fadeOut(animationSpec = tween(300))
        }

        val enterFadeIn: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
            fadeIn(animationSpec = tween(300))
        }

        val exitFadeOut: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
            fadeOut(animationSpec = tween(300))
        }
    }
}