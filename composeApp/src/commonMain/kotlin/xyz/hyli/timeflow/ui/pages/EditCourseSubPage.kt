package xyz.hyli.timeflow.ui.pages

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.all_week
import timeflow.composeapp.generated.resources.back
import timeflow.composeapp.generated.resources.cancel
import timeflow.composeapp.generated.resources.confirm
import timeflow.composeapp.generated.resources.dialog_course_time_picker_section
import timeflow.composeapp.generated.resources.dialog_title_course_time_picker
import timeflow.composeapp.generated.resources.even_week
import timeflow.composeapp.generated.resources.odd_week
import timeflow.composeapp.generated.resources.required
import timeflow.composeapp.generated.resources.save
import timeflow.composeapp.generated.resources.schedule_title_course_classroom
import timeflow.composeapp.generated.resources.schedule_title_course_name
import timeflow.composeapp.generated.resources.schedule_title_course_teacher
import timeflow.composeapp.generated.resources.schedule_title_course_time_end
import timeflow.composeapp.generated.resources.schedule_title_course_time_start
import timeflow.composeapp.generated.resources.schedule_title_course_week
import timeflow.composeapp.generated.resources.schedule_title_edit_course
import timeflow.composeapp.generated.resources.schedule_value_course_time
import xyz.hyli.timeflow.datastore.Course
import xyz.hyli.timeflow.datastore.Range
import xyz.hyli.timeflow.datastore.WeekDescriptionEnum
import xyz.hyli.timeflow.datastore.WeekList
import xyz.hyli.timeflow.ui.components.DialogButton
import xyz.hyli.timeflow.ui.components.DialogButtonType
import xyz.hyli.timeflow.ui.components.DialogDefaults
import xyz.hyli.timeflow.ui.components.IntTextField
import xyz.hyli.timeflow.ui.components.MyDialog
import xyz.hyli.timeflow.ui.components.WheelPicker
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop

enum class EditCourseStyle {
    Screen,
    Dialog
}

@Composable
fun EditCourseScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController,
    initValue: Course
) {
    val settings by viewModel.settings.collectAsState()
    val schedule = settings.schedule[settings.selectedSchedule]!!
    var course = remember { mutableStateOf(initValue) }
    var isNameValid = remember { mutableStateOf(initValue.name.isNotBlank()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
            Text(
                text = stringResource(Res.string.schedule_title_edit_course)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    // TODO: Check Course validity
                    viewModel.updateSchedule(
                        schedule = schedule.copy(
                            courses = if (initValue in schedule.courses) {
                                schedule.courses.map { if (it == initValue) course.value else it }
                            } else {
                                schedule.courses + course.value
                            }
                        )
                    )
                    navHostController.popBackStack()
                },
                enabled = true,
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = stringResource(Res.string.save)
                )
            }
        }
        EditCourseContent(
            style = EditCourseStyle.Screen,
            viewModel = viewModel,
            courseValue = course,
            isNameValid = isNameValid
        )
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditCourseContent(
    style: EditCourseStyle,
    viewModel: TimeFlowViewModel,
    courseValue: MutableState<Course>,
    isNameValid: MutableState<Boolean>
) {
    val settings by viewModel.settings.collectAsState()
    val schedule = settings.schedule[settings.selectedSchedule]!!
    var course by courseValue

    LaunchedEffect(course) {
        isNameValid.value = course.name.isNotBlank()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Course Name
        OutlinedTextField(
            value = course.name,
            onValueChange = { course = course.copy(name = it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = !isNameValid.value,
            label = {
                Text(
                    text = stringResource(Res.string.schedule_title_course_name) + "*"
                )
            },
            supportingText = {
                Text(
                    text = "*" + stringResource(Res.string.required)
                )
            },
            shape = CardDefaults.shape
        )
        // Classroom
        OutlinedTextField(
            value = course.classroom,
            onValueChange = { course = course.copy(classroom = it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text(
                    text = stringResource(Res.string.schedule_title_course_classroom)
                )
            },
            shape = CardDefaults.shape
        )
        // Teacher
        OutlinedTextField(
            value = course.teacher,
            onValueChange = { course = course.copy(teacher = it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text(
                    text = stringResource(Res.string.schedule_title_course_teacher)
                )
            },
            shape = CardDefaults.shape
        )
        // Course Time
        val containerColor = OutlinedTextFieldDefaults.colors().unfocusedContainerColor
        val borderColor = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor
        when (style) {
            EditCourseStyle.Screen -> {
                CourseTimeDialog(
                    initStartTime = course.time.start,
                    initEndTime = course.time.end,
                    totalLessonsCount = schedule.lessonTimePeriodInfo.getTotalLessons(),
                    containerColor = containerColor,
                    borderColor = borderColor,
                    onCourseTimeChange = { range ->
                        course = course.copy(time = range)
                    }
                )
            }

            EditCourseStyle.Dialog -> {
                CourseTimeCard(
                    initStartTime = course.time.start,
                    initEndTime = course.time.end,
                    totalLessonsCount = schedule.lessonTimePeriodInfo.getTotalLessons(),
                    onCourseTimeChange = { range ->
                        course = course.copy(time = range)
                    }
                )
            }
        }

        // Course Week
        Card(
            modifier = Modifier.padding(top = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
            ),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.schedule_title_course_week)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    FlowRow(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val items = listOf(
                            WeekDescriptionEnum.ODD to stringResource(Res.string.odd_week),
                            WeekDescriptionEnum.EVEN to stringResource(Res.string.even_week),
                            WeekDescriptionEnum.ALL to stringResource(Res.string.all_week)
                        )
                        items.forEachIndexed { index, item ->
                            val weekList = WeekList(
                                weekDescription = item.first,
                                totalWeeks = schedule.totalWeeks()
                            )
                            ToggleButton(
                                modifier = Modifier
                                    .padding(horizontal = 1.dp)
                                    .semantics { role = Role.RadioButton },
                                checked = course.week.week.toSet() == weekList.week.toSet(),
                                onCheckedChange = { course = course.copy(week = weekList) },
                                shapes =
                                    when (index) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                        items.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    },
                            ) {
                                Text(item.second)
                            }
                        }
                    }
                }
                FlowRow {
                    for (i in 1..schedule.totalWeeks()) {
                        var isSelected by remember {
                            mutableStateOf(
                                course.week.week.contains(
                                    i
                                )
                            )
                        }
                        LaunchedEffect(course.week) {
                            isSelected = course.week.week.contains(i)
                        }
                        val containerColor by ToggleButtonDefaults.toggleButtonColors().let {
                            animateColorAsState(
                                if (isSelected) it.checkedContainerColor
                                else it.containerColor,
                            )
                        }
                        val contentColor by ToggleButtonDefaults.toggleButtonColors().let {
                            animateColorAsState(
                                if (isSelected) it.checkedContentColor
                                else it.contentColor,
                            )
                        }
                        val colors = ToggleButtonDefaults.toggleButtonColors(
                            containerColor = containerColor,
                            contentColor = contentColor,
                            disabledContainerColor = containerColor,
                            disabledContentColor = contentColor
                        )
                        ToggleButton(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .semantics { role = Role.RadioButton },
                            checked = isSelected,
                            onCheckedChange = {
                                course = if (isSelected) {
                                    course.copy(week = course.week.copy(week = course.week.week - i))
                                } else {
                                    course.copy(week = course.week.copy(week = course.week.week + i))
                                }
                            },
                            shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
                            colors = colors,
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = i.toString()
                                )
                            }
                        }
                    }
                }
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
                    Res.string.schedule_value_course_time,
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
                    selectIndex = initStartTime,
                    visibleCount = 3,
                    onSelect = { _, time ->
                        startTime = time
                        onCourseTimeChange(Range(startTime, endTime))
                    }
                ) {
                    Text(
                        modifier = Modifier.width(32.dp),
                        text = it.toString(),
                        fontFamily = FontFamily.Monospace,
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
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.weight(1f))
                WheelPicker(
                    data = (1..totalLessonsCount).toList(),
                    selectIndex = initEndTime,
                    visibleCount = 3,
                    onSelect = { _, time ->
                        endTime = time
                        onCourseTimeChange(Range(startTime, endTime))
                    }
                ) {
                    Text(
                        modifier = Modifier.width(32.dp),
                        text = it.toString(),
                        fontFamily = FontFamily.Monospace,
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
fun CourseTimeCard(
    initStartTime: Int,
    initEndTime: Int,
    totalLessonsCount: Int,
    onCourseTimeChange: (Range) -> Unit
) {
    var startTime by remember { mutableStateOf(initStartTime) }
    var endTime by remember { mutableStateOf(initEndTime) }
    LaunchedEffect(startTime, endTime) {
        if (startTime <= endTime) {
            onCourseTimeChange(Range(startTime, endTime))
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        IntTextField(
            modifier = Modifier.weight(1f),
            value = startTime,
            range = 1..endTime,
            label = { Text(stringResource(Res.string.schedule_title_course_time_start)) },
            textAlign = TextAlign.Start,
            onValueChange = {
                startTime = it
            },
            shape = CardDefaults.shape
        )
        Spacer(modifier = Modifier.width(8.dp))
        IntTextField(
            modifier = Modifier.weight(1f),
            value = endTime,
            range = startTime..totalLessonsCount,
            label = { Text(stringResource(Res.string.schedule_title_course_time_end)) },
            textAlign = TextAlign.Start,
            onValueChange = {
                endTime = it
            },
            shape = CardDefaults.shape
        )
    }
}