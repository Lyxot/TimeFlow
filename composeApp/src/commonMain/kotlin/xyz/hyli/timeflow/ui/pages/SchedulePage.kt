package xyz.hyli.timeflow.ui.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.all_week
import timeflow.composeapp.generated.resources.back
import timeflow.composeapp.generated.resources.even_week
import timeflow.composeapp.generated.resources.friday
import timeflow.composeapp.generated.resources.monday
import timeflow.composeapp.generated.resources.odd_week
import timeflow.composeapp.generated.resources.required
import timeflow.composeapp.generated.resources.saturday
import timeflow.composeapp.generated.resources.save
import timeflow.composeapp.generated.resources.schedule_title_course_classroom
import timeflow.composeapp.generated.resources.schedule_title_course_name
import timeflow.composeapp.generated.resources.schedule_title_course_teacher
import timeflow.composeapp.generated.resources.schedule_title_course_time
import timeflow.composeapp.generated.resources.schedule_title_course_week
import timeflow.composeapp.generated.resources.schedule_title_edit_course
import timeflow.composeapp.generated.resources.schedule_value_course_time
import timeflow.composeapp.generated.resources.settings_subtitle_schedule_empty
import timeflow.composeapp.generated.resources.sunday
import timeflow.composeapp.generated.resources.thursday
import timeflow.composeapp.generated.resources.tuesday
import timeflow.composeapp.generated.resources.wednesday
import xyz.hyli.timeflow.datastore.Course
import xyz.hyli.timeflow.datastore.Lesson
import xyz.hyli.timeflow.datastore.Range
import xyz.hyli.timeflow.datastore.Schedule
import xyz.hyli.timeflow.datastore.WeekDescriptionEnum
import xyz.hyli.timeflow.datastore.WeekList
import xyz.hyli.timeflow.datastore.Weekday
import xyz.hyli.timeflow.ui.components.CourseTimePickerDialog
import xyz.hyli.timeflow.ui.components.CourseTimePickerStyle
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.navigation.EditCourseDestination
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScheduleScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val settings by viewModel.settings.collectAsState()
    val schedule = settings.schedule[settings.selectedSchedule]
    val row = if (schedule?.displayWeekends == true) 7 else 5
    val column = schedule?.lessonTimePeriodInfo?.getTotalLessons()

    @Composable
    fun ScheduleScreenContent() {
        if (schedule == null || column == null || column == 0) {
            // 显示空状态或错误信息
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.settings_subtitle_schedule_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .then(
                    if (currentPlatform().isDesktop())
                        Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp)
                    else Modifier
                )
        ) {
            // 课程表表格
            ScheduleTable(
                rows = row,
                columns = column,
                schedule = schedule,
                modifier = Modifier.fillMaxWidth(),
                navHostController = navHostController
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

    AnimatedContent(
        settings.initialized,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(300)
            ) togetherWith fadeOut(animationSpec = tween(300))
        }
    ) {
        when (it) {
            false -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(
                        modifier = Modifier.size(96.dp)
                    )
                }
            }

            true -> {
                ScheduleScreenContent()
            }
        }
    }
}

data class TableState(
    val state: TableCellState = TableCellState.NORMAL,
    val row: Int = 0,
    val column: Int = 0,
)

enum class TableCellState {
    NORMAL,
    IS_CLICKED,
    IS_MODIFYING
}

@Composable
fun ScheduleTable(
    rows: Int,
    columns: Int,
    schedule: Schedule,
    modifier: Modifier = Modifier,
    navHostController: NavHostController
) {
    val lessonTimePeriodInfo = schedule.lessonTimePeriodInfo.morning +
            schedule.lessonTimePeriodInfo.afternoon +
            schedule.lessonTimePeriodInfo.evening

    Box(modifier = modifier.fillMaxWidth()) {
        val state = remember { mutableStateOf(TableState()) }
        
        // 自动重置状态的逻辑
        LaunchedEffect(state.value) {
            if (state.value.state == TableCellState.IS_CLICKED) {
                kotlinx.coroutines.delay(10000) // 10秒
                if (state.value.state == TableCellState.IS_CLICKED) {
                    state.value = state.value.copy(state = TableCellState.NORMAL)
                }
            }
        }
        
        // 底层：表格框架
        TableGrid(
            state = state,
            rows = rows,
            columns = columns,
            lessonTimePeriodInfo = lessonTimePeriodInfo
        )

        // 覆盖层：课程内容
        CourseOverlay(
            state = state,
            rows = rows,
            columns = columns,
            schedule = schedule,
            modifier = Modifier.fillMaxSize(),
            navHostController = navHostController
        )
    }
}

@Composable
fun TableGrid(
    state: MutableState<TableState>,
    rows: Int,
    columns: Int,
    lessonTimePeriodInfo: List<Lesson>
) {
    val weekdays = listOf(
        Res.string.monday,
        Res.string.tuesday,
        Res.string.wednesday,
        Res.string.thursday,
        Res.string.friday,
        Res.string.saturday,
        Res.string.sunday
    ).map { stringResource(it) }

    Row(modifier = Modifier.fillMaxWidth()) {
        // 时间列
        TimeColumn(
            modifier = Modifier.width(48.dp),
            columns = columns,
            allLessons = lessonTimePeriodInfo
        )

        // 每一天的空白列（只显示表头和边框）
        for (dayIndex in 0 until rows) {
            EmptyDayColumn(
                state = state,
                modifier = Modifier.weight(1f),
                dayName = weekdays[dayIndex],
                dayIndex = dayIndex,
                columns = columns,
                isLastDay = dayIndex == rows - 1
            )
        }
    }
}

@Composable
fun TimeColumn(
    modifier: Modifier = Modifier,
    columns: Int,
    allLessons: List<Lesson>
) {
    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        // 表头 - 空格
        TableCell(
            height = 40.dp,
            isRightBorder = true,
            isBottomBorder = true
        ) { }

        // 时间行
        for (lessonIndex in 0 until columns) {
            TableCell(
                isRightBorder = true,
                isBottomBorder = lessonIndex < columns - 1
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${lessonIndex + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (lessonIndex < allLessons.size) {
                        Text(
                            text = "${allLessons[lessonIndex].start}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${allLessons[lessonIndex].end}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyDayColumn(
    state: MutableState<TableState>,
    modifier: Modifier = Modifier,
    dayName: String,
    dayIndex: Int,
    columns: Int,
    isLastDay: Boolean
) {
    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        // 表头 - 星期
        TableCell(
            height = 40.dp,
            isRightBorder = !isLastDay,
            isBottomBorder = true
        ) {
            Text(
                text = dayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // 空白课程单元格
        for (lessonIndex in 0 until columns) {
            TableCell(
                isRightBorder = !isLastDay,
                isBottomBorder = lessonIndex < columns - 1
            ) {
                // 空白单元格，只显示边框
                if (state.value.row != dayIndex ||
                    state.value.column != lessonIndex ||
                    state.value.state != TableCellState.IS_CLICKED) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        colors = CardDefaults.cardColors().copy(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    state.value = TableState(
                                        state = TableCellState.IS_CLICKED,
                                        row = dayIndex,
                                        column = lessonIndex
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseOverlay(
    state: MutableState<TableState>,
    rows: Int,
    columns: Int,
    schedule: Schedule,
    modifier: Modifier = Modifier,
    navHostController: NavHostController
) {
    Row(modifier = modifier) {
        // 时间列占位（与底层对齐）
        Box(modifier = Modifier.width(48.dp))

        // 课程覆盖层
        for (dayIndex in 0 until rows) {
            CourseColumn(
                state = state,
                modifier = Modifier.weight(1f),
                dayIndex = dayIndex,
                columns = columns,
                schedule = schedule,
                navHostController = navHostController
            )
        }
    }
}

@Composable
fun CourseColumn(
    state: MutableState<TableState>,
    modifier: Modifier = Modifier,
    dayIndex: Int,
    columns: Int,
    schedule: Schedule,
    navHostController: NavHostController
) {
    val weekdays = listOf(
        Weekday.MONDAY,
        Weekday.TUESDAY,
        Weekday.WEDNESDAY,
        Weekday.THURSDAY,
        Weekday.FRIDAY,
        Weekday.SATURDAY,
        Weekday.SUNDAY
    )
    val daySchedule = schedule.courses.filter {
        it.weekday == weekdays[dayIndex]
    }

    Column(modifier = modifier) {
        // 表头占位（与底层对齐）
        Box(modifier = Modifier.height(40.dp))

        // 课程单元格
        for (lessonIndex in 0 until columns) {
            // 查找该时间段的课程
            val coursesForThisTime = daySchedule.filter { course ->
                course.time.start <= lessonIndex + 1 && course.time.end >= lessonIndex + 1
            }

            Box(
                modifier = Modifier.height(64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (coursesForThisTime.isNotEmpty()) {
                    val course = coursesForThisTime.first()
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .clickable {
                                state.value = TableState(
                                    state = TableCellState.IS_MODIFYING,
                                    row = dayIndex,
                                    column = lessonIndex
                                )
                                // TODO: Dialog to show course details, edit and add new course
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                } else if (state.value.row == dayIndex && state.value.column == lessonIndex) {
                    if (state.value.state != TableCellState.NORMAL) {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        state.value = state.value.copy(
                                            state = TableCellState.IS_MODIFYING
                                        )
                                        navHostController.navigate(
                                            EditCourseDestination(
                                                Course(
                                                    name = "",
                                                    time = Range(lessonIndex + 1, lessonIndex + 1),
                                                    weekday = weekdays[dayIndex],
                                                    week = WeekList(
                                                        weekDescription = WeekDescriptionEnum.ALL,
                                                        totalWeeks = schedule.totalWeeks()
                                                    )
                                                )
                                            )
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
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
fun TableCell(
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    isRightBorder: Boolean = false,
    isBottomBorder: Boolean = false,
    content: @Composable () -> Unit
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val borderWidth = 1.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                color = MaterialTheme.colorScheme.surface
            )
            // 绘制右边框
            .then(
                if (isRightBorder) {
                    Modifier.drawBehind {
                        drawLine(
                            color = borderColor,
                            start = Offset(size.width, 0f),
                            end = Offset(size.width, size.height),
                            strokeWidth = borderWidth.toPx()
                        )
                    }
                } else Modifier
            )
            // 绘制底边框
            .then(
                if (isBottomBorder) {
                    Modifier.drawBehind {
                        drawLine(
                            color = borderColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = borderWidth.toPx()
                        )
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditCourseScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController,
    initValue: Course
) {
    val settings by viewModel.settings.collectAsState()
    val schedule = settings.schedule[settings.selectedSchedule]!!
    var course by remember { mutableStateOf(initValue) }
    var isNameValid by remember { mutableStateOf(initValue.name.isNotBlank()) }

    LaunchedEffect(course) {
        isNameValid = course.name.isNotBlank()
    }

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
                        uuid = settings.selectedSchedule,
                        schedule = schedule.copy(
                            courses = if (initValue in schedule.courses) {
                                schedule.courses.map { if (it == initValue) course else it }
                            } else {
                                schedule.courses + course
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = course.name,
                onValueChange = { course = course.copy(name = it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = !isNameValid,
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
            val showCourseTimePickerDialog = rememberDialogState()
            val containerColor = OutlinedTextFieldDefaults.colors().unfocusedContainerColor
            val borderColor = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor
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
                        text = stringResource(Res.string.schedule_title_course_time)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        stringResource(
                            Res.string.schedule_value_course_time,
                            course.time.start, course.time.end
                        )
                    )
                }
            }
            if (showCourseTimePickerDialog.visible) {
                CourseTimePickerDialog(
                    style =
                        if (currentPlatform().isDesktop())
                            CourseTimePickerStyle.TextField
                        else
                            CourseTimePickerStyle.Wheel,
                    state = showCourseTimePickerDialog,
                    initStartTime = course.time.start,
                    initEndTime = course.time.end,
                    totalLessonsCount = settings.schedule[settings.selectedSchedule]?.lessonTimePeriodInfo?.getTotalLessons()
                        ?: 0,
                    onCourseTimeChange = { start, end ->
                        course = course.copy(time = Range(start, end))
                    }
                )
            }
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
                                    .padding(horizontal = 4.dp)
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
