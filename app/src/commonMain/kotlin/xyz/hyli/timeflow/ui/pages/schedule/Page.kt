/*
 * Copyright (c) 2025 Lyxot and contributors.
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import timeflow.app.generated.resources.Res
import timeflow.app.generated.resources.save
import timeflow.app.generated.resources.schedule_title_week_vacation
import timeflow.app.generated.resources.schedule_title_week_x_part_1
import timeflow.app.generated.resources.schedule_title_week_x_part_2
import timeflow.app.generated.resources.schedule_title_week_x_part_3
import timeflow.app.generated.resources.settings_subtitle_schedule_empty
import timeflow.app.generated.resources.settings_subtitle_schedule_not_selected
import xyz.hyli.timeflow.LocalNavSuiteType
import xyz.hyli.timeflow.data.Course
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.Weekday
import xyz.hyli.timeflow.ui.components.CustomScaffold
import xyz.hyli.timeflow.ui.components.TopAppBarType
import xyz.hyli.timeflow.ui.components.bottomPadding
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.navigation.Destination
import xyz.hyli.timeflow.ui.navigation.NavigationBarType
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

data class ScheduleLayoutParams(
    val headerWidth: MutableState<Dp>,
    val headerHeight: MutableState<Dp>,
    val cellWidth: Dp,
    val rows: Int,
    val columns: Int,
    val noGridCells: MutableState<List<Set<Int>>>
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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .bottomPadding()
            ) { page ->
                // 课程表表格
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
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight.value + 64.dp * rows)
    ) {
        val state = remember { mutableStateOf(TableState()) }
        val cellWidth = (maxWidth - headerWidth.value - 5.dp) / columns
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
            noGridCells = noGridCells
        )

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
            AnimatedContent(
                targetState = courses.isNotEmpty(),
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(300)
                    ) togetherWith fadeOut(animationSpec = tween(300))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp * (time.end - time.start + 1))
                    .padding(bottom = 1.dp)
                    .offset(
                        y = (time.start - 1) * 64.dp - 1.dp
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
            AnimatedContent(
                targetState = courses.isNotEmpty(),
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(300)
                    ) togetherWith fadeOut(animationSpec = tween(300))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp * (time.end - time.start + 1))
                    .padding(bottom = 1.dp)
                    .offset(
                        y = (time.start - 1) * 64.dp - 1.dp
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
                        displayOffSet = 64.dp * (firstSpaceToDisplay - time.start),
                        displayHeight = ((lastSpaceToDisplay - firstSpaceToDisplay + 1) * 64).dp,
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
                    .height(64.dp)
                    .padding(bottom = 1.dp)
                    .offset(
                        y = (index - 1) * 64.dp - 1.dp
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
