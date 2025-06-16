package xyz.hyli.timeflow.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarViewDay
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.page_schedule
import timeflow.composeapp.generated.resources.page_settings
import timeflow.composeapp.generated.resources.page_today
import xyz.hyli.timeflow.datastore.Course
import xyz.hyli.timeflow.ui.pages.EditCourseScreen
import xyz.hyli.timeflow.ui.pages.ScheduleScreen
import xyz.hyli.timeflow.ui.pages.SettingsLessonsPerDayScreen
import xyz.hyli.timeflow.ui.pages.SettingsScreen
import xyz.hyli.timeflow.ui.pages.TodayScreen
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

enum class Destination {
    Schedule,
    Today,
    Settings
}

enum class SettingsDestination {
    LessonsPerDay
}

@Serializable
data class EditCourseDestination(
    val courseProtoBufHexString: String
) {
    @OptIn(ExperimentalSerializationApi::class)
    constructor(course: Course) : this(
        courseProtoBufHexString = ProtoBuf.encodeToHexString(course)
    )
}

@Composable
fun AdaptiveNavigation(
    navHostController: NavHostController,
    navSuiteType: NavigationSuiteType,
    content: @Composable () -> Unit
) {
    val currentBackStackEntry = navHostController.currentBackStackEntryFlow.collectAsState(initial = null)
    val currentPage = currentBackStackEntry.value?.destination?.route
    NavigationSuiteScaffold(
        layoutType =
            if (currentPage !in Destination.entries.map { it.name } &&
                navSuiteType == NavigationSuiteType.NavigationBar)
                NavigationSuiteType.None
            else
                navSuiteType,
        modifier = Modifier.fillMaxSize(),
        navigationSuiteItems = {
            item(
                icon = { Icon(
                    if (currentPage == Destination.Schedule.name ||
                        currentPage?.contains("EditCourseDestination") == true
                    )
                        Icons.AutoMirrored.Filled.EventNote
                    else
                        Icons.AutoMirrored.Outlined.EventNote,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_schedule)) },
                selected = currentPage == Destination.Schedule.name ||
                        currentPage?.contains("EditCourseDestination") == true,
                onClick = { switchPageSingleTop(navHostController, Destination.Schedule) }
            )
            item(
                icon = { Icon(
                    if (currentPage == Destination.Today.name)
                        Icons.Filled.CalendarViewDay
                    else
                        Icons.Outlined.CalendarViewDay,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_today)) },
                selected = currentPage == Destination.Today.name,
                onClick = { switchPageSingleTop(navHostController, Destination.Today) }
            )
            item(
                icon = { Icon(
                    if (currentPage == Destination.Settings.name ||
                        currentPage in SettingsDestination.entries.map { it.name }
                    )
                        Icons.Filled.Settings
                    else
                        Icons.Outlined.Settings,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_settings)) },
                selected = currentPage == Destination.Settings.name ||
                        currentPage in SettingsDestination.entries.map { it.name },
                onClick = { switchPageSingleTop(navHostController, Destination.Settings) }
            )
        }
    ) {
//        Text(currentPage ?: "Unknown Page")
        content()
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun navHost(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
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
        composable(Destination.Today.name) { TodayScreen(viewModel, navHostController) }
        composable(Destination.Schedule.name) { ScheduleScreen(viewModel, navHostController) }
        composable(Destination.Settings.name) { SettingsScreen(viewModel, navHostController) }
        subScreenComposable(SettingsDestination.LessonsPerDay.name) {
            SettingsLessonsPerDayScreen(viewModel, navHostController)
        }
        composable<EditCourseDestination>(
            enterTransition = NavigationAnimation.enterSlideIn,
            exitTransition = NavigationAnimation.exitSlideOut,
            popEnterTransition = NavigationAnimation.enterSlideIn,
            popExitTransition = NavigationAnimation.exitSlideOut
        ) { backStackEntry ->
            val courseProtoBufHexString: String =
                backStackEntry.toRoute<EditCourseDestination>().courseProtoBufHexString
            val course = ProtoBuf.decodeFromHexString<Course>(courseProtoBufHexString)
            EditCourseScreen(viewModel, navHostController, course)
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

private fun NavGraphBuilder.subScreenComposable(
    destination: String,
    content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    composable(
        destination,
        enterTransition = NavigationAnimation.enterSlideIn,
        exitTransition = NavigationAnimation.exitSlideOut,
        popEnterTransition = NavigationAnimation.enterSlideIn,
        popExitTransition = NavigationAnimation.exitSlideOut
    ) {
        content(it)
    }
}