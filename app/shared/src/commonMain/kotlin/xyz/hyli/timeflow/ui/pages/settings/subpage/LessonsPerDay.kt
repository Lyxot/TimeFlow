/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.settings.subpage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.data.Lesson
import xyz.hyli.timeflow.data.LessonTimePeriodInfo
import xyz.hyli.timeflow.data.Time
import xyz.hyli.timeflow.shared.generated.resources.Res
import xyz.hyli.timeflow.shared.generated.resources.save
import xyz.hyli.timeflow.shared.generated.resources.schedule_value_course_time
import xyz.hyli.timeflow.shared.generated.resources.settings_title_lessons_per_day
import xyz.hyli.timeflow.shared.generated.resources.settings_title_lessons_time_afternoon
import xyz.hyli.timeflow.shared.generated.resources.settings_title_lessons_time_evening
import xyz.hyli.timeflow.shared.generated.resources.settings_title_lessons_time_morning
import xyz.hyli.timeflow.shared.generated.resources.settings_title_schedule_lessons_per_day
import xyz.hyli.timeflow.shared.generated.resources.settings_title_schedule_lessons_per_day_afternoon
import xyz.hyli.timeflow.shared.generated.resources.settings_title_schedule_lessons_per_day_evening
import xyz.hyli.timeflow.shared.generated.resources.settings_title_schedule_lessons_per_day_morning
import xyz.hyli.timeflow.shared.generated.resources.settings_warning_lessons_per_day_empty
import xyz.hyli.timeflow.shared.generated.resources.settings_warning_lessons_time_conflict
import xyz.hyli.timeflow.ui.components.BasePreference
import xyz.hyli.timeflow.ui.components.CustomScaffold
import xyz.hyli.timeflow.ui.components.NavigationBackIcon
import xyz.hyli.timeflow.ui.components.PreferenceDivider
import xyz.hyli.timeflow.ui.components.PreferenceNumber
import xyz.hyli.timeflow.ui.components.PreferenceNumberStyle
import xyz.hyli.timeflow.ui.components.PreferenceScreen
import xyz.hyli.timeflow.ui.components.PreferenceSection
import xyz.hyli.timeflow.ui.components.TimePeriodPickerDialog
import xyz.hyli.timeflow.ui.components.TimePeriodPickerStyle
import xyz.hyli.timeflow.ui.components.bottomPadding
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.theme.NotoSans
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsPerDayScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val schedule by viewModel.selectedSchedule.collectAsState()
    val lessonTimePeriodInfo = remember {
        mutableStateOf(
            schedule?.lessonTimePeriodInfo
                ?: LessonTimePeriodInfo.fromPeriodCounts()
        )
    }
    val morningCount = remember { mutableStateOf(lessonTimePeriodInfo.value.morning.size) }
    val afternoonCount = remember { mutableStateOf(lessonTimePeriodInfo.value.afternoon.size) }
    val eveningCount = remember { mutableStateOf(lessonTimePeriodInfo.value.evening.size) }
    val isModified = remember { mutableStateOf(false) }
    val conflictSet = lessonTimePeriodInfo.value.conflictSet
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(lessonTimePeriodInfo.value) {
        if ((morningCount.value + afternoonCount.value + eveningCount.value) == 0) {
            snackbarHostState.showSnackbar(
                message = getString(Res.string.settings_warning_lessons_per_day_empty),
                duration = SnackbarDuration.Indefinite
            )
        } else if (conflictSet.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = getString(Res.string.settings_warning_lessons_time_conflict),
                duration = SnackbarDuration.Indefinite
            )
        }
    }

    CustomScaffold(
        modifier = Modifier.fillMaxSize(),
        title = {
            Text(
                stringResource(Res.string.settings_title_schedule_lessons_per_day)
            )
        },
        navigationIcon = {
            NavigationBackIcon(navHostController)
        },
        actions = {
            if (isModified.value) {
                IconButton(
                    onClick = {
                        viewModel.updateSchedule(
                            schedule = schedule!!.copy(lessonTimePeriodInfo = lessonTimePeriodInfo.value)
                        )
                        navHostController.popBackStack()
                    },
                    enabled = conflictSet.isEmpty() &&
                            (morningCount.value + afternoonCount.value + eveningCount.value) > 0,
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = stringResource(Res.string.save)
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.fillMaxWidth(0.75f)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        PreferenceScreen(
            modifier = Modifier
                .fillMaxWidth()
                .bottomPadding()
        ) {
            PreferenceSection(
                title = stringResource(Res.string.settings_title_lessons_per_day)
            ) {
                // Morning, Afternoon, and Evening Lesson Counts
                PreferenceNumber(
                    style = PreferenceNumberStyle.Slider(true),
                    value = morningCount.value,
                    onValueChange = { newCount ->
                        morningCount.value = newCount.coerceIn(0, 10)
                        lessonTimePeriodInfo.value = lessonTimePeriodInfo.value.copy(
                            morning = LessonTimePeriodInfo.generateLessons(
                                newCount,
                                lessonTimePeriodInfo.value.morning.getOrNull(0)?.start ?: Time(
                                    8,
                                    0
                                ),
                                lessonTimePeriodInfo.value.morning.getOrNull(0)?.end?.minutesSince(
                                    lessonTimePeriodInfo.value.morning[0].start
                                ) ?: 40,
                                lessonTimePeriodInfo.value.morning.getOrNull(1)?.start?.minutesSince(
                                    lessonTimePeriodInfo.value.morning[0].end
                                ) ?: 10
                            )
                        )
                        isModified.value = true
                    },
                    min = 0,
                    max = 10,
                    stepSize = 1,
                    title = stringResource(Res.string.settings_title_schedule_lessons_per_day_morning) + ":\t${morningCount.value}",
                )
                PreferenceNumber(
                    style = PreferenceNumberStyle.Slider(true),
                    value = afternoonCount.value,
                    onValueChange = { newCount ->
                        afternoonCount.value = newCount.coerceIn(0, 10)
                        lessonTimePeriodInfo.value = lessonTimePeriodInfo.value.copy(
                            afternoon = LessonTimePeriodInfo.generateLessons(
                                newCount,
                                lessonTimePeriodInfo.value.afternoon.getOrNull(0)?.start ?: Time(
                                    13,
                                    0
                                ),
                                lessonTimePeriodInfo.value.afternoon.getOrNull(0)?.end?.minutesSince(
                                    lessonTimePeriodInfo.value.afternoon[0].start
                                ) ?: 40,
                                lessonTimePeriodInfo.value.afternoon.getOrNull(1)?.start?.minutesSince(
                                    lessonTimePeriodInfo.value.afternoon[0].end
                                ) ?: 10
                            )
                        )
                        isModified.value = true
                    },
                    min = 0,
                    max = 10,
                    stepSize = 1,
                    title = stringResource(Res.string.settings_title_schedule_lessons_per_day_afternoon) + ":\t${afternoonCount.value}",
                )
                PreferenceNumber(
                    style = PreferenceNumberStyle.Slider(true),
                    value = eveningCount.value,
                    onValueChange = { newCount ->
                        eveningCount.value = newCount.coerceIn(0, 10)
                        lessonTimePeriodInfo.value = lessonTimePeriodInfo.value.copy(
                            evening = LessonTimePeriodInfo.generateLessons(
                                newCount,
                                lessonTimePeriodInfo.value.evening.getOrNull(0)?.start ?: Time(
                                    18,
                                    0
                                ),
                                lessonTimePeriodInfo.value.evening.getOrNull(0)?.end?.minutesSince(
                                    lessonTimePeriodInfo.value.evening[0].start
                                ) ?: 40,
                                lessonTimePeriodInfo.value.evening.getOrNull(1)?.start?.minutesSince(
                                    lessonTimePeriodInfo.value.evening[0].end
                                ) ?: 10
                            )
                        )
                        isModified.value = true
                    },
                    min = 0,
                    max = 10,
                    stepSize = 1,
                    title = stringResource(Res.string.settings_title_schedule_lessons_per_day_evening) + ":\t${eveningCount.value}",
                )
            }

            // Lessons Time Settings
            if (morningCount.value > 0) {
                PreferenceDivider()
                PreferenceSection(
                    title = stringResource(Res.string.settings_title_lessons_time_morning)
                ) {
                    for (i in 0 until morningCount.value) {
                        val lesson = lessonTimePeriodInfo.value.morning[i]
                        val dialogState = rememberDialogState()
                        BasePreference(
                            title = stringResource(Res.string.schedule_value_course_time, i + 1),
                            onClick = { dialogState.show() }
                        ) {
                            if (i in conflictSet) {
                                Text(
                                    text = lesson.start.toString() + " - " + lesson.end.toString(),
                                    color = MaterialTheme.colorScheme.error,
                                    fontFamily = NotoSans,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                )
                            } else {
                                Text(
                                    text = lesson.start.toString() + " - " + lesson.end.toString(),
                                    fontFamily = NotoSans,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                )
                            }
                            if (dialogState.visible) {
                                TimePeriodPickerDialog(
                                    style =
                                        if (currentPlatform().isDesktop())
                                            TimePeriodPickerStyle.TextField
                                        else
                                            TimePeriodPickerStyle.Wheel,
                                    state = dialogState,
                                    initStartTime = lesson.start,
                                    initEndTime = lesson.end,
                                    onTimePeriodChange = { startTime, endTime ->
                                        if (i > 0) {
                                            if (startTime < lessonTimePeriodInfo.value.morning[i - 1].end) {
                                                lessonTimePeriodInfo.value =
                                                    lessonTimePeriodInfo.value.copy(
                                                        morning = lessonTimePeriodInfo.value.morning.toMutableList()
                                                            .apply {
                                                                this[i] = Lesson(
                                                                    start = startTime,
                                                                    end = endTime
                                                                )
                                                            }
                                                    )
                                                return@TimePeriodPickerDialog
                                            }
                                        }
                                        val updatedLessons =
                                            LessonTimePeriodInfo.generateLessons(
                                                morningCount.value - i,
                                                startTime,
                                                endTime.minutesSince(startTime),
                                                if (i == 0) 10 else startTime.minutesSince(
                                                    lessonTimePeriodInfo.value.morning[i - 1].end
                                                )
                                            )
                                        lessonTimePeriodInfo.value =
                                            lessonTimePeriodInfo.value.copy(
                                                morning = lessonTimePeriodInfo.value.morning.toMutableList()
                                                    .subList(0, i) + updatedLessons
                                            )
                                        isModified.value = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
            if (afternoonCount.value > 0) {
                PreferenceDivider()
                PreferenceSection(
                    title = stringResource(Res.string.settings_title_lessons_time_afternoon),
                ) {
                    for (i in 0 until afternoonCount.value) {
                        val lesson = lessonTimePeriodInfo.value.afternoon[i]
                        val dialogState = rememberDialogState()
                        BasePreference(
                            title = stringResource(
                                Res.string.schedule_value_course_time,
                                i + morningCount.value + 1
                            ),
                            onClick = { dialogState.show() }
                        ) {
                            if (i + morningCount.value in conflictSet) {
                                Text(
                                    text = lesson.start.toString() + " - " + lesson.end.toString(),
                                    color = MaterialTheme.colorScheme.error,
                                    fontFamily = NotoSans,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                )
                            } else {
                                Text(
                                    text = lesson.start.toString() + " - " + lesson.end.toString(),
                                    fontFamily = NotoSans,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                )
                            }
                            if (dialogState.visible) {
                                TimePeriodPickerDialog(
                                    style =
                                        if (currentPlatform().isDesktop())
                                            TimePeriodPickerStyle.TextField
                                        else
                                            TimePeriodPickerStyle.Wheel,
                                    state = dialogState,
                                    initStartTime = lesson.start,
                                    initEndTime = lesson.end,
                                    onTimePeriodChange = { startTime, endTime ->
                                        if (i > 0) {
                                            if (startTime < lessonTimePeriodInfo.value.afternoon[i - 1].end) {
                                                lessonTimePeriodInfo.value =
                                                    lessonTimePeriodInfo.value.copy(
                                                        afternoon = lessonTimePeriodInfo.value.afternoon.toMutableList()
                                                            .apply {
                                                                this[i] = Lesson(
                                                                    start = startTime,
                                                                    end = endTime
                                                                )
                                                            }
                                                    )
                                                return@TimePeriodPickerDialog
                                            }
                                        }
                                        val updatedLessons =
                                            LessonTimePeriodInfo.generateLessons(
                                                afternoonCount.value - i,
                                                startTime,
                                                endTime.minutesSince(startTime),
                                                if (i == 0) 10 else startTime.minutesSince(
                                                    lessonTimePeriodInfo.value.afternoon[i - 1].end
                                                )
                                            )
                                        lessonTimePeriodInfo.value =
                                            lessonTimePeriodInfo.value.copy(
                                                afternoon = lessonTimePeriodInfo.value.afternoon.toMutableList()
                                                    .subList(0, i) + updatedLessons
                                            )
                                        isModified.value = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
            if (eveningCount.value > 0) {
                PreferenceDivider()
                PreferenceSection(
                    title = stringResource(Res.string.settings_title_lessons_time_evening)
                ) {
                    for (i in 0 until eveningCount.value) {
                        val lesson = lessonTimePeriodInfo.value.evening[i]
                        val dialogState = rememberDialogState()
                        BasePreference(
                            title = stringResource(
                                Res.string.schedule_value_course_time,
                                i + morningCount.value + afternoonCount.value + 1
                            ),
                            onClick = { dialogState.show() }
                        ) {
                            if (i + morningCount.value + afternoonCount.value in conflictSet) {
                                Text(
                                    text = lesson.start.toString() + " - " + lesson.end.toString(),
                                    color = MaterialTheme.colorScheme.error,
                                    fontFamily = NotoSans,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                )
                            } else {
                                Text(
                                    text = lesson.start.toString() + " - " + lesson.end.toString(),
                                    fontFamily = NotoSans,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                )
                            }
                            if (dialogState.visible) {
                                TimePeriodPickerDialog(
                                    style =
                                        if (currentPlatform().isDesktop())
                                            TimePeriodPickerStyle.TextField
                                        else
                                            TimePeriodPickerStyle.Wheel,
                                    state = dialogState,
                                    initStartTime = lesson.start,
                                    initEndTime = lesson.end,
                                    onTimePeriodChange = { startTime, endTime ->
                                        if (i > 0) {
                                            if (startTime < lessonTimePeriodInfo.value.evening[i - 1].end) {
                                                lessonTimePeriodInfo.value =
                                                    lessonTimePeriodInfo.value.copy(
                                                        evening = lessonTimePeriodInfo.value.evening.toMutableList()
                                                            .apply {
                                                                this[i] = Lesson(
                                                                    start = startTime,
                                                                    end = endTime
                                                                )
                                                            }
                                                    )
                                                return@TimePeriodPickerDialog
                                            }
                                        }
                                        val updatedLessons =
                                            LessonTimePeriodInfo.generateLessons(
                                                eveningCount.value - i,
                                                startTime,
                                                endTime.minutesSince(startTime),
                                                if (i == 0) 10 else startTime.minutesSince(
                                                    lessonTimePeriodInfo.value.evening[i - 1].end
                                                )
                                            )
                                        lessonTimePeriodInfo.value =
                                            lessonTimePeriodInfo.value.copy(
                                                evening = lessonTimePeriodInfo.value.evening.toMutableList()
                                                    .subList(0, i) + updatedLessons
                                            )
                                        isModified.value = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
