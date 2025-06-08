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
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import xyz.hyli.timeflow.ui.pages.EventsScreen
import xyz.hyli.timeflow.ui.pages.ScheduleScreen
import xyz.hyli.timeflow.ui.pages.SettingsLessonsPerDayScreen
import xyz.hyli.timeflow.ui.pages.SettingsScreen
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

enum class Destination() {
    Schedule,
    Events,
    Settings,
    SettingsLessonsPerDay
}

@Composable
fun CompactScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    navHost(
        viewModel,
        navHostController,
        WindowWidthSizeClass.Compact
    ) { content ->
        CompactNavigation(navHostController) {
            content()
        }
    }
}

@Composable
fun MediumScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    navHost(
        viewModel,
        navHostController,
        WindowWidthSizeClass.Medium
    ) { content ->
        MediumNavigation(navHostController) {
            content()
        }
    }
}

@Composable
fun ExpandedScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    navHost(
        viewModel,
        navHostController,
        WindowWidthSizeClass.Expanded
    ) { content ->
        ExpandedNavigation(navHostController) {
            content()
        }
    }
}

@Composable
fun navHost(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    provider: @Composable (@Composable () -> Unit) -> Unit
) {
    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navHostController,
        startDestination = Destination.Schedule.name,
        enterTransition = NavigationAnimation.enterFadeIn,
        exitTransition = NavigationAnimation.exitFadeOut,
        popEnterTransition = NavigationAnimation.enterFadeIn,
        popExitTransition = NavigationAnimation.exitFadeOut
    ) {
        composable(Destination.Events.name) { provider { EventsScreen(viewModel, navHostController) } }
        composable(Destination.Schedule.name) { provider { ScheduleScreen(viewModel, navHostController) } }
        composable(Destination.Settings.name) { provider { SettingsScreen(viewModel, navHostController) } }
        composable(
            Destination.SettingsLessonsPerDay.name,
            enterTransition = NavigationAnimation.enterSlideIn,
            exitTransition = NavigationAnimation.exitSlideOut,
            popEnterTransition = NavigationAnimation.enterSlideIn,
            popExitTransition = NavigationAnimation.exitSlideOut
        ) {
            if (widthSizeClass == WindowWidthSizeClass.Compact) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    SettingsLessonsPerDayScreen(viewModel, navHostController)
                }
            } else {
                provider { SettingsLessonsPerDayScreen(viewModel, navHostController) }
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