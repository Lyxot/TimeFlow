package xyz.hyli.timeflow.ui.pages.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.cancel
import timeflow.composeapp.generated.resources.confirm
import timeflow.composeapp.generated.resources.dialog_course_time_picker_section
import timeflow.composeapp.generated.resources.dialog_title_course_time_picker
import timeflow.composeapp.generated.resources.save
import timeflow.composeapp.generated.resources.schedule_button_edit
import timeflow.composeapp.generated.resources.schedule_course_not_this_week
import timeflow.composeapp.generated.resources.schedule_title_add_course
import timeflow.composeapp.generated.resources.schedule_title_confirm_delete_schedule
import timeflow.composeapp.generated.resources.schedule_title_course_detail
import timeflow.composeapp.generated.resources.schedule_title_course_time_start
import timeflow.composeapp.generated.resources.schedule_title_create_schedule
import timeflow.composeapp.generated.resources.schedule_title_edit_course
import timeflow.composeapp.generated.resources.schedule_title_update_selected_schedule
import timeflow.composeapp.generated.resources.schedule_value_confirm_delete_schedule
import timeflow.composeapp.generated.resources.schedule_value_course_time_period
import timeflow.composeapp.generated.resources.schedule_value_course_week
import timeflow.composeapp.generated.resources.schedule_value_schedule_name_empty
import timeflow.composeapp.generated.resources.schedule_value_update_selected_schedule
import xyz.hyli.timeflow.datastore.Course
import xyz.hyli.timeflow.datastore.Range
import xyz.hyli.timeflow.datastore.Schedule
import xyz.hyli.timeflow.datastore.WeekDescriptionEnum
import xyz.hyli.timeflow.datastore.WeekList
import xyz.hyli.timeflow.ui.components.DialogButton
import xyz.hyli.timeflow.ui.components.DialogButtonType
import xyz.hyli.timeflow.ui.components.DialogDefaults
import xyz.hyli.timeflow.ui.components.DialogInputValidator
import xyz.hyli.timeflow.ui.components.DialogState
import xyz.hyli.timeflow.ui.components.MyDialog
import xyz.hyli.timeflow.ui.components.TextInputDialog
import xyz.hyli.timeflow.ui.components.WheelPicker
import xyz.hyli.timeflow.ui.components.rememberDialogInputValidator
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.pages.schedule.subpage.DeleteCourseButton
import xyz.hyli.timeflow.ui.pages.schedule.subpage.EditCourseContent
import xyz.hyli.timeflow.ui.pages.schedule.subpage.EditCourseStyle
import xyz.hyli.timeflow.ui.pages.schedule.subpage.confirmEditCourse
import xyz.hyli.timeflow.ui.theme.NotoSans
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@Composable
fun AddScheduleDialog(
    state: MutableState<Boolean>,
    viewModel: TimeFlowViewModel
) {
    val stringScheduleNameEmpty = stringResource(Res.string.schedule_value_schedule_name_empty)
    TextInputDialog(
        title = stringResource(Res.string.schedule_title_create_schedule),
        initialValue = "",
        onValueChange = { newValue ->
            viewModel.createSchedule(Schedule(name = newValue))
            state.value = false
        },
        onDismiss = { state.value = false },
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

@Composable
fun ConfirmSelectScheduleDialog(
    name: String,
    showConfirmSelectScheduleDialog: DialogState,
    onConfirm: () -> Unit
) {
    MyDialog(
        state = showConfirmSelectScheduleDialog,
        title = {
            Text(
                text = stringResource(Res.string.schedule_title_update_selected_schedule)
            )
        },
        buttons = DialogDefaults.buttons(
            positive = DialogButton(stringResource(Res.string.confirm)),
            negative = DialogButton(stringResource(Res.string.cancel)),
        ),
        onEvent = { event ->
            if (event.isPositiveButton) {
                onConfirm()
            }
        }
    ) {
        Text(
            text = stringResource(Res.string.schedule_value_update_selected_schedule, name),
        )
    }
}

@Composable
fun CourseListDialog(
    courses: List<Course>,
    currentWeek: Int,
    totalWeeks: Int,
    time: Range,
    showCourseListDialog: DialogState,
    onClick: (Course) -> Unit
) {
    MyDialog(
        state = showCourseListDialog,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.schedule_title_course_detail)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        onClick(
                            Course(
                                name = "",
                                time = time,
                                weekday = courses.first().weekday,
                                week = WeekList(
                                    weekDescription = WeekDescriptionEnum.ALL,
                                    totalWeeks = totalWeeks
                                ),
                                color = -1
                            )
                        )
                        showCourseListDialog.dismiss()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
        },
        buttons = DialogDefaults.buttons(
            positive = DialogButton(stringResource(Res.string.confirm)),
            negative = DialogButton.DISABLED,
        )
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            courses.forEachIndexed { index, course ->
                SubcomposeLayout { constraints ->
                    val button = subcompose("button") {
                        Button(
                            modifier = Modifier,
                            onClick = {
                                onClick(course)
                                showCourseListDialog.dismiss()
                            }
                        ) {
                            Text(
                                text = stringResource(Res.string.schedule_button_edit),
                                maxLines = 1
                            )
                        }
                    }[0].measure(constraints)
                    val info = subcompose("info") {
                        Row(
                            modifier = Modifier
                                .width((constraints.maxWidth - button.width).toDp())
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier
                            ) {
                                Text(
                                    text = course.name.let {
                                        if (course.week.week.contains(currentWeek)) it
                                        else it + " " + stringResource(Res.string.schedule_course_not_this_week)
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = buildAnnotatedString {
                                        append(
                                            stringResource(
                                                Res.string.schedule_value_course_week,
                                                course.week.getString()
                                            )
                                        )
                                        withStyle(
                                            SpanStyle(
                                                fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                            )
                                        ) {
                                            append(" | ")
                                        }
                                        append(
                                            stringResource(
                                                Res.string.schedule_value_course_time_period,
                                                course.time.start,
                                                course.time.end
                                            )
                                        )
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                if (course.classroom.isNotBlank() || course.teacher.isNotBlank()) {
                                    Text(
                                        buildAnnotatedString {
                                            append(course.classroom)
                                            if (course.classroom.isNotBlank() && course.teacher.isNotBlank()) {
                                                withStyle(
                                                    SpanStyle(
                                                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                                    )
                                                ) {
                                                    append(" | ")
                                                }
                                            }
                                            append(course.teacher)
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }[0].measure(constraints)
                    val height = maxOf(button.height, info.height)
                    layout(constraints.maxWidth, height) {
                        info.place(0, (height - info.height) / 2)
                        button.place(info.width, (height - button.height) / 2)
                    }
                }
                if (index < courses.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EditCourseDialog(
    state: MutableState<TableState>,
    scheduleParams: ScheduleParams,
    initValue: Course,
    showEditCourseDialog: DialogState
) {
    val schedule = scheduleParams.schedule
    val course = remember { mutableStateOf(initValue) }
    val isNameValid =
        remember { mutableStateOf(course.value.name.isNotBlank()) }
    val isTimeValid = remember {
        mutableStateOf(
            if (course.value.time.start > course.value.time.end) {
                false
            } else {
                schedule.courses.none {
                    it != initValue && it.time.start <= course.value.time.end && it.time.end >= course.value.time.start && it.weekday == course.value.weekday && it.week.week.any { it in course.value.week.week }
                }
            }
        )
    }
    val validWeeks = (1..schedule.totalWeeks()).toMutableList().let {
        it - schedule.courses.filter {
            it != initValue && it.time.start <= course.value.time.end && it.time.end >= course.value.time.start && it.weekday == course.value.weekday
        }.flatMap { it.week.week }
    }
    val isWeekValid =
        remember { mutableStateOf(course.value.week.week.isNotEmpty() && course.value.week.week.all { it in validWeeks }) }
    showEditCourseDialog.enableButton(
        button = DialogButtonType.Positive,
        enabled = isNameValid.value && isTimeValid.value && isWeekValid.value
    )
    MyDialog(
        state = showEditCourseDialog,
        title = {
            Text(
                text =
                    if (initValue in schedule.courses) stringResource(Res.string.schedule_title_edit_course)
                    else stringResource(Res.string.schedule_title_add_course)
            )
        },
        buttons = DialogDefaults.buttons(
            positive = DialogButton(stringResource(Res.string.save)),
            negative = DialogButton(stringResource(Res.string.cancel)),
        ),
        onEvent = { event ->
            if (event.isPositiveButton) {
                confirmEditCourse(
                    courseValue = course,
                    initValue = initValue,
                    viewModel = scheduleParams.viewModel,
                    schedule = schedule
                )
            }
            state.value = TableState() // 重置状态
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            EditCourseContent(
                style = EditCourseStyle.Dialog,
                viewModel = scheduleParams.viewModel,
                initValue = initValue,
                courseValue = course,
                isNameValid = isNameValid,
                isTimeValid = isTimeValid,
                isWeekValid = isWeekValid,
                validWeeks = validWeeks
            )
            if (initValue in schedule.courses) {
                DeleteCourseButton(
                    onClick = {
                        scheduleParams.viewModel.updateSchedule(
                            schedule = schedule.copy(
                                courses = schedule.courses - course.value
                            )
                        )
                        showEditCourseDialog.dismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun CourseTimeDialog(
    initStartTime: Int,
    initEndTime: Int,
    totalLessonsCount: Int,
    containerColor: Color,
    borderColor: Color,
    onCourseTimeChange: (Range) -> Unit,
) {
    val showCourseTimePickerDialog = rememberDialogState()
    var startTime by remember { mutableStateOf(initStartTime) }
    var endTime by remember { mutableStateOf(initEndTime) }
    Card(
        modifier = Modifier.padding(top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        border = BorderStroke(1.dp, borderColor),
        onClick = { showCourseTimePickerDialog.show() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.schedule_title_course_time_start)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                stringResource(
                    Res.string.schedule_value_course_time_period,
                    startTime, endTime
                )
            )
        }
    }
    if (showCourseTimePickerDialog.visible) {
        MyDialog(
            state = showCourseTimePickerDialog,
            title = { Text(stringResource(Res.string.dialog_title_course_time_picker)) },
            buttons = DialogDefaults.buttons(
                positive = DialogButton(stringResource(Res.string.confirm)),
                negative = DialogButton(stringResource(Res.string.cancel)),
            ),
            onEvent = { event ->
                if (event.isPositiveButton) {
                    onCourseTimeChange(Range(startTime, endTime))
                }
            }
        ) {
            LaunchedEffect(startTime, endTime) {
                showCourseTimePickerDialog.enableButton(
                    DialogButtonType.Positive,
                    startTime <= endTime
                )
            }

            Row(
                modifier = Modifier.height(128.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                WheelPicker(
                    data = (1..totalLessonsCount).toList(),
                    selectIndex = initStartTime - 1,
                    visibleCount = 3,
                    onSelect = { _, time ->
                        startTime = time
                    }
                ) {
                    Text(
                        modifier = Modifier.width(32.dp),
                        text = it.toString(),
                        fontFamily = NotoSans,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = stringResource(Res.string.dialog_course_time_picker_section),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "—",
                    fontWeight = FontWeight.Bold,
                    fontFamily = NotoSans
                )
                Spacer(modifier = Modifier.weight(1f))
                WheelPicker(
                    data = (1..totalLessonsCount).toList(),
                    selectIndex = initEndTime - 1,
                    visibleCount = 3,
                    onSelect = { _, time ->
                        endTime = time
                    }
                ) {
                    Text(
                        modifier = Modifier.width(32.dp),
                        text = it.toString(),
                        fontFamily = NotoSans,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = stringResource(Res.string.dialog_course_time_picker_section),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DeleteSelectedSchedulesDialog(
    selectedScheduleName: List<String>,
    onConfirm: () -> Unit,
    showConfirmDeleteSelectedSchedulesDialog: DialogState
) {
    MyDialog(
        state = showConfirmDeleteSelectedSchedulesDialog,
        title = {
            Text(
                text = stringResource(Res.string.schedule_title_confirm_delete_schedule)
            )
        },
        buttons = DialogDefaults.buttons(
            positive = DialogButton(stringResource(Res.string.confirm)),
            negative = DialogButton(stringResource(Res.string.cancel)),
        ),
        onEvent = { event ->
            if (event.isPositiveButton) {
                // Confirm delete selected schedules
                onConfirm.invoke()
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(Res.string.schedule_value_confirm_delete_schedule)
            )
            Spacer(modifier = Modifier.height(8.dp))
            selectedScheduleName.forEach {
                Text(
                    text = " • $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
