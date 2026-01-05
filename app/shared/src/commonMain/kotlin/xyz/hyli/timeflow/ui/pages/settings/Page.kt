/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
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
import xyz.hyli.timeflow.BuildConfig
import xyz.hyli.timeflow.data.Date
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.ThemeMode
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.ui.components.*
import xyz.hyli.timeflow.ui.navigation.Destination
import xyz.hyli.timeflow.ui.theme.LocalThemeIsDark
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.Files.settingsFilePath
import xyz.hyli.timeflow.utils.Files.showFileInFileManager
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop
import xyz.hyli.timeflow.utils.supportDynamicColor
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val settingsState = viewModel.settings.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val uriHandler = LocalUriHandler.current
    CustomScaffold(
        modifier = Modifier.fillMaxSize(),
        title = {
            Text(
                text = stringResource(Res.string.page_settings),
                style = MaterialTheme.typography.titleLarge
            )
        }
    ) {
        PreferenceScreen(
            modifier = Modifier
                .fillMaxWidth()
                .bottomPadding()
        ) {
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
                    value = settings.themeMode,
                    onValueChange = {
                        viewModel.updateTheme(it)
                    },
                    items = ThemeMode.entries,
                    itemTextProvider = { themeList[ThemeMode.entries.indexOf(it)] },
                    title = stringResource(Res.string.settings_title_theme)
                )
                // Dynamic Color Settings
                val themeDynamicColorDependency =
                    Dependency.State(mutableStateOf(currentPlatform())) {
                        it.supportDynamicColor()
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
                    !it.isScheduleEmpty
                }
                BasePreference(
                    trailingContent = {
                        Text(
                            text = settings.selectedSchedule?.name ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    title = stringResource(Res.string.settings_title_selected_schedule),
                    subtitle =
                        if (settings.isScheduleEmpty)
                            stringResource(Res.string.settings_subtitle_schedule_empty)
                        else
                            null,
                    enabled = selectedScheduleDependency,
                    onClick = {
                        navHostController.navigate(Destination.Schedule.ScheduleList)
                    }
                )
                // Add New Schedule
                val stringScheduleNameEmpty =
                    stringResource(Res.string.settings_warning_schedule_name_empty)
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
                it.isScheduleSelected
            }
            PreferenceSection(
                title = stringResource(Res.string.settings_title_schedule),
                subtitle =
                    if (settings.isScheduleSelected)
                        null
                    else if (settings.isScheduleEmpty)
                        stringResource(Res.string.settings_subtitle_schedule_empty)
                    else
                        stringResource(Res.string.settings_subtitle_schedule_not_selected),
                enabled = scheduleDependency
            ) {
                val schedule by viewModel.selectedSchedule.collectAsState()
                // Schedule Name
                PreferenceInputText(
                    value = settings.schedules[settings.selectedScheduleID]?.name ?: "",
                    onValueChange = { newName ->
                        viewModel.updateSchedule(
                            schedule = schedule!!.copy(name = newName)
                        )
                    },
                    title = stringResource(Res.string.settings_title_schedule_name),
                    enabled = scheduleDependency
                )
                // Term Start and End Dates
                PreferenceDate(
                    value = schedule?.termStartDate?.toLocalDate()
                        ?: Schedule.defaultTermStartDate().toLocalDate(),
                    onValueChange = { newDate ->
                        val newTermStartDate = Date.fromLocalDate(newDate)
                        val currentTermEndDate = schedule!!.termEndDate
                        val newTermEndDate =
                            if (newTermStartDate.weeksTill(currentTermEndDate) in 1..60) {
                                currentTermEndDate
                            } else newTermStartDate.addWeeks(schedule!!.totalWeeks)
                        viewModel.updateSchedule(
                            schedule = schedule!!.copy(
                                termStartDate = newTermStartDate,
                                termEndDate = newTermEndDate
                            )
                        )
                    },

                    title = stringResource(Res.string.settings_title_schedule_term_start_date),
                    enabled = scheduleDependency
                )
                PreferenceDate(
                    value = schedule?.termEndDate?.toLocalDate()
                        ?: Schedule.defaultTermEndDate().toLocalDate(),
                    onValueChange = { newDate ->
                        viewModel.updateSchedule(
                            schedule = schedule!!.copy(
                                termEndDate = Date.fromLocalDate(
                                    newDate
                                )
                            )
                        )
                    },
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            val termStartDate = schedule?.termStartDate
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
                    value = schedule?.totalWeeks
                        ?: 16,
                    min = 1,
                    max = 60,
                    onValueChange = {
                        val newEndDate = schedule!!.termStartDate.addWeeks(it)
                        viewModel.updateSchedule(
                            schedule = schedule!!.copy(termEndDate = newEndDate)
                        )
                    },
                    title = stringResource(Res.string.settings_title_schedule_total_weeks),
                    enabled = scheduleDependency,
                )
                // Lessons Per Day Settings
                BasePreference(
                    title = stringResource(Res.string.settings_title_schedule_lessons_per_day),
                    subtitle = stringResource(Res.string.settings_subtitle_schedule_lessons_per_day),
                    onClick = {
                        navHostController.navigate(Destination.Settings.LessonsPerDay)
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
                    value = schedule?.displayWeekends == true,
                    onValueChange = {
                        viewModel.updateSchedule(
                            schedule = schedule!!.copy(displayWeekends = it)
                        )
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
                        navHostController.navigate(Destination.Settings.About)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                        contentDescription = null
                    )
                }
                val configPathDependency =
                    Dependency.State(mutableStateOf(currentPlatform())) {
                        it.isDesktop()
                    }
                BasePreference(
                    title = stringResource(Res.string.settings_title_config_path),
                    subtitle = settingsFilePath,
                    onClick = {
                        settingsFilePath?.let { showFileInFileManager?.invoke(it) }
                    },
                    visible = configPathDependency
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Launch,
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
                    Text("${stringResource(Res.string.settings_value_version)}: ${BuildConfig.APP_VERSION_NAME}-${BuildConfig.GIT_COMMIT_HASH}")
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
        }
    }
}
