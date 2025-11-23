package xyz.hyli.timeflow.ui.pages.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.page_settings
import timeflow.composeapp.generated.resources.settings_subtitle_create_schedule
import timeflow.composeapp.generated.resources.settings_subtitle_schedule_empty
import timeflow.composeapp.generated.resources.settings_subtitle_schedule_lessons_per_day
import timeflow.composeapp.generated.resources.settings_subtitle_schedule_not_selected
import timeflow.composeapp.generated.resources.settings_subtitle_theme_dynamic_color
import timeflow.composeapp.generated.resources.settings_theme_dark
import timeflow.composeapp.generated.resources.settings_theme_light
import timeflow.composeapp.generated.resources.settings_theme_system
import timeflow.composeapp.generated.resources.settings_title_create_schedule
import timeflow.composeapp.generated.resources.settings_title_display_weekends
import timeflow.composeapp.generated.resources.settings_title_general
import timeflow.composeapp.generated.resources.settings_title_schedule
import timeflow.composeapp.generated.resources.settings_title_schedule_lessons_per_day
import timeflow.composeapp.generated.resources.settings_title_schedule_name
import timeflow.composeapp.generated.resources.settings_title_schedule_term_end_date
import timeflow.composeapp.generated.resources.settings_title_schedule_term_start_date
import timeflow.composeapp.generated.resources.settings_title_schedule_total_weeks
import timeflow.composeapp.generated.resources.settings_title_selected_schedule
import timeflow.composeapp.generated.resources.settings_title_theme
import timeflow.composeapp.generated.resources.settings_title_theme_color
import timeflow.composeapp.generated.resources.settings_title_theme_dynamic_color
import timeflow.composeapp.generated.resources.settings_warning_schedule_name_empty
import xyz.hyli.timeflow.datastore.Date
import xyz.hyli.timeflow.datastore.Schedule
import xyz.hyli.timeflow.ui.components.BasePreference
import xyz.hyli.timeflow.ui.components.Dependency
import xyz.hyli.timeflow.ui.components.DialogInputValidator
import xyz.hyli.timeflow.ui.components.PreferenceBool
import xyz.hyli.timeflow.ui.components.PreferenceBoolStyle
import xyz.hyli.timeflow.ui.components.PreferenceColor
import xyz.hyli.timeflow.ui.components.PreferenceDate
import xyz.hyli.timeflow.ui.components.PreferenceDivider
import xyz.hyli.timeflow.ui.components.PreferenceInputText
import xyz.hyli.timeflow.ui.components.PreferenceList
import xyz.hyli.timeflow.ui.components.PreferenceListStyle
import xyz.hyli.timeflow.ui.components.PreferenceNumber
import xyz.hyli.timeflow.ui.components.PreferenceNumberStyle
import xyz.hyli.timeflow.ui.components.PreferenceScreen
import xyz.hyli.timeflow.ui.components.PreferenceSection
import xyz.hyli.timeflow.ui.components.rememberDialogInputValidator
import xyz.hyli.timeflow.ui.navigation.SettingsDestination
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop
import xyz.hyli.timeflow.utils.supportDynamicColor

@Composable
fun SettingsScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val settingsState = viewModel.settings.collectAsState()
    val settings by viewModel.settings.collectAsState()
    PreferenceScreen(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (currentPlatform().isDesktop())
                    Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
                else Modifier
            )
    ) {
        Text(
            text = stringResource(Res.string.page_settings),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(
                bottom = 16.dp,
                start = if (currentPlatform().isDesktop()) 0.dp else 16.dp,
                top = if (currentPlatform().isDesktop()) 0.dp else 16.dp
            )
        )
        // General Settings
        PreferenceSection(
            title = stringResource(Res.string.settings_title_general)
        ) {
            // Theme Settings
            val themeList = listOf(
                stringResource(Res.string.settings_theme_system),
                stringResource(Res.string.settings_theme_light),
                stringResource(Res.string.settings_theme_dark)
            )
            PreferenceList(
                style = PreferenceListStyle.Style.SegmentedButtons,
                value = settings.theme,
                onValueChange = {
                    viewModel.updateTheme(it)
                },
                items = listOf(0, 1, 2),
                itemTextProvider = { themeList[it] },
                title = stringResource(Res.string.settings_title_theme)
            )
            // Dynamic Color Settings
            val themeDynamicColorDependency =
                Dependency.State(mutableStateOf(currentPlatform())) {
                    currentPlatform().supportDynamicColor()
                }
            PreferenceBool(
                style = PreferenceBoolStyle.Style.Switch,
                value = settings.themeDynamicColor,
                onValueChange = {
                    viewModel.updateThemeDynamicColor(it)
                },
                title = stringResource(Res.string.settings_title_theme_dynamic_color),
                subtitle = stringResource(Res.string.settings_subtitle_theme_dynamic_color),
                visible = themeDynamicColorDependency,
            )
            // Theme Color Settings
            if (!settings.themeDynamicColor || !currentPlatform().supportDynamicColor()) {
                PreferenceColor(
                    value = Color(settings.themeColor),
                    onValueChange = {
                        viewModel.updateThemeColor(it.toArgb())
                    },
                    title = stringResource(Res.string.settings_title_theme_color)
                )
            }
            // Current selected schedule settings
            val selectedScheduleDependency = Dependency.State(settingsState) {
                it.schedule.values.any { !it.deleted }
            }
            PreferenceList(
                value = settings.selectedSchedule,
                onValueChange = {
                    viewModel.updateSelectedSchedule(it)
                },
                items = settings.schedule.filter { !it.value.deleted }.keys.toList(),
                itemTextProvider = { settings.schedule[it]?.name ?: "" },
                title = stringResource(Res.string.settings_title_selected_schedule),
                subtitle =
                    if (!settings.schedule.values.any { !it.deleted })
                        stringResource(Res.string.settings_subtitle_schedule_empty)
                    else
                        null,
                enabled = selectedScheduleDependency
            )
            // Add New Schedule
            val stringScheduleNameEmpty = stringResource(Res.string.settings_warning_schedule_name_empty)
            PreferenceInputText(
                value = "",
                onValueChange = { newScheduleName ->
                    if (newScheduleName.isNotEmpty()) {
                        val newSchedule = Schedule(
                            name = newScheduleName
                        )
                        viewModel.createSchedule(
                            newSchedule
                        )
                    }
                },
                title = stringResource(Res.string.settings_title_create_schedule),
                subtitle = stringResource(Res.string.settings_subtitle_create_schedule),
                validator = rememberDialogInputValidator(
                    validate = {
                        if (it.isNotEmpty())
                            DialogInputValidator.Result.Valid
                        else
                            DialogInputValidator.Result.Error(stringScheduleNameEmpty)
                    }
                )
            )
        }
        PreferenceDivider()
        // Schedule Settings
        val scheduleDependency = Dependency.State(settingsState) {
            it.selectedSchedule.isNotEmpty()
        }
        PreferenceSection(
            title = stringResource(Res.string.settings_title_schedule),
            subtitle =
                if (settings.selectedSchedule.isNotEmpty())
                    null
                else if (!settings.schedule.values.any { !it.deleted })
                    stringResource(Res.string.settings_subtitle_schedule_empty)
                else
                    stringResource(Res.string.settings_subtitle_schedule_not_selected),
            enabled = scheduleDependency
        ) {
            // Schedule Name
            PreferenceInputText(
                value = settings.schedule[settings.selectedSchedule]?.name ?: "",
                onValueChange = { newName ->
                    val currentSchedule = settings.schedule[settings.selectedSchedule]
                    if (currentSchedule != null) {
                        viewModel.updateSchedule(
                            schedule = currentSchedule.copy(name = newName)
                        )
                    }
                },
                title = stringResource(Res.string.settings_title_schedule_name),
                enabled = scheduleDependency
            )
            // Term Start and End Dates
            PreferenceDate(
                value = settings.schedule[settings.selectedSchedule]?.termStartDate?.toLocalDate()
                    ?: Schedule.defaultTermStartDate().toLocalDate(),
                onValueChange = { newDate ->
                    val currentSchedule = settings.schedule[settings.selectedSchedule]
                    if (currentSchedule != null) {
                        viewModel.updateSchedule(
                            schedule = currentSchedule.copy(termStartDate = Date.fromLocalDate(newDate))
                        )
                    }
                },
                title = stringResource(Res.string.settings_title_schedule_term_start_date),
                enabled = scheduleDependency
            )
            PreferenceDate(
                value = settings.schedule[settings.selectedSchedule]?.termEndDate?.toLocalDate()
                    ?: Schedule.defaultTermEndDate().toLocalDate(),
                onValueChange = { newDate ->
                    val currentSchedule = settings.schedule[settings.selectedSchedule]
                    if (currentSchedule != null) {
                        viewModel.updateSchedule(
                            schedule = currentSchedule.copy(termEndDate = Date.fromLocalDate(newDate))
                        )
                    }
                },
                title = stringResource(Res.string.settings_title_schedule_term_end_date),
                enabled = scheduleDependency
            )
            // Term Weeks
            PreferenceNumber(
                style =
                    if (currentPlatform().isDesktop())
                        PreferenceNumberStyle.TextField()
                    else
                        PreferenceNumberStyle.Wheel,
                value = settings.schedule[settings.selectedSchedule]?.totalWeeks()
                    ?: 16,
                min = 1,
                max = 60,
                onValueChange = {
                    val currentSchedule = settings.schedule[settings.selectedSchedule]
                    if (currentSchedule != null) {
                        val newEndDate = currentSchedule.termStartDate.addWeeks(it)
                        viewModel.updateSchedule(
                            schedule = currentSchedule.copy(termEndDate = newEndDate)
                        )
                    }
                },
                title = stringResource(Res.string.settings_title_schedule_total_weeks),
                enabled = scheduleDependency,
            )
            // Lessons Per Day Settings
            BasePreference(
                title = stringResource(Res.string.settings_title_schedule_lessons_per_day),
                subtitle = stringResource(Res.string.settings_subtitle_schedule_lessons_per_day),
                onClick = {
                    navHostController.navigate(SettingsDestination.LessonsPerDay.name)
                },
                enabled = scheduleDependency
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                    contentDescription = null
                )
            }
            PreferenceBool(
                style = PreferenceBoolStyle.Style.Switch,
                value = settings.schedule[settings.selectedSchedule]?.displayWeekends == true,
                onValueChange = {
                    val currentSchedule = settings.schedule[settings.selectedSchedule]
                    if (currentSchedule != null) {
                        viewModel.updateSchedule(
                            schedule = currentSchedule.copy(displayWeekends = it)
                        )
                    }
                },
                title = stringResource(Res.string.settings_title_display_weekends),
                enabled = scheduleDependency
            )
        }
        Spacer(
            modifier = Modifier.height(
                maxOf(
                    WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding(),
                    24.dp
                )
            )
        )
    }
}
