package xyz.hyli.timeflow.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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

@Composable
fun CompactNavigation(
    navHostController: NavHostController,
    content: @Composable () -> Unit
) {
    val currentPage = Destination.valueOf(
        navHostController.currentDestination?.route ?: Destination.Schedule.name
    )
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(
                        if (currentPage == Destination.Schedule) ScheduleFilled else Schedule,
                        contentDescription = null) },
                    label = { Text(stringResource(Res.string.page_schedule)) },
                    selected = currentPage == Destination.Schedule,
                    onClick = { switchPageSingleTop(navHostController, Destination.Schedule) }
                )
                NavigationBarItem(
                    icon = { Icon(
                        if (currentPage == Destination.Events) EventsFilled else Events,
                        contentDescription = null) },
                    label = { Text(stringResource(Res.string.page_events)) },
                    selected = currentPage == Destination.Events,
                    onClick = { switchPageSingleTop(navHostController, Destination.Events) }
                )
                NavigationBarItem(
                    icon = { Icon(
                        if (currentPage == Destination.Settings ||
                            currentPage == Destination.SettingsLessonsPerDay)
                            SettingsFilled
                        else
                            Settings,
                        contentDescription = null) },
                    label = { Text(stringResource(Res.string.page_settings))},
                    selected = currentPage == Destination.Settings ||
                            currentPage == Destination.SettingsLessonsPerDay,
                    onClick = { switchPageSingleTop(navHostController, Destination.Settings) }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .padding(horizontal = 8.dp)
        ) {
            content()
        }
    }
}

@Composable
fun MediumNavigation(
    navHostController: NavHostController,
    content: @Composable () -> Unit
) {
    val currentPage = Destination.valueOf(
        navHostController.currentDestination?.route ?: Destination.Schedule.name
    )
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        //TODO: Use Material 3 Expressive NavigationRail when available
        //https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#ModalWideNavigationRail(androidx.compose.ui.Modifier,androidx.compose.material3.WideNavigationRailState,kotlin.Boolean,androidx.compose.ui.graphics.Shape,androidx.compose.ui.graphics.Shape,androidx.compose.material3.WideNavigationRailColors,kotlin.Function0,androidx.compose.ui.unit.Dp,androidx.compose.foundation.layout.WindowInsets,androidx.compose.foundation.layout.Arrangement.Vertical,androidx.compose.material3.ModalWideNavigationRailProperties,kotlin.Function0
        NavigationRail {
            NavigationRailItem(
                modifier = Modifier.padding(vertical = 8.dp),
                icon = { Icon(
                    if (currentPage == Destination.Schedule) ScheduleFilled else Schedule,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_schedule)) },
                selected = currentPage == Destination.Schedule,
                onClick = { switchPageSingleTop(navHostController, Destination.Schedule) }
            )
            NavigationRailItem(
                modifier = Modifier.padding(vertical = 8.dp),
                icon = { Icon(
                    if (currentPage == Destination.Events) EventsFilled else Events,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_events)) },
                selected = currentPage == Destination.Events,
                onClick = { switchPageSingleTop(navHostController, Destination.Events) }
            )
            NavigationRailItem(
                modifier = Modifier.padding(vertical = 8.dp),
                icon = { Icon(
                    if (currentPage == Destination.Settings ||
                        currentPage == Destination.SettingsLessonsPerDay)
                        SettingsFilled
                    else
                        Settings,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_settings)) },
                selected = currentPage == Destination.Settings ||
                        currentPage == Destination.SettingsLessonsPerDay,
                onClick = { switchPageSingleTop(navHostController, Destination.Settings) }
            )
        }
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
        ) {
            content()
        }
    }
}

@Composable
fun ExpandedNavigation(
    navHostController: NavHostController,
    content: @Composable () -> Unit
) {
    val currentPage = Destination.valueOf(
        navHostController.currentDestination?.route ?: Destination.Schedule.name
    )
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        ModalDrawerSheet {
            NavigationDrawerItem(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                icon = { Icon(
                    if (currentPage == Destination.Schedule) ScheduleFilled else Schedule,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_schedule)) },
                selected = currentPage == Destination.Schedule,
                onClick = { switchPageSingleTop(navHostController, Destination.Schedule) }
            )
            NavigationDrawerItem(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                icon = { Icon(
                    if (currentPage == Destination.Events) EventsFilled else Events,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_events)) },
                selected = currentPage == Destination.Events,
                onClick = { switchPageSingleTop(navHostController, Destination.Events) }
            )
            NavigationDrawerItem(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                icon = { Icon(
                    if (currentPage == Destination.Settings ||
                        currentPage == Destination.SettingsLessonsPerDay)
                        SettingsFilled
                    else
                        Settings,
                    contentDescription = null) },
                label = { Text(stringResource(Res.string.page_settings)) },
                selected = currentPage == Destination.Settings ||
                        currentPage == Destination.SettingsLessonsPerDay,
                onClick = { switchPageSingleTop(navHostController, Destination.Settings) }
            )
        }
        Scaffold(
            modifier = Modifier.weight(1f)
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                content()
            }
        }
    }
}