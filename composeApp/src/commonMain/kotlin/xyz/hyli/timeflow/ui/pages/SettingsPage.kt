package xyz.hyli.timeflow.ui.pages

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
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.back
import timeflow.composeapp.generated.resources.lesson_1
import timeflow.composeapp.generated.resources.lesson_10
import timeflow.composeapp.generated.resources.lesson_11
import timeflow.composeapp.generated.resources.lesson_12
import timeflow.composeapp.generated.resources.lesson_13
import timeflow.composeapp.generated.resources.lesson_14
import timeflow.composeapp.generated.resources.lesson_15
import timeflow.composeapp.generated.resources.lesson_16
import timeflow.composeapp.generated.resources.lesson_17
import timeflow.composeapp.generated.resources.lesson_18
import timeflow.composeapp.generated.resources.lesson_19
import timeflow.composeapp.generated.resources.lesson_2
import timeflow.composeapp.generated.resources.lesson_20
import timeflow.composeapp.generated.resources.lesson_21
import timeflow.composeapp.generated.resources.lesson_22
import timeflow.composeapp.generated.resources.lesson_23
import timeflow.composeapp.generated.resources.lesson_24
import timeflow.composeapp.generated.resources.lesson_25
import timeflow.composeapp.generated.resources.lesson_26
import timeflow.composeapp.generated.resources.lesson_27
import timeflow.composeapp.generated.resources.lesson_28
import timeflow.composeapp.generated.resources.lesson_29
import timeflow.composeapp.generated.resources.lesson_3
import timeflow.composeapp.generated.resources.lesson_30
import timeflow.composeapp.generated.resources.lesson_4
import timeflow.composeapp.generated.resources.lesson_5
import timeflow.composeapp.generated.resources.lesson_6
import timeflow.composeapp.generated.resources.lesson_7
import timeflow.composeapp.generated.resources.lesson_8
import timeflow.composeapp.generated.resources.lesson_9
import timeflow.composeapp.generated.resources.page_settings
import timeflow.composeapp.generated.resources.save
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
import timeflow.composeapp.generated.resources.settings_title_lessons_per_day
import timeflow.composeapp.generated.resources.settings_title_lessons_time_afternoon
import timeflow.composeapp.generated.resources.settings_title_lessons_time_evening
import timeflow.composeapp.generated.resources.settings_title_lessons_time_morning
import timeflow.composeapp.generated.resources.settings_title_schedule
import timeflow.composeapp.generated.resources.settings_title_schedule_lessons_per_day
import timeflow.composeapp.generated.resources.settings_title_schedule_lessons_per_day_afternoon
import timeflow.composeapp.generated.resources.settings_title_schedule_lessons_per_day_evening
import timeflow.composeapp.generated.resources.settings_title_schedule_lessons_per_day_morning
import timeflow.composeapp.generated.resources.settings_title_schedule_name
import timeflow.composeapp.generated.resources.settings_title_schedule_term_end_date
import timeflow.composeapp.generated.resources.settings_title_schedule_term_start_date
import timeflow.composeapp.generated.resources.settings_title_selected_schedule
import timeflow.composeapp.generated.resources.settings_title_theme
import timeflow.composeapp.generated.resources.settings_title_theme_color
import timeflow.composeapp.generated.resources.settings_title_theme_dynamic_color
import timeflow.composeapp.generated.resources.settings_warning_lessons_per_day_empty
import timeflow.composeapp.generated.resources.settings_warning_lessons_time_conflict
import timeflow.composeapp.generated.resources.settings_warning_schedule_name_empty
import xyz.hyli.timeflow.datastore.Date
import xyz.hyli.timeflow.datastore.Lesson
import xyz.hyli.timeflow.datastore.LessonTimePeriodInfo
import xyz.hyli.timeflow.datastore.Schedule
import xyz.hyli.timeflow.datastore.Time
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
import xyz.hyli.timeflow.ui.components.TimePeriodPickerDialog
import xyz.hyli.timeflow.ui.components.TimePeriodPickerStyle
import xyz.hyli.timeflow.ui.components.rememberDialogInputValidator
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.navigation.SettingsDestination
import xyz.hyli.timeflow.ui.theme.NotoSans
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
        modifier = Modifier.fillMaxWidth()
    ) {
        if (currentPlatform().isDesktop()) {
            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }
        Text(
            text = stringResource(Res.string.page_settings),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
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
                items = settings.schedule.keys.toList(),
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
        Spacer(modifier = Modifier.height(
            if (currentPlatform().isDesktop()) 12.dp
            else 24.dp
        ))
    }
}

@Composable
fun SettingsLessonsPerDayScreen(
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
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        .padding(horizontal = 12.dp),
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
                        .padding(horizontal = 12.dp)
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
            val lessonStringResourceList = listOf(
                Res.string.lesson_1,
                Res.string.lesson_2,
                Res.string.lesson_3,
                Res.string.lesson_4,
                Res.string.lesson_5,
                Res.string.lesson_6,
                Res.string.lesson_7,
                Res.string.lesson_8,
                Res.string.lesson_9,
                Res.string.lesson_10,
                Res.string.lesson_11,
                Res.string.lesson_12,
                Res.string.lesson_13,
                Res.string.lesson_14,
                Res.string.lesson_15,
                Res.string.lesson_16,
                Res.string.lesson_17,
                Res.string.lesson_18,
                Res.string.lesson_19,
                Res.string.lesson_20,
                Res.string.lesson_21,
                Res.string.lesson_22,
                Res.string.lesson_23,
                Res.string.lesson_24,
                Res.string.lesson_25,
                Res.string.lesson_26,
                Res.string.lesson_27,
                Res.string.lesson_28,
                Res.string.lesson_29,
                Res.string.lesson_30
            )

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
                            title = stringResource(lessonStringResourceList[i]),
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
                                        val updatedLessons = LessonTimePeriodInfo.generateLessons(
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
                            title = stringResource(lessonStringResourceList[i + morningCount.value]),
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
                                        val updatedLessons = LessonTimePeriodInfo.generateLessons(
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
                            title = stringResource(lessonStringResourceList[i + morningCount.value + afternoonCount.value]),
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
                                        val updatedLessons = LessonTimePeriodInfo.generateLessons(
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
                modifier = if (currentPlatform().isDesktop()) Modifier.height(12.dp)
                    else Modifier.height(
                        maxOf(
                            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
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