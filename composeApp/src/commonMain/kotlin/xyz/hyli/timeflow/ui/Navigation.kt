package xyz.hyli.timeflow.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import xyz.hyli.timeflow.ui.pages.SettingsScreen
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@Composable
fun CompactScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController,
    colorScheme: ColorScheme
) {
    val currentPage by viewModel.currentPage
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = colorScheme.background
            ) {
                NavigationBarItem(
                    icon = { Icon(
                        if (currentPage == 0) ScheduleFilled else Schedule,
                        contentDescription = null) },
                    label = { Text(stringResource(Res.string.page_schedule)) },
                    selected = currentPage == 0,
                    onClick = { switchPage(viewModel, navHostController, 0, "ScheduleScreen") }
                )
                NavigationBarItem(
                    icon = { Icon(
                        if (currentPage == 1) EventsFilled else Events,
                        contentDescription = null) },
                    label = { Text(stringResource(Res.string.page_events)) },
                    selected = currentPage == 1,
                    onClick = { switchPage(viewModel, navHostController, 1, "EventsScreen") }
                )
                NavigationBarItem(
                    icon = { Icon(
                        if (currentPage == 2) SettingsFilled else Settings,
                        contentDescription = null) },
                    label = { Text(stringResource(Res.string.page_settings))},
                    selected = currentPage == 2,
                    onClick = { switchPage(viewModel, navHostController, 2, "SettingsScreen") }
                )
            }
        }
    ) { innerPadding ->
        navHost(viewModel, navHostController)
    }
}

@Composable
fun MediumScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController,
    colorScheme: ColorScheme
) {
    val currentPage by viewModel.currentPage
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        //TODO: Use Material 3 Expressive NavigationRail when available
        //https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#ModalWideNavigationRail(androidx.compose.ui.Modifier,androidx.compose.material3.WideNavigationRailState,kotlin.Boolean,androidx.compose.ui.graphics.Shape,androidx.compose.ui.graphics.Shape,androidx.compose.material3.WideNavigationRailColors,kotlin.Function0,androidx.compose.ui.unit.Dp,androidx.compose.foundation.layout.WindowInsets,androidx.compose.foundation.layout.Arrangement.Vertical,androidx.compose.material3.ModalWideNavigationRailProperties,kotlin.Function0
        NavigationRail(
            modifier = Modifier.navigationBarsPadding(),
            containerColor = colorScheme.background,
        ) {
            NavigationRailItem(
                modifier = Modifier.padding(vertical = 8.dp),
                icon = { Icon(
                    if (currentPage == 0) ScheduleFilled else Schedule,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_schedule)) },
                selected = currentPage == 0,
                onClick = { switchPage(viewModel, navHostController, 0, "ScheduleScreen") }
            )
            NavigationRailItem(
                modifier = Modifier.padding(vertical = 8.dp),
                icon = { Icon(
                    if (currentPage == 1) EventsFilled else Events,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_events)) },
                selected = currentPage == 1,
                onClick = { switchPage(viewModel, navHostController, 1, "EventsScreen") }
            )
            NavigationRailItem(
                modifier = Modifier.padding(vertical = 8.dp),
                icon = { Icon(
                    if (currentPage == 2) SettingsFilled else Settings,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_settings)) },
                selected = currentPage == 2,
                onClick = { switchPage(viewModel, navHostController, 2, "SettingsScreen") }
            )
        }
        navHost(viewModel, navHostController)
    }
}

@Composable
fun ExpandedScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController,
    colorScheme: ColorScheme
) {
    val currentPage by viewModel.currentPage
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        ModalDrawerSheet(
            drawerState = drawerState,
            modifier = Modifier.navigationBarsPadding(),
            drawerContainerColor = colorScheme.background
        ) {
            NavigationDrawerItem(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                icon = { Icon(
                    if (currentPage == 0) ScheduleFilled else Schedule,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_schedule)) },
                selected = currentPage == 0,
                onClick = { switchPage(viewModel, navHostController, 0, "ScheduleScreen") }
            )
            NavigationDrawerItem(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                icon = { Icon(
                    if (currentPage == 1) EventsFilled else Events,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_events)) },
                selected = currentPage == 1,
                onClick = { switchPage(viewModel, navHostController, 1, "EventsScreen") }
            )
            NavigationDrawerItem(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                icon = { Icon(
                    if (currentPage == 2) SettingsFilled else Settings,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_settings)) },
                selected = currentPage == 2,
                onClick = { switchPage(viewModel, navHostController, 2, "SettingsScreen") }
            )
        }
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            navHost(viewModel, navHostController)
        }
    }
}

@Composable
fun navHost(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    NavHost(
        navController = navHostController,
        startDestination = "ScheduleScreen",
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable("EventsScreen") { EventsScreen(viewModel) }
        composable("ScheduleScreen") { ScheduleScreen(viewModel) }
        composable("SettingsScreen") { SettingsScreen(viewModel) }
    }
}

fun switchPage(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController,
    id: Int,
    destination: String
) {
    viewModel.setCurrentPage(id)
    navHostController.navigate(destination) {
        popUpTo(navHostController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}