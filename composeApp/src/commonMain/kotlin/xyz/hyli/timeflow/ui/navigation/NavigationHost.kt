package xyz.hyli.timeflow.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import xyz.hyli.timeflow.ui.pages.EventsScreen
import xyz.hyli.timeflow.ui.pages.ScheduleScreen
import xyz.hyli.timeflow.ui.pages.SettingsScreen
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

enum class Destination() {
    Schedule,
    Events,
    Settings,
}

@Composable
fun CompactScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    navHost(
        viewModel,
        navHostController
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
        navHostController
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
        navHostController
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
    provider: @Composable (@Composable () -> Unit) -> Unit
) {
    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navHostController,
        startDestination = Destination.Schedule.name,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable(Destination.Events.name) { provider { EventsScreen(viewModel, navHostController) } }
        composable(Destination.Schedule.name) { provider { ScheduleScreen(viewModel, navHostController) } }
        composable(Destination.Settings.name) { provider { SettingsScreen(viewModel, navHostController) } }
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