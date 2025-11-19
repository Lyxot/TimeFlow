package xyz.hyli.timeflow.ui.pages.settings.subpage

import androidx.collection.IntSet
import androidx.collection.MutableIntSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.back
import timeflow.composeapp.generated.resources.save
import timeflow.composeapp.generated.resources.schedule_value_course_time
import timeflow.composeapp.generated.resources.settings_title_lessons_per_day
import timeflow.composeapp.generated.resources.settings_title_lessons_time_afternoon
import timeflow.composeapp.generated.resources.settings_title_lessons_time_evening
import timeflow.composeapp.generated.resources.settings_title_lessons_time_morning
import timeflow.composeapp.generated.resources.settings_title_schedule_lessons_per_day_afternoon
import timeflow.composeapp.generated.resources.settings_title_schedule_lessons_per_day_evening
import timeflow.composeapp.generated.resources.settings_title_schedule_lessons_per_day_morning
import timeflow.composeapp.generated.resources.settings_warning_lessons_per_day_empty
import timeflow.composeapp.generated.resources.settings_warning_lessons_time_conflict
import xyz.hyli.timeflow.datastore.Lesson
import xyz.hyli.timeflow.datastore.LessonTimePeriodInfo
import xyz.hyli.timeflow.datastore.Time
import xyz.hyli.timeflow.ui.components.BasePreference
import xyz.hyli.timeflow.ui.components.PreferenceDivider
import xyz.hyli.timeflow.ui.components.PreferenceNumber
import xyz.hyli.timeflow.ui.components.PreferenceNumberStyle
import xyz.hyli.timeflow.ui.components.PreferenceScreen
import xyz.hyli.timeflow.ui.components.PreferenceSection
import xyz.hyli.timeflow.ui.components.TimePeriodPickerDialog
import xyz.hyli.timeflow.ui.components.TimePeriodPickerStyle
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.theme.NotoSans
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop

@Composable
fun LessonsPerDayScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val settings by viewModel.settings.collectAsState()
    val lessonTimePeriodInfo = remember {
        mutableStateOf(
            settings.schedule[settings.selectedSchedule]?.lessonTimePeriodInfo
                ?: LessonTimePeriodInfo.fromPeriodCounts()
        )
    }
    val morningCount = remember { mutableStateOf(lessonTimePeriodInfo.value.morning.size) }
    val afternoonCount = remember { mutableStateOf(lessonTimePeriodInfo.value.afternoon.size) }
    val eveningCount = remember { mutableStateOf(lessonTimePeriodInfo.value.evening.size) }
    val isModified = remember { mutableStateOf(false) }
    val conflictSet = lessonsPerDayValidator(lessonTimePeriodInfo.value)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .then(
                if (currentPlatform().isDesktop())
                    Modifier.padding(start = 16.dp, top = 16.dp)
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .then(
                    if (currentPlatform().isDesktop())
                        Modifier.padding(end = 16.dp)
                    else Modifier
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    navHostController.popBackStack()
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(Res.string.back)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if ((morningCount.value + afternoonCount.value + eveningCount.value) == 0) {
                Text(
                    text = stringResource(Res.string.settings_warning_lessons_per_day_empty),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            } else if (conflictSet.size > 0) {
                Text(
                    text = stringResource(Res.string.settings_warning_lessons_time_conflict),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (isModified.value) {
                IconButton(
                    onClick = {
                        val currentSchedule = settings.schedule[settings.selectedSchedule]
                        if (currentSchedule != null) {
                            viewModel.updateSchedule(
                                schedule = currentSchedule.copy(lessonTimePeriodInfo = lessonTimePeriodInfo.value)
                            )
                            navHostController.popBackStack()
                        }
                    },
                    enabled = conflictSet.size == 0 &&
                            (morningCount.value + afternoonCount.value + eveningCount.value) > 0,
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = stringResource(Res.string.save)
                    )
                }
            }
        }
        PreferenceScreen(
            modifier = Modifier.fillMaxWidth()
                .then(
                    if (currentPlatform().isDesktop())
                        Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
                    else Modifier
                )
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
}

private fun lessonsPerDayValidator(
    lessonTimePeriodInfo: LessonTimePeriodInfo
): IntSet {
    val lessons =
        lessonTimePeriodInfo.morning + lessonTimePeriodInfo.afternoon + lessonTimePeriodInfo.evening
    val conflictSet = MutableIntSet()
    for (i in 1 until lessons.size) {
        if (lessons[i].start < lessons[i - 1].end) { // Overlapping lessons
            conflictSet.add(i - 1)
            conflictSet.add(i)
        }
    }
    return conflictSet
}