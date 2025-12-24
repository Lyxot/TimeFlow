/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarViewDay
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.data.Course
import xyz.hyli.timeflow.shared.generated.resources.Res
import xyz.hyli.timeflow.shared.generated.resources.page_schedule
import xyz.hyli.timeflow.shared.generated.resources.page_settings
import xyz.hyli.timeflow.shared.generated.resources.page_today
import xyz.hyli.timeflow.ui.components.ifThen
import xyz.hyli.timeflow.ui.pages.schedule.ScheduleScreen
import xyz.hyli.timeflow.ui.pages.schedule.subpage.EditCourseScreen
import xyz.hyli.timeflow.ui.pages.schedule.subpage.ScheduleListScreen
import xyz.hyli.timeflow.ui.pages.settings.SettingsScreen
import xyz.hyli.timeflow.ui.pages.settings.subpage.AboutScreen
import xyz.hyli.timeflow.ui.pages.settings.subpage.LessonsPerDayScreen
import xyz.hyli.timeflow.ui.pages.settings.subpage.LicenseScreen
import xyz.hyli.timeflow.ui.pages.today.TodayScreen
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isMacOS
import xyz.hyli.timeflow.utils.isWindows

@Serializable
sealed class Destination {
    @Serializable
    object ScheduleGraph

    @Serializable
    data object Schedule : Destination() {
        @Serializable
        object ScheduleList : Destination()

        @Serializable
        data class EditCourse(
            val courseID: Int,
            val courseHexString: String
        ) : Destination() {
            @OptIn(ExperimentalSerializationApi::class)
            constructor(courseID: Short, course: Course) : this(
                courseID = courseID.toInt(),
                courseHexString = ProtoBuf.encodeToHexString(course)
            )

            @OptIn(ExperimentalSerializationApi::class)
            fun toCourse(): Pair<Short, Course> {
                return Pair(
                    courseID.toShort(),
                    ProtoBuf.decodeFromHexString<Course>(courseHexString)
                )
            }
        }
    }

    @Serializable
    object Today : Destination()

    @Serializable
    object SettingsGraph

    @Serializable
    data object Settings : Destination() {
        @Serializable
        object LessonsPerDay

        @Serializable
        object About

        @Serializable
        object License
    }
}

val NavigationBarType = listOf(
    NavigationSuiteType.NavigationBar,
    NavigationSuiteType.ShortNavigationBarCompact,
    NavigationSuiteType.ShortNavigationBarMedium
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AdaptiveNavigation(
    navHostController: NavHostController,
    navSuiteType: NavigationSuiteType,
    content: @Composable () -> Unit
) {
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    val destination = navBackStackEntry?.destination

    val isSchedule =
        destination?.hierarchy?.any { it.hasRoute<Destination.ScheduleGraph>() } == true
    val isToday = destination?.hasRoute<Destination.Today>() == true
    val isSettings =
        destination?.hierarchy?.any { it.hasRoute<Destination.SettingsGraph>() } == true

    val state = rememberNavigationSuiteScaffoldState()
    LaunchedEffect(isSchedule, isToday, isSettings, navSuiteType) {
        if (navSuiteType in NavigationBarType) {
            val isInMainPage = destination?.hasRoute<Destination.Schedule>() == true ||
                    destination?.hasRoute<Destination.Today>() == true ||
                    destination?.hasRoute<Destination.Settings>() == true

            if (isInMainPage) {
                state.show()
            } else {
                state.hide()
            }
        }
    }

    NavigationSuiteScaffold(
        state = state,
        layoutType = navSuiteType,
        modifier = Modifier.fillMaxSize(),
        navigationSuiteColors = NavigationSuiteDefaults.colors().let {
            NavigationSuiteDefaults.colors(
                navigationRailContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                navigationDrawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            )
        },
        navigationSuiteItems = {
            item(
                modifier = Modifier
                    .testTag("ScheduleNavItem")
                    .ifThen(navSuiteType !in NavigationBarType && (currentPlatform().isMacOS() || currentPlatform().isWindows())) {
                        Modifier.padding(top = 28.dp)
                    },
                icon = {
                    Icon(
                        if (isSchedule)
                            Icons.AutoMirrored.Filled.EventNote
                        else
                            Icons.AutoMirrored.Outlined.EventNote,
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(Res.string.page_schedule)) },
                selected = isSchedule,
                onClick = { switchPageSingleTop(navHostController, Destination.Schedule) }
            )
            item(
                icon = {
                    Icon(
                        if (isToday)
                            Icons.Filled.CalendarViewDay
                        else
                            Icons.Outlined.CalendarViewDay,
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(Res.string.page_today)) },
                selected = isToday,
                onClick = { switchPageSingleTop(navHostController, Destination.Today) }
            )
            item(
                icon = {
                    Icon(
                        if (isSettings)
                            Icons.Filled.Settings
                        else
                            Icons.Outlined.Settings,
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(Res.string.page_settings)) },
                selected = isSettings,
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
fun TimeFlowNavHost(
    modifier: Modifier = Modifier,
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController,
) {
    NavHost(
        modifier = modifier.fillMaxSize(),
        navController = navHostController,
        startDestination = Destination.ScheduleGraph,
        enterTransition = NavigationAnimation.enterFadeIn,
        exitTransition = NavigationAnimation.exitFadeOut,
        popEnterTransition = NavigationAnimation.enterFadeIn,
        popExitTransition = NavigationAnimation.exitFadeOut
    ) {
        composable<Destination.Today> { TodayScreen(viewModel) }

        navigation<Destination.ScheduleGraph>(startDestination = Destination.Schedule) {
            composable<Destination.Schedule> { ScheduleScreen(viewModel, navHostController) }
            subScreenComposable<Destination.Schedule.ScheduleList> {
                ScheduleListScreen(viewModel, navHostController)
            }
            composable<Destination.Schedule.EditCourse>(
                enterTransition = NavigationAnimation.enterSlideIn,
                exitTransition = NavigationAnimation.exitSlideOut,
                popEnterTransition = NavigationAnimation.enterSlideIn,
                popExitTransition = NavigationAnimation.exitSlideOut
            ) { backStackEntry ->
                val destination = backStackEntry.toRoute<Destination.Schedule.EditCourse>()
                val (courseID, course) = destination.toCourse()
                EditCourseScreen(viewModel, navHostController, courseID, course)
            }
        }

        navigation<Destination.SettingsGraph>(startDestination = Destination.Settings) {
            composable<Destination.Settings> { SettingsScreen(viewModel, navHostController) }
            subScreenComposable<Destination.Settings.LessonsPerDay> {
                LessonsPerDayScreen(viewModel, navHostController)
            }
            subScreenComposable<Destination.Settings.About> {
                AboutScreen(navHostController)
            }
            subScreenComposable<Destination.Settings.License> {
                LicenseScreen(navHostController)
            }
        }
    }
}

fun switchPageSingleTop(
    navHostController: NavHostController,
    destination: Destination
) {
    navHostController.navigate(destination) {
        popUpTo(navHostController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private class NavigationAnimation {
    companion object {
        val enterSlideIn: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
            {
                slideIn(
                    initialOffset = { IntOffset(it.width / 2, 0) },
                    animationSpec = tween(300)
                ) +
                        fadeIn(animationSpec = tween(300))
            }

        val exitSlideOut: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
            {
                slideOut(targetOffset = { IntOffset(it.width, 0) }, animationSpec = tween(300)) +
                        fadeOut(animationSpec = tween(300))
            }

        val enterFadeIn: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
            {
                fadeIn(animationSpec = tween(300))
            }

        val exitFadeOut: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
            {
                fadeOut(animationSpec = tween(300))
            }
    }
}

private inline fun <reified T : Any> NavGraphBuilder.subScreenComposable(
    crossinline content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    composable<T>(
        enterTransition = NavigationAnimation.enterSlideIn,
        exitTransition = NavigationAnimation.exitSlideOut,
        popEnterTransition = NavigationAnimation.enterSlideIn,
        popExitTransition = NavigationAnimation.exitSlideOut
    ) {
        content(it)
    }
}