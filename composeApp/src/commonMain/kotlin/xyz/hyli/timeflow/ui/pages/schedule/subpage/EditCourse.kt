package xyz.hyli.timeflow.ui.pages.schedule.subpage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.materialkolor.ktx.harmonize
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.all_week
import timeflow.composeapp.generated.resources.back
import timeflow.composeapp.generated.resources.confirm
import timeflow.composeapp.generated.resources.even_week
import timeflow.composeapp.generated.resources.odd_week
import timeflow.composeapp.generated.resources.required
import timeflow.composeapp.generated.resources.save
import timeflow.composeapp.generated.resources.schedule_button_confirm_delete
import timeflow.composeapp.generated.resources.schedule_button_delete_course
import timeflow.composeapp.generated.resources.schedule_title_add_course
import timeflow.composeapp.generated.resources.schedule_title_course_classroom
import timeflow.composeapp.generated.resources.schedule_title_course_color
import timeflow.composeapp.generated.resources.schedule_title_course_name
import timeflow.composeapp.generated.resources.schedule_title_course_teacher
import timeflow.composeapp.generated.resources.schedule_title_course_time_end
import timeflow.composeapp.generated.resources.schedule_title_course_time_start
import timeflow.composeapp.generated.resources.schedule_title_course_week
import timeflow.composeapp.generated.resources.schedule_title_edit_course
import xyz.hyli.timeflow.datastore.Course
import xyz.hyli.timeflow.datastore.Range
import xyz.hyli.timeflow.datastore.Schedule
import xyz.hyli.timeflow.datastore.WeekDescriptionEnum
import xyz.hyli.timeflow.datastore.WeekList
import xyz.hyli.timeflow.ui.components.ColorButton
import xyz.hyli.timeflow.ui.components.ColorDefinitions.COLORS
import xyz.hyli.timeflow.ui.components.ColorPicker
import xyz.hyli.timeflow.ui.components.ColorPickerStyle
import xyz.hyli.timeflow.ui.components.IntTextField
import xyz.hyli.timeflow.ui.pages.schedule.CourseTimeDialog
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState())
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
                text =
                    if (initValue in schedule.courses) stringResource(Res.string.schedule_title_edit_course)
                    else stringResource(Res.string.schedule_title_add_course)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    confirmEditCourse(
                        courseValue = course,
                        initValue = initValue,
                        viewModel = viewModel,
                        schedule = schedule
                    )
                    navHostController.popBackStack()
                },
                enabled = isNameValid.value && isTimeValid.value && isWeekValid.value,
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
                    viewModel.updateSchedule(
                        schedule = schedule.copy(
                            courses = schedule.courses - course.value
                        )
                    )
                    navHostController.popBackStack()
                }
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditCourseContent(
    style: EditCourseStyle,
    viewModel: TimeFlowViewModel,
    initValue: Course,
    courseValue: MutableState<Course>,
    isNameValid: MutableState<Boolean>,
    isTimeValid: MutableState<Boolean>,
    isWeekValid: MutableState<Boolean>,
    validWeeks: List<Int>
) {
    val settings by viewModel.settings.collectAsState()
    val schedule = settings.schedule[settings.selectedSchedule]!!
    var course by courseValue

    LaunchedEffect(course) {
        isNameValid.value = course.name.isNotBlank()
        isTimeValid.value = if (course.time.start > course.time.end) {
            false
        } else {
            schedule.courses.none {
                it != initValue && it.time.start <= course.time.end && it.time.end >= course.time.start && it.weekday == course.weekday && it.week.week.any { it in course.week.week }
            }
        }
        isWeekValid.value =
            course.week.week.isNotEmpty() && course.week.week.all { it in validWeeks }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Course Name
        OutlinedTextField(
            value = course.name,
            onValueChange = { course = course.copy(name = it.trim()) },
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
        fun onCourseTimeChange(range: Range) {
            course = course.copy(
                time = range, week = WeekList(
                    week = course.week.week.filter {
                        it in (1..schedule.totalWeeks()).toMutableList().let {
                            it - schedule.courses.filter {
                                it != initValue && it.time.start <= range.end && it.time.end >= range.start && it.weekday == course.weekday
                            }.flatMap { it.week.week }
                        }
                    }
                )
            )
        }
        when (style) {
            EditCourseStyle.Screen -> {
                CourseTimeDialog(
                    initStartTime = course.time.start,
                    initEndTime = course.time.end,
                    totalLessonsCount = schedule.lessonTimePeriodInfo.getTotalLessons(),
                    containerColor = containerColor,
                    borderColor = borderColor,
                    onCourseTimeChange = { onCourseTimeChange(it) }
                )
            }

            EditCourseStyle.Dialog -> {
                CourseTimeCard(
                    initStartTime = course.time.start,
                    initEndTime = course.time.end,
                    totalLessonsCount = schedule.lessonTimePeriodInfo.getTotalLessons(),
                    onCourseTimeChange = { onCourseTimeChange(it) }
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
                                totalWeeks = schedule.totalWeeks(),
                                validWeeks = validWeeks
                            )
                            ToggleButton(
                                modifier = Modifier
                                    .padding(horizontal = 1.dp)
                                    .semantics { role = Role.RadioButton },
                                checked = course.week.week.toSet() == weekList.week.toSet() && !(
                                        item.first != WeekDescriptionEnum.ALL && course.week.week.toSet() == WeekList(
                                            weekDescription = WeekDescriptionEnum.ALL,
                                            totalWeeks = schedule.totalWeeks(),
                                            validWeeks = validWeeks
                                        ).week.toSet()
                                        ),
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
                        val isSelected = course.week.week.contains(i)
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
                            contentColor = contentColor
                        ).let {
                            if (i in validWeeks) {
                                it
                            } else {
                                it.copy(
                                    checkedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                    checkedContentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        ToggleButton(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .semantics { role = Role.RadioButton },
                            checked = isSelected,
                            enabled = i in validWeeks || isSelected,
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
        // Card color
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
                var isColorCustom by remember { mutableStateOf(Color(course.color) !in COLORS && course.color != -1) }
                Text(
                    text = stringResource(Res.string.schedule_title_course_color),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
                FlowRow {
                    COLORS.forEach {
                        ColorButton(
                            color = it.harmonize(
                                MaterialTheme.colorScheme.secondaryContainer,
                                true
                            ),
                            containerColor = it.harmonize(
                                MaterialTheme.colorScheme.primaryContainer,
                                true
                            ),
                            onClick = {
                                course = course.copy(color = it.toArgb())
                                isColorCustom = false
                            },
                            selected = it.toArgb() == course.color && !isColorCustom,
                            size = 48.dp,
                            modifier = Modifier.padding(horizontal = 2.dp),
                            cardColor = Color.Transparent
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .aspectRatio(1f)
                            .padding(horizontal = 2.dp),
                        shape = CardDefaults.shape,
                        color = Color.Transparent,
                        onClick = {
                            isColorCustom = true
                        },
                        enabled = true,
                    ) {
                        val containerSize by animateDpAsState(targetValue = if (isColorCustom) 48.dp / 80 * 28 else 0.dp)
                        val iconSize by animateDpAsState(targetValue = if (isColorCustom) 48.dp / 80 * 16 else 0.dp)
                        val paletteColors = listOf(
                            Color.Red,
                            Color.Magenta,
                            Color.Blue,
                            Color.Cyan,
                            Color.Green,
                            Color.Yellow,
                            Color.Red // 再次添加红色以形成无缝循环
                        ).map { it.harmonize(MaterialTheme.colorScheme.secondaryContainer, true) }
                        val sweepGradientBrush = Brush.sweepGradient(colors = paletteColors)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(containerSize)
                                .drawBehind {
                                    drawCircle(brush = sweepGradientBrush)
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .size(containerSize)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                            )
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(iconSize).align(Alignment.Center),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
                AnimatedContent(
                    modifier = Modifier.padding(top = 8.dp),
                    targetState = isColorCustom,
                    transitionSpec = {
                        fadeIn() + expandVertically() togetherWith fadeOut()
                    }
                ) {
                    if (isColorCustom) {
                        val customColor = remember { mutableStateOf(Color(course.color)) }
                        ColorPicker(
                            color = customColor,
                            alphaSupported = false,
                            style = ColorPickerStyle.COMMON,
                            showColor = customColor.value.harmonize(
                                MaterialTheme.colorScheme.secondaryContainer,
                                true
                            ),
                            onColorChange = {
                                course = course.copy(color = it.toArgb())
                            }
                        )
                    }
                }
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

@Composable
fun DeleteCourseButton(
    onClick: () -> Unit,
) {
    var deleteButtonState by remember { mutableStateOf(false) }
    LaunchedEffect(deleteButtonState) {
        if (deleteButtonState) {
            delay(10000)
            if (deleteButtonState) deleteButtonState = false
        }
    }
    val containerColor by animateColorAsState(
        if (deleteButtonState) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            Color.Unspecified
        }
    )
    val contentColor by animateColorAsState(
        if (deleteButtonState) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            Color.Unspecified
        }
    )
    Button(
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth(),
        onClick = {
            if (deleteButtonState) {
                onClick()
                deleteButtonState = false
            } else {
                deleteButtonState = true
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text =
                    if (deleteButtonState) stringResource(Res.string.schedule_button_confirm_delete)
                    else stringResource(Res.string.schedule_button_delete_course)
            )
            AnimatedContent(
                targetState = deleteButtonState,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(300)
                    ) + expandHorizontally() togetherWith fadeOut(
                        animationSpec = tween(
                            300
                        )
                    )
                }
            ) {
                if (it) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = stringResource(Res.string.confirm)
                    )

                }
            }
        }
    }
}

fun confirmEditCourse(
    courseValue: MutableState<Course>,
    initValue: Course,
    viewModel: TimeFlowViewModel,
    schedule: Schedule,
) {
    var course by courseValue
    if (course.color == -1) {
        course = course.copy(
            color =
                schedule.courses.firstOrNull { it.name == course.name }?.color
                    ?: COLORS.random().toArgb()
        )
    }
    viewModel.updateSchedule(
        schedule = schedule.copy(
            courses = if (initValue in schedule.courses) {
                schedule.courses.map { if (it == initValue) course else it }
            } else {
                schedule.courses + course
            }
        )
    )
}
