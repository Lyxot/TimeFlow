/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.ic_launcher
import timeflow.composeapp.generated.resources.ic_launcher_night
import timeflow.composeapp.generated.resources.page_settings
import timeflow.composeapp.generated.resources.settings_subtitle_create_schedule
import timeflow.composeapp.generated.resources.settings_subtitle_schedule_empty
import timeflow.composeapp.generated.resources.settings_subtitle_schedule_lessons_per_day
import timeflow.composeapp.generated.resources.settings_subtitle_schedule_not_selected
import timeflow.composeapp.generated.resources.settings_subtitle_theme_dynamic_color
import timeflow.composeapp.generated.resources.settings_theme_dark
import timeflow.composeapp.generated.resources.settings_theme_light
import timeflow.composeapp.generated.resources.settings_theme_system
import timeflow.composeapp.generated.resources.settings_title_about
import timeflow.composeapp.generated.resources.settings_title_changelog
import timeflow.composeapp.generated.resources.settings_title_create_schedule
import timeflow.composeapp.generated.resources.settings_title_display_weekends
import timeflow.composeapp.generated.resources.settings_title_donate
import timeflow.composeapp.generated.resources.settings_title_feedback
import timeflow.composeapp.generated.resources.settings_title_general
import timeflow.composeapp.generated.resources.settings_title_other
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
import timeflow.composeapp.generated.resources.settings_value_version
import timeflow.composeapp.generated.resources.settings_warning_schedule_name_empty
import timeflow.composeapp.generated.resources.url_changelog
import timeflow.composeapp.generated.resources.url_donate
import timeflow.composeapp.generated.resources.url_feedback
import xyz.hyli.timeflow.BuildConfig
import xyz.hyli.timeflow.datastore.Date
import xyz.hyli.timeflow.datastore.Schedule
import xyz.hyli.timeflow.ui.components.BasePreference
import xyz.hyli.timeflow.ui.components.Dependency
import xyz.hyli.timeflow.ui.components.DialogInputValidator
import xyz.hyli.timeflow.ui.components.MILLIS_PER_DAY
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
import xyz.hyli.timeflow.ui.theme.LocalThemeIsDark
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop
import xyz.hyli.timeflow.utils.isMacOS
import xyz.hyli.timeflow.utils.supportDynamicColor
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Composable
fun SettingsScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val settingsState = viewModel.settings.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val uriHandler = LocalUriHandler.current
    PreferenceScreen(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (currentPlatform().isMacOS())
                    Modifier.padding(vertical = 16.dp)
                else Modifier
            )
    ) {
        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = stringResource(Res.string.page_settings),
            style = MaterialTheme.typography.titleLarge
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
                        val newTermStartDate = Date.fromLocalDate(newDate)
                        val currentTermEndDate = currentSchedule.termEndDate
                        val newTermEndDate =
                            if (newTermStartDate.weeksTill(currentTermEndDate) in 1..60) {
                                currentTermEndDate
                            } else newTermStartDate.addWeeks(currentSchedule.totalWeeks())
                        viewModel.updateSchedule(
                            schedule = currentSchedule.copy(
                                termStartDate = newTermStartDate,
                                termEndDate = newTermEndDate
                            )
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
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val currentSchedule = settings.schedule[settings.selectedSchedule]
                        val termStartDate = currentSchedule?.termStartDate
                            ?: Schedule.defaultTermStartDate()
                        val epochDays = (utcTimeMillis / (MILLIS_PER_DAY)).toInt()
                        val date = LocalDate.fromEpochDays(epochDays)
                        return termStartDate.weeksTill(date) in 1..60
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
        PreferenceDivider()
        // Other
        PreferenceSection(
            title = stringResource(Res.string.settings_title_other)
        ) {
            BasePreference(
                title = stringResource(Res.string.settings_title_about),
                onClick = {
                    navHostController.navigate(SettingsDestination.About.name)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                    contentDescription = null
                )
            }
            val urlDonate = stringResource(Res.string.url_donate)
            BasePreference(
                title = stringResource(Res.string.settings_title_donate),
                onClick = {
                    uriHandler.openUri(urlDonate)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Launch,
                    contentDescription = null
                )
            }
            val urlFeedback = stringResource(Res.string.url_feedback)
            BasePreference(
                title = stringResource(Res.string.settings_title_feedback),
                onClick = {
                    uriHandler.openUri(urlFeedback)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Launch,
                    contentDescription = null
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isDark by LocalThemeIsDark.current
            Icon(
                modifier = Modifier
                    .size(192.dp)
                    .clip(RoundedCornerShape(45.dp)),
                imageVector = vectorResource(
                    if (isDark) Res.drawable.ic_launcher_night
                    else Res.drawable.ic_launcher
                ),
                contentDescription = null,
                tint = Color.Unspecified,
            )
            Text(
                text = BuildConfig.APP_NAME,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.headlineLarge
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("${stringResource(Res.string.settings_value_version)}: ${BuildConfig.APP_VERSION_NAME}(${BuildConfig.APP_VERSION_CODE})")
                Text(
                    text = "©️ ${
                        Instant.fromEpochMilliseconds(BuildConfig.BUILD_TIME).toLocalDateTime(
                            TimeZone.currentSystemDefault()
                        ).year
                    } ${BuildConfig.AUTHOR}"
                )
            }
            val urlChangelog =
                stringResource(Res.string.url_changelog, BuildConfig.APP_VERSION_NAME)
            ElevatedButton(
                onClick = {
                    uriHandler.openUri(urlChangelog)
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.settings_title_changelog))
                }
            }
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
