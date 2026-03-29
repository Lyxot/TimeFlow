/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.data.Lesson
import xyz.hyli.timeflow.data.LessonTimePeriodInfo
import xyz.hyli.timeflow.data.Time
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.ui.components.*
import xyz.hyli.timeflow.ui.theme.NotoSans
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop

/**
 * LessonsPerDay screen bound to ViewModel (used from Settings page).
 */
@Composable
fun LessonsPerDayScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val schedule by viewModel.selectedSchedule.collectAsState()
    LessonsPerDayContent(
        initialInfo = schedule?.lessonTimePeriodInfo ?: LessonTimePeriodInfo.fromPeriodCounts(),
        navHostController = navHostController,
        onSave = { info ->
            viewModel.updateSchedule(schedule = schedule!!.copy(lessonTimePeriodInfo = info))
        }
    )
}

/**
 * ViewModel-independent LessonsPerDay screen.
 * Can be used from both Settings and AI Preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsPerDayContent(
    initialInfo: LessonTimePeriodInfo,
    navHostController: NavHostController,
    onSave: (LessonTimePeriodInfo) -> Unit
) {
    val lessonTimePeriodInfo = remember { mutableStateOf(initialInfo) }
    val morningCount = remember { mutableStateOf(initialInfo.morning.size) }
    val afternoonCount = remember { mutableStateOf(initialInfo.afternoon.size) }
    val eveningCount = remember { mutableStateOf(initialInfo.evening.size) }
    val isModified = remember { mutableStateOf(false) }
    val conflictSet = lessonTimePeriodInfo.value.conflictSet
    val snackbarHostState = LocalSnackbarHostState.current
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
                        onSave(lessonTimePeriodInfo.value)
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
            AnimatedSnackbarHost(
                hostState = snackbarHostState
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
                LessonTimeSection(
                    title = stringResource(Res.string.settings_title_lessons_time_morning),
                    lessons = lessonTimePeriodInfo.value.morning,
                    count = morningCount.value,
                    indexOffset = 0,
                    conflictSet = conflictSet,
                    onUpdate = { updatedLessons ->
                        lessonTimePeriodInfo.value = lessonTimePeriodInfo.value.copy(morning = updatedLessons)
                        isModified.value = true
                    }
                )
            }
            if (afternoonCount.value > 0) {
                PreferenceDivider()
                LessonTimeSection(
                    title = stringResource(Res.string.settings_title_lessons_time_afternoon),
                    lessons = lessonTimePeriodInfo.value.afternoon,
                    count = afternoonCount.value,
                    indexOffset = morningCount.value,
                    conflictSet = conflictSet,
                    onUpdate = { updatedLessons ->
                        lessonTimePeriodInfo.value = lessonTimePeriodInfo.value.copy(afternoon = updatedLessons)
                        isModified.value = true
                    }
                )
            }
            if (eveningCount.value > 0) {
                PreferenceDivider()
                LessonTimeSection(
                    title = stringResource(Res.string.settings_title_lessons_time_evening),
                    lessons = lessonTimePeriodInfo.value.evening,
                    count = eveningCount.value,
                    indexOffset = morningCount.value + afternoonCount.value,
                    conflictSet = conflictSet,
                    onUpdate = { updatedLessons ->
                        lessonTimePeriodInfo.value = lessonTimePeriodInfo.value.copy(evening = updatedLessons)
                        isModified.value = true
                    }
                )
            }
        }
    }
}

@Composable
private fun PreferenceScope.LessonTimeSection(
    title: String,
    lessons: List<Lesson>,
    count: Int,
    indexOffset: Int,
    conflictSet: Set<Int>,
    onUpdate: (List<Lesson>) -> Unit
) {
    PreferenceSection(title = title) {
        for (i in 0 until count) {
            val lesson = lessons[i]
            val dialogState = rememberDialogState()
            BasePreference(
                title = stringResource(Res.string.schedule_value_course_time, i + indexOffset + 1),
                onClick = { dialogState.show() }
            ) {
                val isConflict = (i + indexOffset) in conflictSet
                Text(
                    text = lesson.start.toString() + " - " + lesson.end.toString(),
                    color = if (isConflict) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
                    fontFamily = NotoSans,
                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                )
                if (dialogState.visible) {
                    TimePeriodPickerDialog(
                        style = if (currentPlatform().isDesktop())
                            TimePeriodPickerStyle.TextField
                        else
                            TimePeriodPickerStyle.Wheel,
                        state = dialogState,
                        initStartTime = lesson.start,
                        initEndTime = lesson.end,
                        onTimePeriodChange = { startTime, endTime ->
                            if (i > 0 && startTime < lessons[i - 1].end) {
                                onUpdate(
                                    lessons.toMutableList().apply {
                                        this[i] = Lesson(start = startTime, end = endTime)
                                    }
                                )
                                return@TimePeriodPickerDialog
                            }
                            val updatedLessons = LessonTimePeriodInfo.generateLessons(
                                count - i,
                                startTime,
                                endTime.minutesSince(startTime),
                                if (i == 0) 10 else startTime.minutesSince(lessons[i - 1].end)
                            )
                            onUpdate(lessons.toMutableList().subList(0, i) + updatedLessons)
                        }
                    )
                }
            }
        }
    }
}
