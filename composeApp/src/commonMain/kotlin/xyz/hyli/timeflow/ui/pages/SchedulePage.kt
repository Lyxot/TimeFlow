package xyz.hyli.timeflow.ui.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.cancel
import timeflow.composeapp.generated.resources.friday
import timeflow.composeapp.generated.resources.monday
import timeflow.composeapp.generated.resources.saturday
import timeflow.composeapp.generated.resources.save
import timeflow.composeapp.generated.resources.schedule_title_edit_course
import timeflow.composeapp.generated.resources.schedule_title_week_vacation
import timeflow.composeapp.generated.resources.schedule_title_week_x_part_1
import timeflow.composeapp.generated.resources.schedule_title_week_x_part_2
import timeflow.composeapp.generated.resources.settings_subtitle_schedule_empty
import timeflow.composeapp.generated.resources.settings_subtitle_schedule_not_selected
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
import xyz.hyli.timeflow.ui.components.DialogButton
import xyz.hyli.timeflow.ui.components.DialogButtonType
import xyz.hyli.timeflow.ui.components.DialogDefaults
import xyz.hyli.timeflow.ui.components.DialogState
import xyz.hyli.timeflow.ui.components.MyDialog
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.navigation.EditCourseDestination
import xyz.hyli.timeflow.ui.navigation.NavigationBarType
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop

val weekdays = listOf(
    Weekday.MONDAY,
    Weekday.TUESDAY,
    Weekday.WEDNESDAY,
    Weekday.THURSDAY,
    Weekday.FRIDAY,
    Weekday.SATURDAY,
    Weekday.SUNDAY
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScheduleScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController,
    navSuiteType: NavigationSuiteType,
) {
    val settings by viewModel.settings.collectAsState()
    val schedule = settings.schedule[settings.selectedSchedule]
    val columns = if (schedule?.displayWeekends == true) 7 else 5
    val rows = schedule?.lessonTimePeriodInfo?.getTotalLessons()

    @Composable
    fun ScheduleScreenContent() {
        if (schedule == null || rows == null || rows == 0) {
            // 显示空状态或错误信息
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text =
                        if (!settings.schedule.values.any { !it.deleted })
                            stringResource(Res.string.settings_subtitle_schedule_empty)
                        else
                            stringResource(Res.string.settings_subtitle_schedule_not_selected),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            return
        }

        val pagerState = schedule.totalWeeks().let {
            rememberPagerState(
                initialPage = (schedule.weeksTill() - 1).coerceIn(0, it), // page 0 for week 1
                pageCount = {
                    it + 1 // +1 for vacation
                }
            )
        }
        val coroutineScope = rememberCoroutineScope()
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // placeholder
                IconButton(
                    onClick = { },
                    enabled = false
                ) { }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    enabled = pagerState.currentPage > 0
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                        contentDescription = null
                    )
                }
                Box(
                    modifier = Modifier.clickable {
                        // TODO: Select week dialog (bottom sheet)
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Row {
                        val color = if (pagerState.currentPage < pagerState.pageCount - 1)
                            MaterialTheme.colorScheme.onBackground
                        else
                            Color.Transparent
                        Text(
                            text = stringResource(Res.string.schedule_title_week_x_part_1),
                            color = color,
                        )
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1}",
                                fontFamily = FontFamily.Monospace,
                                color = color,
                            )
                            Text(
                                text = "${pagerState.pageCount}",
                                fontFamily = FontFamily.Monospace,
                                color = Color.Transparent
                            )
                        }
                        Text(
                            text = stringResource(Res.string.schedule_title_week_x_part_2),
                            color = color,
                        )
                    }

                    Text(
                        text = stringResource(Res.string.schedule_title_week_vacation),
                        color =
                            if (pagerState.currentPage < pagerState.pageCount - 1)
                                Color.Transparent
                            else
                                MaterialTheme.colorScheme.onBackground,
                    )
                }
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    enabled = pagerState.currentPage < pagerState.pageCount - 1
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        // TODO: Select a schedule
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.SyncAlt,
                        contentDescription = stringResource(Res.string.save)
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                pageSpacing = 12.dp,
                beyondViewportPageCount = 2, // 预加载前后各一页
            ) { page ->
                // 课程表表格
                ScheduleTable(
                    viewModel = viewModel,
                    rows = rows,
                    columns = columns,
                    schedule = schedule,
                    currentWeek = page + 1,
                    modifier = Modifier.fillMaxWidth(),
                    navHostController = navHostController,
                    navSuiteType = navSuiteType
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
    val row: Int = 0,
    val column: Int = 0,
    val isClicked: Int = 0 // 0: 未点击, 1: 点击中, 2: 点击后等待重置
)

@Composable
fun ScheduleTable(
    viewModel: TimeFlowViewModel,
    rows: Int,
    columns: Int,
    schedule: Schedule,
    currentWeek: Int,
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    navSuiteType: NavigationSuiteType
) {
    val lessonTimePeriodInfo = schedule.lessonTimePeriodInfo.morning +
            schedule.lessonTimePeriodInfo.afternoon +
            schedule.lessonTimePeriodInfo.evening

    Box(modifier = modifier.fillMaxWidth()) {
        val state = remember { mutableStateOf(TableState()) }
        
        // 自动重置状态的逻辑
        LaunchedEffect(state.value) {
            if (state.value.isClicked == 1) {
                val current = state.value
                delay(10000) // 10秒
                if (state.value == current) {
                    state.value = TableState()
                }
            }
        }
        // 底层：表格框架
        TableGrid(
            state = state,
            modifier = Modifier.zIndex(1f),
            rows = rows,
            columns = columns,
            dateList = schedule.dateList(currentWeek),
            lessonTimePeriodInfo = lessonTimePeriodInfo
        )

        // 覆盖层：课程内容
        CourseOverlay(
            viewModel = viewModel,
            state = state,
            rows = rows,
            columns = columns,
            schedule = schedule,
            currentWeek = currentWeek,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f),
            navHostController = navHostController,
            navSuiteType = navSuiteType
        )
    }
}

@Composable
fun TableGrid(
    state: MutableState<TableState>,
    modifier: Modifier = Modifier,
    rows: Int,
    columns: Int,
    dateList: List<LocalDate>,
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

    Row(modifier = modifier.fillMaxWidth()) {
        // 时间列
        TimeColumn(
            modifier = Modifier.width(48.dp),
            rows = rows,
            allLessons = lessonTimePeriodInfo
        )

        // 每一天的空白列（只显示表头和边框）
        for (dayIndex in 0 until columns) {
            EmptyDayColumn(
                state = state,
                modifier = Modifier.weight(1f),
                dayName = weekdays[dayIndex],
                date = dateList[dayIndex],
                dayIndex = dayIndex,
                rows = rows,
                isLastDay = dayIndex == columns - 1
            )
        }
    }
}

@Composable
fun TimeColumn(
    modifier: Modifier = Modifier,
    rows: Int,
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
        for (lessonIndex in 0 until rows) {
            TableCell(
                isRightBorder = true,
                isBottomBorder = lessonIndex < rows - 1
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
    date: LocalDate,
    dayIndex: Int,
    rows: Int,
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val color = if (date == Clock.System.todayIn(TimeZone.currentSystemDefault())) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = color
                )
                Text(
                    text = "${date.monthNumber}/${date.dayOfMonth.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = color.copy(alpha = 0.6f)
                )
            }
        }

        // 空白课程单元格
        for (lessonIndex in 1..rows) {
            TableCell(
                isRightBorder = !isLastDay,
                isBottomBorder = lessonIndex < rows
            ) {
                // 空白单元格，只显示边框
                if (state.value.row != lessonIndex ||
                    state.value.column != dayIndex + 1
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        colors = CardDefaults.cardColors().copy(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) { }
                }
            }
        }
    }
}

@Composable
fun CourseOverlay(
    viewModel: TimeFlowViewModel,
    state: MutableState<TableState>,
    rows: Int,
    columns: Int,
    schedule: Schedule,
    currentWeek: Int,
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    navSuiteType: NavigationSuiteType
) {
    Row(modifier = modifier) {
        // 时间列占位（与底层对齐）
        Box(modifier = Modifier.width(48.dp))

        // 课程覆盖层
        for (dayIndex in 0 until columns) {
            CourseColumn(
                viewModel = viewModel,
                state = state,
                modifier = Modifier.weight(1f),
                dayIndex = dayIndex,
                rows = rows,
                schedule = schedule,
                currentWeek = currentWeek,
                navHostController = navHostController,
                navSuiteType = navSuiteType
            )
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
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    end = if (isRightBorder) borderWidth else 0.dp,
                    bottom = if (isBottomBorder) borderWidth else 0.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun EmptyTableCell(
    state: MutableState<TableState>,
    index: Int,
    dayIndex: Int,
    totalWeeks: Int,
    onClick: (Course) -> Unit
) {
    AnimatedContent(
        state.value,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            fadeIn(
                animationSpec = tween(300)
            ) togetherWith fadeOut(animationSpec = tween(300))
        }
    ) {
        if (it.row == index && it.column == dayIndex + 1 && it.isClicked != 0) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            onClick(
                                Course(
                                    name = "",
                                    time = Range(
                                        index,
                                        index
                                    ),
                                    weekday = weekdays[dayIndex],
                                    week = WeekList(
                                        weekDescription = WeekDescriptionEnum.ALL,
                                        totalWeeks = totalWeeks
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
        } else {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            state.value = TableState(
                                row = index,
                                column = dayIndex + 1,
                                isClicked = 1 // 点击中
                            )
                        }
                )
            }
        }
    }
}

@Composable
fun EditCourseDialog(
    state: MutableState<TableState>,
    viewModel: TimeFlowViewModel,
    initValue: Course,
    showEditCourseDialog: DialogState
) {
    val settings by viewModel.settings.collectAsState()
    val schedule = settings.schedule[settings.selectedSchedule]!!
    val newCourse = remember { mutableStateOf(initValue) }
    var isNameValid =
        remember { mutableStateOf(newCourse.value.name.isNotBlank()) }
    val isTimeValid = remember {
        mutableStateOf(
            if (newCourse.value.time.start > newCourse.value.time.end) {
                false
            } else {
                schedule.courses.none {
                    it != initValue && it.time.start <= newCourse.value.time.end && it.time.end >= newCourse.value.time.start && it.weekday == newCourse.value.weekday && it.week.week.any { it in newCourse.value.week.week }
                }
            }
        )
    }
    val validWeeks = (1..schedule.totalWeeks()).toMutableList().let {
        it - schedule.courses.filter {
            it != initValue && it.time.start <= newCourse.value.time.end && it.time.end >= newCourse.value.time.start && it.weekday == newCourse.value.weekday
        }.flatMap { it.week.week }
    }
    val isWeekValid =
        remember { mutableStateOf(newCourse.value.week.week.isNotEmpty() && newCourse.value.week.week.all { it in validWeeks }) }
    showEditCourseDialog.enableButton(
        button = DialogButtonType.Positive,
        enabled = isNameValid.value && isTimeValid.value && isWeekValid.value
    )
    MyDialog(
        state = showEditCourseDialog,
        title = {
            Text(
                text = stringResource(Res.string.schedule_title_edit_course)
            )
        },
        buttons = DialogDefaults.buttons(
            positive = DialogButton(stringResource(Res.string.save)),
            negative = DialogButton(stringResource(Res.string.cancel)),
        ),
        onEvent = { event ->
            if (event.isPositiveButton) {
                viewModel.updateSchedule(
                    schedule = schedule.copy(
                        courses = if (initValue in schedule.courses) {
                            schedule.courses.map { if (it == initValue) newCourse.value else it }
                        } else {
                            schedule.courses + newCourse.value
                        }
                    )
                )
            }
            state.value = TableState() // 重置状态
        }
    ) {
        EditCourseContent(
            style = EditCourseStyle.Dialog,
            viewModel = viewModel,
            initValue = initValue,
            courseValue = newCourse,
            isNameValid = isNameValid,
            isTimeValid = isTimeValid,
            isWeekValid = isWeekValid,
            validWeeks = validWeeks
        )
    }
}

@Composable
fun CourseColumn(
    viewModel: TimeFlowViewModel,
    state: MutableState<TableState>,
    modifier: Modifier,
    dayIndex: Int,
    rows: Int,
    schedule: Schedule,
    currentWeek: Int,
    navHostController: NavHostController,
    navSuiteType: NavigationSuiteType
) {
    val showEditCourseDialog = rememberDialogState()
    val renderedTimeSlots = mutableSetOf<Int>()
    val daySchedule = schedule.courses.filter {
        it.weekday == weekdays[dayIndex]
    }
    val dayScheduleTimeForCurrentWeek = mutableSetOf<Range>()
    val dayScheduleTimeForOtherWeek = mutableSetOf<Range>()
    daySchedule.forEach {
        if (it.week.week.contains(currentWeek)) {
            dayScheduleTimeForCurrentWeek.add(it.time)
        } else {
            dayScheduleTimeForOtherWeek.add(it.time)
        }
    }
    var selectedCourse by remember {
        mutableStateOf(
            Course(
                name = "",
                time = Range(
                    1,
                    1
                ),
                weekday = weekdays[dayIndex],
                week = WeekList(
                    weekDescription = WeekDescriptionEnum.ALL,
                    totalWeeks = schedule.totalWeeks()
                )
            )
        )
    }

    Box(modifier = modifier) {
        // 当前周的课程
        dayScheduleTimeForCurrentWeek.forEach { time ->
            val courseForThisTime = daySchedule.filter { course ->
                course.time == time && course.week.week.contains(currentWeek)
            }
            renderedTimeSlots.addAll(time.start..time.end)
            Box(
                modifier = Modifier
                    .height(64.dp * (time.end - time.start + 1))
                    .padding(end = 1.dp, bottom = 1.dp)
                    .offset(
                        y = 40.dp + ((time.start - 1) * 64).dp
                    )
                    .zIndex(100f),
                contentAlignment = Alignment.Center
            ) {
                CourseCell(courseForThisTime)
            }
        }
        // 非当前周的课程
        dayScheduleTimeForOtherWeek.sortedBy { it.end - it.start }.forEachIndexed { _, time ->
            if ((time.start..time.end).all { it in renderedTimeSlots }) {
                return@forEachIndexed // 已经渲染过的时间段跳过
            }
            val courseForThisTime = daySchedule.filter { course ->
                course.time == time && !course.week.week.contains(currentWeek)
            }
            // 找出第一个未渲染的时间段
            val firstSpaceToDisplay = (time.start..time.end).first { it !in renderedTimeSlots }
            var lastSpaceToDisplay = firstSpaceToDisplay
            (firstSpaceToDisplay..time.end).forEach {
                if (it !in renderedTimeSlots) {
                    lastSpaceToDisplay = it
                } else {
                    return@forEach // 找到第一个空位后停止
                }
            }
            renderedTimeSlots.addAll(time.start..time.end)
            Box(
                modifier = Modifier
                    .height(64.dp * (time.end - time.start + 1))
                    .padding(end = 1.dp, bottom = 1.dp)
                    .offset(
                        y = 40.dp + ((time.start - 1) * 64).dp
                    )
                    .zIndex((99 - (time.end - time.start + 1)).toFloat()) // 渲染顺序
                ,
                contentAlignment = Alignment.Center
            ) {
                CourseCell(
                    courseForThisTime,
                    displayOffSet = 64.dp * (firstSpaceToDisplay - time.start),
                    displayHeight = ((lastSpaceToDisplay - firstSpaceToDisplay + 1) * 64).dp
                )
            }
        }
        // 渲染空白单元格
        (1..rows).filter { it !in renderedTimeSlots }.forEach { index ->
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .padding(end = 1.dp, bottom = 1.dp)
                    .offset(
                        y = 40.dp + ((index - 1) * 64).dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                EmptyTableCell(
                    state = state,
                    index = index,
                    dayIndex = dayIndex,
                    totalWeeks = schedule.totalWeeks(),
                    onClick = { course ->
                        state.value = state.value.copy(
                            isClicked = 2, // 点击后等待重置
                        )
                        selectedCourse = course
                        if (navSuiteType !in NavigationBarType) {
                            showEditCourseDialog.show()
                        } else {
                            navHostController.navigate(
                                EditCourseDestination(
                                    selectedCourse
                                )
                            )
                        }
                    }
                )
            }
        }
    }
    if (showEditCourseDialog.visible) {
        EditCourseDialog(
            state = state,
            viewModel = viewModel,
            initValue = schedule.courses.firstOrNull {
                it.weekday == weekdays[dayIndex] && it.time.start == state.value.row
            } ?: Course(
                name = "",
                time = Range(state.value.row, state.value.row),
                weekday = weekdays[dayIndex],
                week = WeekList(
                    weekDescription = WeekDescriptionEnum.ALL,
                    totalWeeks = schedule.totalWeeks()
                )
            ),
            showEditCourseDialog = showEditCourseDialog
        )
    }
}

@Composable
fun CourseCell(
    coursesForThisTime: List<Course>,
    displayOffSet: Dp = 0.dp,
    displayHeight: Dp = coursesForThisTime.first().time.let { (it.end - it.start + 1) * 64 }.dp
) {
    val course = coursesForThisTime.first()
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        onClick = {
            // TODO: Dialog to show course details, edit and add new course
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(displayHeight)
                    .offset(
                        y = displayOffSet
                    )
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                if (course.classroom.isNotBlank()) {
                    Text(
                        text = "@${course.classroom}",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                if (course.teacher.isNotBlank()) {
                    Text(
                        text = course.teacher,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
