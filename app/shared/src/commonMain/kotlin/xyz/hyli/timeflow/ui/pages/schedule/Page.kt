/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.schedule

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.LocalNavSuiteType
import xyz.hyli.timeflow.data.*
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.ui.components.CustomScaffold
import xyz.hyli.timeflow.ui.components.TopAppBarType
import xyz.hyli.timeflow.ui.components.bottomPadding
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.navigation.Destination
import xyz.hyli.timeflow.ui.navigation.NavigationBarType
import xyz.hyli.timeflow.ui.theme.NotoSans
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

private val BASE_CELL_HEIGHT = 56.dp
private val NAME_LINE_HEIGHT = 16.dp
private val INFO_LINE_HEIGHT = 14.dp
private const val NAME_CHAR_WIDTH = 12f
private const val INFO_CHAR_WIDTH = 11f
private const val CELL_CONTENT_PADDING = 12f

private fun estimateTextLines(text: String, availableWidthDp: Float, charWidthDp: Float): Int {
    if (text.isEmpty()) return 0
    var totalWidth = 0f
    for (c in text) {
        totalWidth += if (c.code > 0x7F) charWidthDp else charWidthDp * 0.6f
    }
    return maxOf(1, kotlin.math.ceil((totalWidth / availableWidthDp).toDouble()).toInt())
}

private fun computeRowYOffsets(rowHeights: List<Dp>): List<Dp> {
    val offsets = mutableListOf(0.dp)
    rowHeights.forEach { h -> offsets.add(offsets.last() + h) }
    return offsets
}

data class ScheduleLayoutParams(
    val headerWidth: MutableState<Dp>,
    val headerHeight: MutableState<Dp>,
    val cellWidth: Dp,
    val rows: Int,
    val columns: Int,
    val noGridCells: MutableState<List<Set<Int>>>,
    val rowHeights: List<Dp>,
    val rowYOffsets: List<Dp>
)

data class ScheduleParams(
    val viewModel: TimeFlowViewModel,
    val navHostController: NavHostController,
    val schedule: Schedule,
    val currentWeek: Int
)

data class TableState(
    val row: Int = 0,
    val column: Int = 0,
    val isClicked: Int = 0 // 0: 未点击, 1: 点击中, 2: 点击后等待重置
)

val weekdays = listOf(
    Weekday.MONDAY,
    Weekday.TUESDAY,
    Weekday.WEDNESDAY,
    Weekday.THURSDAY,
    Weekday.FRIDAY,
    Weekday.SATURDAY,
    Weekday.SUNDAY
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController,
) {
    val settings by viewModel.settings.collectAsState()
    val schedule by viewModel.selectedSchedule.collectAsState()
    val columns = if (schedule?.displayWeekends == true) 7 else 5
    val rows = schedule?.lessonTimePeriodInfo?.totalLessonsCount
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    @Composable
    fun BoxScope.ScheduleScreenContent(
        scrollState: ScrollState
    ) {
        if (schedule == null || rows == null || rows == 0) {
            // 显示空状态或错误信息
            Text(
                modifier = Modifier.align(Alignment.Center),
                text =
                    if (settings.isScheduleEmpty)
                        stringResource(Res.string.settings_subtitle_schedule_empty)
                    else
                        stringResource(Res.string.settings_subtitle_schedule_not_selected),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            return
        }
        var isOverviewMode by remember { mutableStateOf(false) }
        val pagerState = schedule!!.totalWeeks.let {
            rememberPagerState(
                initialPage = (schedule!!.termStartDate.weeksTill() - 1).coerceIn(
                    0,
                    it
                ), // page 0 for week 1
                pageCount = {
                    it + 1 // +1 for vacation
                }
            )
        }
        val coroutineScope = rememberCoroutineScope()
        val showAddScheduleDialog = remember { mutableStateOf(false) }
        CustomScaffold(
            modifier = Modifier.fillMaxSize(),
            title = {
                if (isOverviewMode) {
                    Text(
                        text = stringResource(Res.string.schedule_title_overview),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        SubcomposeLayout { constraints ->
                            val list = listOf(
                                "part1" to Res.string.schedule_title_week_x_part_1,
                                "part2" to Res.string.schedule_title_week_x_part_2,
                                "part3" to Res.string.schedule_title_week_x_part_3,
                                "vacation" to Res.string.schedule_title_week_vacation
                            ).map {
                                subcompose(it.first) {
                                    Text(
                                        text =
                                            if (it.first == "part2") "${pagerState.currentPage + 1}"
                                            else stringResource(it.second),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }[0].measure(constraints)
                            }
                            val part2Max = subcompose("part2max") {
                                Text(
                                    text = stringResource(
                                        Res.string.schedule_title_week_x_part_2,
                                        pagerState.pageCount + 1
                                    ),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }[0].measure(constraints)
                            val width =
                                maxOf(
                                    list[0].width + part2Max.width + list[2].width,
                                    list[3].width
                                )
                            layout(width, constraints.minHeight) {
                                if (pagerState.currentPage < pagerState.pageCount - 1) {
                                    list[0].placeRelative(
                                        0,
                                        -list[0].height / 2
                                    )
                                    list[1].placeRelative(
                                        (width - list[1].width) / 2,
                                        -list[1].height / 2
                                    )
                                    list[2].placeRelative(
                                        width - list[2].width,
                                        -list[2].height / 2
                                    )
                                } else {
                                    list[3].placeRelative(
                                        (width - list[3].width) / 2,
                                        -list[3].height / 2
                                    )
                                }
                            }
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
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        navHostController.navigate(Destination.Schedule.ScheduleList)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.SyncAlt,
                        contentDescription = stringResource(Res.string.save)
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { isOverviewMode = !isOverviewMode },
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarViewMonth,
                        contentDescription = stringResource(Res.string.schedule_title_overview)
                    )
                }
                IconButton(
                    onClick = {
                        showAddScheduleDialog.value = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.save)
                    )
                }
            },
            topAppBarType = TopAppBarType.CenterAligned,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.fillMaxWidth(0.75f)
                )
            }
        ) {
            AnimatedContent(
                targetState = isOverviewMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                }
            ) { overview ->
                if (overview) {
                    OverviewScheduleTable(
                        schedule = schedule!!,
                        navHostController = navHostController,
                        viewModel = viewModel,
                        rows = rows,
                        columns = columns,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .bottomPadding(extra = 56.dp)
                    )
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .bottomPadding(extra = 56.dp)
                    ) { page ->
                        ScheduleTable(
                            scheduleParams = ScheduleParams(
                                viewModel = viewModel,
                                navHostController = navHostController,
                                schedule = schedule!!,
                                currentWeek = page + 1
                            ),
                            rows = rows,
                            columns = columns,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        if (showAddScheduleDialog.value) {
            AddScheduleDialog(
                state = showAddScheduleDialog,
                viewModel = viewModel
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
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (it) {
                false -> {
                    LoadingIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(96.dp)
                    )
                }

                true -> {
                    val scrollState = rememberScrollState()
                    var fabVisible by remember { mutableStateOf(true) }
                    var lastScroll by remember { mutableStateOf(0) }
                    LaunchedEffect(scrollState.value) {
                        val delta = scrollState.value - lastScroll
                        if (delta > 0) fabVisible = false
                        else if (delta < 0) fabVisible = true
                        lastScroll = scrollState.value
                    }
                    ScheduleScreenContent(scrollState)
                    ScheduleFAB(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        viewModel = viewModel,
                        visible = fabVisible,
                        showMessage = { message ->
                            scope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleTable(
    scheduleParams: ScheduleParams,
    rows: Int,
    columns: Int,
    modifier: Modifier = Modifier
) {
    val lessons = scheduleParams.schedule.lessonTimePeriodInfo.lessons
    val headerWidth = remember { mutableStateOf(48.dp) }
    val headerHeight = remember { mutableStateOf(40.dp) }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val state = remember { mutableStateOf(TableState()) }
        val cellWidth = (maxWidth - headerWidth.value - 5.dp) / columns
        val availableTextWidth = (cellWidth - CELL_CONTENT_PADDING.dp).value

        val rowHeights = remember(scheduleParams.schedule, rows, cellWidth) {
            val heights = MutableList(rows) { BASE_CELL_HEIGHT }
            for (dayIndex in 0 until columns) {
                val courses = scheduleParams.schedule.getCoursesOfWeekday(weekdays[dayIndex])
                val timeSlots = courses.values.map { it.time }.toSet()
                for (time in timeSlots) {
                    val span = time.end - time.start + 1
                    val maxCourseHeight = courses.values
                        .filter { it.time == time }
                        .maxOfOrNull { course ->
                            val nameLines =
                                minOf(3, estimateTextLines(course.name, availableTextWidth, NAME_CHAR_WIDTH))
                            var h = NAME_LINE_HEIGHT * nameLines
                            if (course.classroom.isNotBlank()) {
                                h += INFO_LINE_HEIGHT * minOf(
                                    2,
                                    estimateTextLines("@${course.classroom}", availableTextWidth, INFO_CHAR_WIDTH)
                                )
                            }
                            if (course.teacher.isNotBlank()) {
                                h += INFO_LINE_HEIGHT
                            }
                            h + 8.dp
                        } ?: continue
                    val baseSpanHeight = BASE_CELL_HEIGHT * span
                    if (maxCourseHeight > baseSpanHeight) {
                        val perRowExtra = (maxCourseHeight - baseSpanHeight) / span
                        for (i in (time.start - 1) until time.end) {
                            if (i in heights.indices) {
                                heights[i] = maxOf(heights[i], BASE_CELL_HEIGHT + perRowExtra)
                            }
                        }
                    }
                }
            }
            heights
        }

        val rowYOffsets = remember(rowHeights) { computeRowYOffsets(rowHeights) }
        val totalHeight = rowYOffsets.last()

        val noGridCells = remember {
            mutableStateOf(
                List(rows) { setOf<Int>() }
            )
        }
        // 自动重置状态的逻辑 - 修复无限循环问题
        LaunchedEffect(state.value) {
            if (state.value.isClicked == 1) {
                val current = state.value
                delay(10000) // 10秒
                if (state.value == current) {
                    state.value = TableState()
                }
            }
        }
        val layoutParams = ScheduleLayoutParams(
            headerWidth = headerWidth,
            headerHeight = headerHeight,
            cellWidth = cellWidth,
            rows = rows,
            columns = columns,
            noGridCells = noGridCells,
            rowHeights = rowHeights,
            rowYOffsets = rowYOffsets
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight.value + totalHeight)
        ) {
            // 底层：表格框架
            TableGrid(
                layoutParams = layoutParams,
                dateList = scheduleParams.schedule.dateList(scheduleParams.currentWeek),
                lessonTimePeriodInfo = lessons
            )

            // 覆盖层：课程内容
            CourseOverlay(
                layoutParams = layoutParams,
                scheduleParams = scheduleParams,
                state = state
            )
        }
    }
}

@Composable
fun CourseOverlay(
    layoutParams: ScheduleLayoutParams,
    scheduleParams: ScheduleParams,
    state: MutableState<TableState>
) {
    // 课程覆盖层
    for (dayIndex in 0 until layoutParams.columns) {
        CourseColumn(
            layoutParams = layoutParams,
            scheduleParams = scheduleParams,
            state = state,
            dayIndex = dayIndex
        )
    }
}

@Composable
fun CourseColumn(
    layoutParams: ScheduleLayoutParams,
    scheduleParams: ScheduleParams,
    state: MutableState<TableState>,
    dayIndex: Int
) {
    val navSuiteType by LocalNavSuiteType.current
    val showEditCourseDialog = rememberDialogState()
    val renderedTimeSlots = mutableSetOf<Int>()
    var noGridCells by layoutParams.noGridCells
    var selectCourse by remember { mutableStateOf<Course?>(null) }
    var selectCourseID by remember { mutableStateOf<Short?>(null) }

    val (timeSlotsForCurrentWeek, timeSlotsForOtherWeek, timeSlots) = remember(
        scheduleParams.schedule,
        scheduleParams.currentWeek
    ) {
        scheduleParams.schedule.getTimeSlotsFor(weekdays[dayIndex], scheduleParams.currentWeek)
    }

    val emptySlots = remember(layoutParams.rows, renderedTimeSlots) {
        (1..layoutParams.rows).filterNot { it in renderedTimeSlots }
    }

    LaunchedEffect(timeSlots) {
        noGridCells = noGridCells.toMutableList().apply {
            this[dayIndex] = timeSlots.flatMapTo(mutableSetOf()) { time ->
                if (time.end - time.start > 0) {
                    (time.start until time.end).map { it + 1 }
                } else {
                    emptyList()
                }
            }
        }
    }

    fun editCourse(courseID: Short, course: Course) {
        if (navSuiteType !in NavigationBarType) {
            selectCourse = course
            selectCourseID = courseID
            showEditCourseDialog.show()
        } else {
            scheduleParams.navHostController.navigate(
                Destination.Schedule.EditCourse(
                    courseID = courseID,
                    course = course
                )
            )
        }
    }

    if (showEditCourseDialog.visible) {
        EditCourseDialog(
            state = state,
            scheduleParams = scheduleParams,
            courseID = selectCourseID!!,
            initValue = selectCourse!!,
            showEditCourseDialog = showEditCourseDialog
        )
    }

    Box(
        modifier = Modifier
            .padding(end = 1.dp)
            .width(layoutParams.cellWidth - 1.dp)
            .offset(
                x = layoutParams.headerWidth.value + 5.dp + layoutParams.cellWidth * dayIndex,
                y = layoutParams.headerHeight.value + 1.dp
            )
    ) {
        // 当前周的课程
        timeSlotsForCurrentWeek.forEach { time ->
            renderedTimeSlots.addAll(time.start..time.end)
            val courses = remember(scheduleParams.schedule, time, scheduleParams.currentWeek) {
                scheduleParams.schedule.getCoursesAt(
                    time,
                    weekdays[dayIndex],
                    scheduleParams.currentWeek
                )
            }
            val coursesForThisTime = remember(scheduleParams.schedule, time) {
                scheduleParams.schedule.getCoursesOverlapping(time, weekdays[dayIndex])
            }
            val cellHeight = layoutParams.rowYOffsets[time.end] - layoutParams.rowYOffsets[time.start - 1]
            AnimatedContent(
                targetState = courses.isNotEmpty(),
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(300)
                    ) togetherWith fadeOut(animationSpec = tween(300))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellHeight)
                    .padding(bottom = 1.dp)
                    .offset(
                        y = layoutParams.rowYOffsets[time.start - 1] - 1.dp
                    )
                    .zIndex(100f),
                contentAlignment = Alignment.Center
            ) {
                if (it) {
                    CourseCell(
                        schedule = scheduleParams.schedule,
                        courses = courses,
                        coursesForThisTime = coursesForThisTime,
                        currentWeek = scheduleParams.currentWeek,
                        totalWeeks = scheduleParams.schedule.totalWeeks,
                        displayHeight = cellHeight,
                        onEditCourse = { courseID, course ->
                            editCourse(courseID, course)
                        },
                        onCreateNewCourse = { course ->
                            val newCourseID = scheduleParams.schedule.newCourseId()
                            editCourse(newCourseID, course)
                        }
                    )
                }
            }
        }
        // 非当前周的课程
        timeSlotsForOtherWeek.forEach { time ->
            if ((time.start..time.end).all { it in renderedTimeSlots }) {
                return@forEach // 已经渲染过的时间段跳过
            }
            // 找出第一个未渲染的时间段
            val (firstSpaceToDisplay, lastSpaceToDisplay) = remember(time, renderedTimeSlots) {
                val first = (time.start..time.end).first { it !in renderedTimeSlots }
                var last = first
                (first..time.end).forEach { slot ->
                    if (slot !in renderedTimeSlots) {
                        last = slot
                    } else {
                        return@remember Pair(first, last)
                    }
                }
                Pair(first, last)
            }
            renderedTimeSlots.addAll(time.start..time.end)
            val courses = remember(scheduleParams.schedule, time, scheduleParams.currentWeek) {
                scheduleParams.schedule.getOtherWeekCoursesAt(
                    time,
                    weekdays[dayIndex],
                    scheduleParams.currentWeek
                )
            }
            val coursesForThisTime = remember(scheduleParams.schedule, time) {
                scheduleParams.schedule.getCoursesOverlapping(time, weekdays[dayIndex])
            }
            val spanHeight = layoutParams.rowYOffsets[time.end] - layoutParams.rowYOffsets[time.start - 1]
            AnimatedContent(
                targetState = courses.isNotEmpty(),
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(300)
                    ) togetherWith fadeOut(animationSpec = tween(300))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spanHeight)
                    .padding(bottom = 1.dp)
                    .offset(
                        y = layoutParams.rowYOffsets[time.start - 1] - 1.dp
                    )
                    .zIndex(99 - time.start + 1.0f / (time.end - time.start)) // 渲染顺序
                ,
                contentAlignment = Alignment.Center
            ) {
                if (it) {
                    CourseCell(
                        schedule = scheduleParams.schedule,
                        courses = courses,
                        coursesForThisTime = coursesForThisTime,
                        currentWeek = scheduleParams.currentWeek,
                        totalWeeks = scheduleParams.schedule.totalWeeks,
                        displayOffSet = layoutParams.rowYOffsets[firstSpaceToDisplay - 1] - layoutParams.rowYOffsets[time.start - 1],
                        displayHeight = layoutParams.rowYOffsets[lastSpaceToDisplay] - layoutParams.rowYOffsets[firstSpaceToDisplay - 1],
                        onEditCourse = { courseID, course ->
                            editCourse(courseID, course)
                        },
                        onCreateNewCourse = { course ->
                            val newCourseID = scheduleParams.schedule.newCourseId()
                            editCourse(newCourseID, course)
                        }
                    )
                }
            }
        }
        // 渲染空白单元格
        emptySlots.forEach { index ->
            Box(
                modifier = Modifier
                    .height(layoutParams.rowHeights[index - 1])
                    .padding(bottom = 1.dp)
                    .offset(
                        y = layoutParams.rowYOffsets[index - 1] - 1.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                EmptyTableCell(
                    state = state,
                    index = index,
                    dayIndex = dayIndex,
                    totalWeeks = scheduleParams.schedule.totalWeeks,
                    onCreateNewCourse = { course ->
                        state.value = state.value.copy(
                            isClicked = 2, // 点击后等待重置
                        )
                        val courseID = scheduleParams.schedule.newCourseId()
                        editCourse(courseID, course)
                    }
                )
            }
        }
    }
}

@Composable
fun OverviewScheduleTable(
    schedule: Schedule,
    navHostController: NavHostController,
    viewModel: TimeFlowViewModel,
    rows: Int,
    columns: Int,
    modifier: Modifier = Modifier
) {
    val lessons = schedule.lessonTimePeriodInfo.lessons
    val interEntrySpacing = 9.dp // Spacer(4dp) + HorizontalDivider(0.5dp) + Spacer(4dp)
    val columnPadding = 8.dp     // Column padding 4dp top + 4dp bottom

    // Collect overview slots for all weekdays
    val overviewData = remember(schedule) {
        List(columns) { dayIndex ->
            schedule.getOverviewTimeSlotsFor(weekdays[dayIndex])
        }
    }

    val headerWidth = remember { mutableStateOf(48.dp) }
    val headerHeight = remember { mutableStateOf(40.dp) }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val cellWidth = (maxWidth - headerWidth.value - 5.dp) / columns
        val availableTextWidth = (cellWidth - CELL_CONTENT_PADDING.dp).value

        // Compute dynamic row heights based on actual content and cell width
        val rowHeights = remember(overviewData, rows, cellWidth) {
            val heights = MutableList(rows) { BASE_CELL_HEIGHT }
            for (daySlots in overviewData) {
                for ((range, courses) in daySlots) {
                    if (courses.isEmpty()) continue
                    val span = range.end - range.start + 1
                    var neededHeight = columnPadding
                    courses.values.forEachIndexed { index, course ->
                        if (index > 0) neededHeight += interEntrySpacing
                        neededHeight += NAME_LINE_HEIGHT * minOf(
                            3,
                            estimateTextLines(course.name, availableTextWidth, NAME_CHAR_WIDTH)
                        )
                        val weekTextLen = course.week.toString().length + 4
                        neededHeight += INFO_LINE_HEIGHT * estimateTextLines(
                            "x".repeat(weekTextLen),
                            availableTextWidth,
                            INFO_CHAR_WIDTH
                        )
                        neededHeight += INFO_LINE_HEIGHT // time period
                        if (course.classroom.isNotBlank()) {
                            neededHeight += INFO_LINE_HEIGHT * minOf(
                                2,
                                estimateTextLines(course.classroom, availableTextWidth, INFO_CHAR_WIDTH)
                            )
                        }
                        if (course.teacher.isNotBlank()) {
                            neededHeight += INFO_LINE_HEIGHT
                        }
                    }
                    val baseSpanHeight = BASE_CELL_HEIGHT * span
                    if (neededHeight > baseSpanHeight) {
                        val perRowExtra = (neededHeight - baseSpanHeight) / span
                        for (i in (range.start - 1) until range.end) {
                            if (i in heights.indices) {
                                heights[i] = maxOf(heights[i], BASE_CELL_HEIGHT + perRowExtra)
                            }
                        }
                    }
                }
            }
            heights
        }

        val rowYOffsets = remember(rowHeights) { computeRowYOffsets(rowHeights) }
        val totalHeight = rowYOffsets.last()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight.value + totalHeight)
        ) {

            // Grid: weekday headers (no dates)
            OverviewTableHeader(
                columns = columns,
                cellWidth = cellWidth,
                headerWidth = headerWidth,
                headerHeight = headerHeight
            )

            // Grid: row headers and horizontal dividers
            OverviewRowHeaders(
                rows = rows,
                rowHeights = rowHeights,
                rowYOffsets = rowYOffsets,
                headerWidth = headerWidth,
                headerHeight = headerHeight,
                lessons = lessons
            )

            // Grid: vertical dividers
            for (dayIndex in 0 until columns) {
                VerticalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier
                        .offset(
                            x = headerWidth.value + 4.dp + cellWidth * dayIndex,
                            y = 0.dp
                        )
                        .height(headerHeight.value + totalHeight)
                )
            }

            // Grid: horizontal dividers per column
            for (dayIndex in 0 until columns) {
                val noGridRows = remember(overviewData, dayIndex) {
                    val set = mutableSetOf<Int>()
                    overviewData[dayIndex].keys.forEach { range ->
                        if (range.end - range.start > 0) {
                            (range.start until range.end).forEach { set.add(it + 1) }
                        }
                    }
                    set
                }
                Box(
                    modifier = Modifier
                        .width(cellWidth)
                        .offset(x = headerWidth.value + 4.dp + cellWidth * dayIndex)
                ) {
                    for (lessonIndex in 0 until rows) {
                        if (lessonIndex + 1 in noGridRows) continue
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.offset(
                                y = headerHeight.value + rowYOffsets[lessonIndex] - 1.dp
                            )
                        )
                    }
                }
            }

            // Course overlay
            val navSuiteType by LocalNavSuiteType.current
            val showEditCourseDialog = rememberDialogState()
            var selectCourse by remember { mutableStateOf<Course?>(null) }
            var selectCourseID by remember { mutableStateOf<Short?>(null) }
            val showCourseListDialog = rememberDialogState()
            var courseListDialogCourses by remember { mutableStateOf<Map<Short, Course>>(emptyMap()) }
            var courseListDialogTime by remember { mutableStateOf(Range(1, 1)) }

            fun editCourse(courseID: Short, course: Course) {
                if (navSuiteType !in NavigationBarType) {
                    selectCourse = course
                    selectCourseID = courseID
                    showEditCourseDialog.show()
                } else {
                    navHostController.navigate(
                        Destination.Schedule.EditCourse(
                            courseID = courseID,
                            course = course
                        )
                    )
                }
            }

            if (showEditCourseDialog.visible && selectCourse != null && selectCourseID != null) {
                val state = remember { mutableStateOf(TableState()) }
                EditCourseDialog(
                    state = state,
                    scheduleParams = ScheduleParams(
                        viewModel = viewModel,
                        navHostController = navHostController,
                        schedule = schedule,
                        currentWeek = 1
                    ),
                    courseID = selectCourseID!!,
                    initValue = selectCourse!!,
                    showEditCourseDialog = showEditCourseDialog
                )
            }

            if (showCourseListDialog.visible) {
                CourseListDialog(
                    schedule = schedule,
                    courses = courseListDialogCourses,
                    currentWeek = 1,
                    totalWeeks = schedule.totalWeeks,
                    time = courseListDialogTime,
                    showCourseListDialog = showCourseListDialog,
                    onEditCourse = { courseID, course -> editCourse(courseID, course) },
                    onCreateNewCourse = { course ->
                        val newCourseID = schedule.newCourseId()
                        editCourse(newCourseID, course)
                    }
                )
            }

            for (dayIndex in 0 until columns) {
                Box(
                    modifier = Modifier
                        .padding(end = 1.dp)
                        .width(cellWidth - 1.dp)
                        .offset(
                            x = headerWidth.value + 5.dp + cellWidth * dayIndex,
                            y = headerHeight.value + 1.dp
                        )
                ) {
                    overviewData[dayIndex].forEach { (range, courses) ->
                        if (courses.isEmpty()) return@forEach
                        val cellHeight = rowYOffsets[range.end] - rowYOffsets[range.start - 1]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(cellHeight)
                                .padding(bottom = 1.dp)
                                .offset(y = rowYOffsets[range.start - 1] - 1.dp)
                        ) {
                            OverviewCourseCell(
                                courses = courses,
                                onClick = {
                                    courseListDialogCourses = courses
                                    courseListDialogTime = range
                                    showCourseListDialog.show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewTableHeader(
    columns: Int,
    cellWidth: Dp,
    headerWidth: MutableState<Dp>,
    headerHeight: MutableState<Dp>
) {
    val weekdayNames = listOf(
        Res.string.monday,
        Res.string.tuesday,
        Res.string.wednesday,
        Res.string.thursday,
        Res.string.friday,
        Res.string.saturday,
        Res.string.sunday
    ).map { stringResource(it) }

    Box(modifier = Modifier.fillMaxWidth()) {
        for (dayIndex in 0 until columns) {
            SubcomposeLayout(
                modifier = Modifier.padding(end = 1.dp)
            ) { constraints ->
                val column = subcompose("overview_day$dayIndex") {
                    Column(
                        modifier = Modifier
                            .width(cellWidth - 1.dp)
                            .height(IntrinsicSize.Max)
                            .offset(
                                x = headerWidth.value + 5.dp + cellWidth * dayIndex,
                                y = 0.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = weekdayNames[dayIndex],
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }[0].measure(constraints)
                val newHeight = column.height.toDp()
                if (headerHeight.value != newHeight) {
                    headerHeight.value = newHeight
                }
                layout(cellWidth.roundToPx(), column.height) {
                    column.placeRelative(0, 0)
                }
            }
        }
    }
}

@Composable
private fun OverviewRowHeaders(
    rows: Int,
    rowHeights: List<Dp>,
    rowYOffsets: List<Dp>,
    headerWidth: MutableState<Dp>,
    headerHeight: MutableState<Dp>,
    lessons: List<Lesson>
) {
    Column(
        modifier = Modifier.offset(
            x = 1.dp,
            y = headerHeight.value - 1.dp
        )
    ) {
        for (lessonIndex in 0 until rows) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.width(headerWidth.value + 4.dp)
            )
            SubcomposeLayout { constraints ->
                val column = subcompose("overview_lesson$lessonIndex") {
                    Column(
                        modifier = Modifier
                            .height(rowHeights[lessonIndex] - 1.dp)
                            .width(IntrinsicSize.Max),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${lessonIndex + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        if (lessonIndex < lessons.size) {
                            Text(
                                text = "${lessons[lessonIndex].start}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = NotoSans,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = "${lessons[lessonIndex].end}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = NotoSans,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }[0].measure(constraints)
                val newWidth = column.width.toDp()
                if (headerWidth.value != newWidth) {
                    headerWidth.value = newWidth
                }
                layout(column.width, (rowHeights[lessonIndex] - 1.dp).roundToPx()) {
                    column.placeRelative(0, 0)
                }
            }
        }
    }
}
