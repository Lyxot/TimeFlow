package xyz.hyli.timeflow.ui.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.materialkolor.ktx.harmonize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.confirm
import timeflow.composeapp.generated.resources.friday
import timeflow.composeapp.generated.resources.monday
import timeflow.composeapp.generated.resources.saturday
import timeflow.composeapp.generated.resources.save
import timeflow.composeapp.generated.resources.schedule_button_edit
import timeflow.composeapp.generated.resources.schedule_course_not_this_week
import timeflow.composeapp.generated.resources.schedule_title_course_detail
import timeflow.composeapp.generated.resources.schedule_title_week_vacation
import timeflow.composeapp.generated.resources.schedule_title_week_x_part_1
import timeflow.composeapp.generated.resources.schedule_title_week_x_part_2
import timeflow.composeapp.generated.resources.schedule_title_week_x_part_3
import timeflow.composeapp.generated.resources.schedule_value_course_time
import timeflow.composeapp.generated.resources.schedule_value_course_week
import timeflow.composeapp.generated.resources.schedule_warning_multiple_courses
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
import xyz.hyli.timeflow.ui.components.ColorDefinitions.COLORS
import xyz.hyli.timeflow.ui.components.DialogButton
import xyz.hyli.timeflow.ui.components.DialogDefaults
import xyz.hyli.timeflow.ui.components.DialogState
import xyz.hyli.timeflow.ui.components.MyDialog
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.navigation.EditCourseDestination
import xyz.hyli.timeflow.ui.navigation.NavigationBarType
import xyz.hyli.timeflow.ui.theme.NotoSans
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class ScheduleLayoutParams(
    val headerWidth: MutableState<Dp>,
    val headerHeight: MutableState<Dp>,
    val cellWidth: Dp,
    val rows: Int,
    val columns: Int,
    val noGridCells: MutableState<Set<Pair<Int, Int>>>
)

data class ScheduleParams(
    val viewModel: TimeFlowViewModel,
    val navHostController: NavHostController,
    val navSuiteType: NavigationSuiteType,
    val schedule: Schedule,
    val currentWeek: Int,
    val totalWeeks: Int = schedule.totalWeeks()
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
                IconButton(
                    onClick = {
                        // TODO: Add new schedule dialog
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.save)
                    )
                }
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
                                    else stringResource(it.second)
                            )
                        }[0].measure(constraints)
                    }
                    val part2Max = subcompose("part2max") {
                        Text(
                            text = stringResource(
                                Res.string.schedule_title_week_x_part_2,
                                pagerState.pageCount + 1
                            )
                        )
                    }[0].measure(constraints)
                    val width = maxOf(list[0].width + part2Max.width + list[2].width, list[3].width)
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
                pageSpacing = 12.dp
            ) { page ->
                // 课程表表格
                ScheduleTable(
                    scheduleParams = ScheduleParams(
                        viewModel = viewModel,
                        navHostController = navHostController,
                        navSuiteType = navSuiteType,
                        schedule = schedule,
                        currentWeek = page + 1
                    ),
                    rows = rows,
                    columns = columns,
                    modifier = Modifier.fillMaxWidth()
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
    scheduleParams: ScheduleParams,
    rows: Int,
    columns: Int,
    modifier: Modifier = Modifier
) {
    val lessonTimePeriodInfo = scheduleParams.schedule.lessonTimePeriodInfo.morning +
            scheduleParams.schedule.lessonTimePeriodInfo.afternoon +
            scheduleParams.schedule.lessonTimePeriodInfo.evening
    val headerWidth = remember { mutableStateOf(48.dp) }
    val headerHeight = remember { mutableStateOf(40.dp) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(
                headerHeight.value + 64.dp * rows + maxOf(
                    WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding(),
                    24.dp
                )
            )
    ) {
        val state = remember { mutableStateOf(TableState()) }
        val cellWidth = (maxWidth - headerWidth.value - 6.dp) / columns
        val noGridCells = remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
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
            lessonTimePeriodInfo = lessonTimePeriodInfo
        )

        // 覆盖层：课程内容
        CourseOverlay(
            layoutParams = layoutParams,
            scheduleParams = scheduleParams,
            state = state
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun TableGrid(
    layoutParams: ScheduleLayoutParams,
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
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    for (dayIndex in 0 until layoutParams.columns) {
        VerticalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.offset(
                x = layoutParams.headerWidth.value + 5.dp + layoutParams.cellWidth * dayIndex,
                y = 0.dp
            )
                .height(layoutParams.headerHeight.value + 64.dp * layoutParams.rows)
        )
    }

    Row(
        modifier = Modifier
            .offset(
                x = layoutParams.headerWidth.value + 6.dp,
                y = 0.dp
            )
    ) {
        for (dayIndex in 0 until layoutParams.columns) {
            SubcomposeLayout(
                modifier = Modifier.padding(end = 1.dp)
            ) { constraints ->
                val column = subcompose("day$dayIndex") {
                    Column(
                        modifier = Modifier
                            .width(layoutParams.cellWidth - 1.dp)
                            .height(IntrinsicSize.Max),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val color =
                            if (dateList[dayIndex] == today) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onBackground
                            }
                        Text(
                            text = weekdays[dayIndex],
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = color
                        )
                        Text(
                            text = dateList[dayIndex].let {
                                it.month.number.toString() + "/" + it.day.toString()
                                    .padStart(2, '0')
                            },
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            color = color.copy(alpha = 0.6f)
                        )
                    }
                }[0].measure(constraints)
                val newHeight = column.height.toDp()
                if (layoutParams.headerHeight.value != newHeight) {
                    layoutParams.headerHeight.value = newHeight
                }
                layout(layoutParams.cellWidth.roundToPx(), column.height) {
                    column.placeRelative(
                        x = 0,
                        y = 0
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .offset(
                x = 1.dp,
                y = layoutParams.headerHeight.value - 1.dp
            )
    ) {
        for (lessonIndex in 0 until layoutParams.rows) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier
                    .width(layoutParams.headerWidth.value + 5.dp)
            )
            SubcomposeLayout { constraints ->
                val column = subcompose("lesson$lessonIndex") {
                    Column(
                        modifier = Modifier
                            .height(63.dp)
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
                        if (lessonIndex < lessonTimePeriodInfo.size) {
                            Text(
                                text = "${lessonTimePeriodInfo[lessonIndex].start}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = NotoSans,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = "${lessonTimePeriodInfo[lessonIndex].end}",
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
                if (layoutParams.headerWidth.value != newWidth) {
                    layoutParams.headerWidth.value = newWidth
                }
                layout(column.width, 63.dp.roundToPx()) {
                    column.placeRelative(
                        x = 0,
                        y = 0
                    )
                }
            }
        }
    }

    for (dayIndex in 0 until layoutParams.columns) {
        Box(
            modifier = Modifier.width(layoutParams.cellWidth)
                .offset(
                    x = layoutParams.headerWidth.value + 5.dp + layoutParams.cellWidth * dayIndex,
                )
        ) {
            for (lessonIndex in 0 until layoutParams.rows) {
                if (Pair(lessonIndex + 1, dayIndex + 1) in layoutParams.noGridCells.value) continue
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier
                        .offset(
                            y = layoutParams.headerHeight.value + 64.dp * lessonIndex - 1.dp
                        )
                )
            }
        }
    }
}

@Composable
fun CourseOverlay(
    layoutParams: ScheduleLayoutParams,
    scheduleParams: ScheduleParams,
    state: MutableState<TableState>
) {
    Row(
        modifier = Modifier
            .offset(
                x = layoutParams.headerWidth.value + 6.dp,
                y = layoutParams.headerHeight.value + 1.dp
            )
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
}

@Composable
fun EmptyTableCell(
    state: MutableState<TableState>,
    index: Int,
    dayIndex: Int,
    totalWeeks: Int,
    onClick: (Course) -> Unit
) {
    val isActive =
        state.value.row == index && state.value.column == dayIndex + 1 && state.value.isClicked != 0

    AnimatedContent(
        targetState = isActive,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            fadeIn(
                animationSpec = tween(300)
            ) togetherWith fadeOut(animationSpec = tween(300))
        }
    ) { active ->
        if (active) {
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
                                    ),
                                    color = COLORS.random().toArgb()
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
                                color = COLORS.random().toArgb()
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
        Column {
            courses.forEachIndexed { index, course ->
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
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
                                        Res.string.schedule_value_course_time,
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
                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            onClick(course)
                            showCourseListDialog.dismiss()
                        }
                    ) {
                        Text(
                            stringResource(Res.string.schedule_button_edit)
                        )
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
fun CourseColumn(
    layoutParams: ScheduleLayoutParams,
    scheduleParams: ScheduleParams,
    state: MutableState<TableState>,
    dayIndex: Int
) {
    val showEditCourseDialog = rememberDialogState()
    val renderedTimeSlots = mutableSetOf<Int>()
    var noGridCells by layoutParams.noGridCells
    var daySchedule by remember { mutableStateOf<List<Course>?>(null) }
    var dayScheduleTimeForCurrentWeek by remember { mutableStateOf<Set<Range>?>(null) }
    var dayScheduleTimeForOtherWeek by remember { mutableStateOf<Set<Range>?>(null) }
    var selectCourse by remember { mutableStateOf<Course?>(null) }

    LaunchedEffect(
        scheduleParams.schedule.courses,
        dayIndex,
        scheduleParams.currentWeek,
        layoutParams.rows
    ) {
        val filteredSchedule = scheduleParams.schedule.courses.filter {
            it.weekday == weekdays[dayIndex]
        }
        val currentWeekTimes = mutableSetOf<Range>()
        val otherWeekTimes = mutableSetOf<Range>()

        filteredSchedule.forEach { course ->
            if (course.week.week.contains(scheduleParams.currentWeek) &&
                scheduleParams.currentWeek in 1..scheduleParams.totalWeeks
            ) {
                currentWeekTimes.add(course.time)
            } else {
                otherWeekTimes.add(course.time)
            }
        }

        daySchedule = filteredSchedule
        dayScheduleTimeForCurrentWeek = currentWeekTimes
        dayScheduleTimeForOtherWeek = otherWeekTimes.sortedBy { it.end - it.start }.toSet()
    }
    val emptySlots = remember(layoutParams.rows, renderedTimeSlots) {
        (1..layoutParams.rows).filterNot { it in renderedTimeSlots }
    }

    fun editCourse(course: Course) {
        if (scheduleParams.navSuiteType !in NavigationBarType) {
            selectCourse = course
            showEditCourseDialog.show()
        } else {
            scheduleParams.navHostController.navigate(
                EditCourseDestination(
                    course
                )
            )
        }
    }

    if (showEditCourseDialog.visible) {
        EditCourseDialog(
            state = state,
            scheduleParams = scheduleParams,
            initValue = selectCourse!!,
            showEditCourseDialog = showEditCourseDialog
        )
    }

    Box(
        modifier = Modifier
            .padding(end = 1.dp)
            .width(layoutParams.cellWidth - 1.dp)
    ) {
        if (daySchedule != null && dayScheduleTimeForCurrentWeek != null && dayScheduleTimeForOtherWeek != null) {
            // 当前周的课程
            dayScheduleTimeForCurrentWeek!!.forEach { time ->
                renderedTimeSlots.addAll(time.start..time.end)
                dayScheduleTimeForOtherWeek!! - time
                if (time.end - time.start > 0) {
                    noGridCells = noGridCells.toMutableSet().let {
                        it + (time.start until time.end).map { Pair(it + 1, dayIndex + 1) }
                    }
                }
                var courses by remember { mutableStateOf<List<Course>?>(null) }
                var coursesForThisTime by remember { mutableStateOf<List<Course>?>(null) }
                LaunchedEffect(daySchedule, time, scheduleParams.currentWeek) {
                    courses = daySchedule!!.filter { course ->
                        course.time == time && course.week.week.contains(scheduleParams.currentWeek)
                    }
                    coursesForThisTime = daySchedule!!.filter { course ->
                        course.time.end >= time.start && course.time.start <= time.end
                    }
                }
                AnimatedContent(
                    targetState = courses != null || coursesForThisTime != null,
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
                            courses = courses!!,
                            coursesForThisTime = coursesForThisTime!!,
                            currentWeek = scheduleParams.currentWeek,
                            totalWeeks = scheduleParams.totalWeeks,
                            onClick = { course ->
                                editCourse(course)
                            }
                        )
                    }
                }
            }
            // 非当前周的课程
            dayScheduleTimeForOtherWeek!!.forEachIndexed { _, time ->
                if ((time.start..time.end).all { it in renderedTimeSlots }) {
                    return@forEachIndexed // 已经渲染过的时间段跳过
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
                if (time.end - time.start > 0) {
                    noGridCells = noGridCells.toMutableSet().let {
                        it + (time.start until time.end).map { Pair(it + 1, dayIndex + 1) }
                    }
                }
                var courses by remember { mutableStateOf<List<Course>?>(null) }
                var coursesForThisTime by remember { mutableStateOf<List<Course>?>(null) }
                LaunchedEffect(daySchedule, time, scheduleParams.currentWeek) {
                    courses = daySchedule!!
                        .filter { course ->
                            course.time == time &&
                                    (!course.week.week.contains(scheduleParams.currentWeek) || scheduleParams.currentWeek !in 1..scheduleParams.totalWeeks)
                        }
                        .sortedBy {
                            // 按照距离当前周的最小差值排序, 优先显示在当前周之后的课程
                            it.week.week.minOfOrNull { week ->
                                (week - scheduleParams.currentWeek).let {
                                    if (it < 0) it + scheduleParams.totalWeeks else it
                                }
                            } ?: Int.MAX_VALUE
                        }
                    coursesForThisTime = daySchedule!!.filter { course ->
                        course.time.end >= time.start && course.time.start <= time.end
                    }
                }
                AnimatedContent(
                    targetState = courses != null || coursesForThisTime != null,
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
                            courses = courses!!,
                            coursesForThisTime = coursesForThisTime!!,
                            currentWeek = scheduleParams.currentWeek,
                            totalWeeks = scheduleParams.totalWeeks,
                            displayOffSet = 64.dp * (firstSpaceToDisplay - time.start),
                            displayHeight = ((lastSpaceToDisplay - firstSpaceToDisplay + 1) * 64).dp,
                            onClick = { course ->
                                editCourse(course)
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
                        totalWeeks = scheduleParams.totalWeeks,
                        onClick = { course ->
                            state.value = state.value.copy(
                                isClicked = 2, // 点击后等待重置
                            )
                            editCourse(course)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CourseCell(
    courses: List<Course>,
    coursesForThisTime: List<Course>,
    currentWeek: Int,
    totalWeeks: Int,
    displayOffSet: Dp = 0.dp,
    displayHeight: Dp = courses.first().time.let { (it.end - it.start + 1) * 64 }.dp,
    onClick: (Course) -> Unit
) {
    val showCourseListDialog = rememberDialogState()
    val course = courses.first()
    var containerColor by remember { mutableStateOf(Color.Unspecified) }
    var contentColor by remember { mutableStateOf(Color.Unspecified) }
    if (course.week.week.contains(currentWeek) && courses.size > 1) {
        containerColor = MaterialTheme.colorScheme.error
        contentColor = MaterialTheme.colorScheme.onError
    } else if (course.week.week.contains(currentWeek)) {
        containerColor =
            Color(course.color).harmonize(MaterialTheme.colorScheme.secondaryContainer, true)
        contentColor =
            Color(course.color).harmonize(MaterialTheme.colorScheme.onSecondaryContainer, true)
    } else {
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f)
        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    if (showCourseListDialog.visible) {
        CourseListDialog(
            courses = coursesForThisTime,
            currentWeek = currentWeek,
            totalWeeks = totalWeeks,
            time = course.time,
            showCourseListDialog = showCourseListDialog,
            onClick = onClick
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val width = maxWidth
        val height = maxHeight
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            onClick = {
                showCourseListDialog.show()
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(containerColor)
            ) {
                if (coursesForThisTime.size > 1) {
                    Box(
                        modifier = Modifier
                            .padding(maxOf(minOf(width * 0.02f, height * 0.02f), 2.dp))
                            .size(minOf(20.dp, width / 4))
                            .clip(RightBottomTriangleShape)
                            .clip(RoundedCornerShape(0.dp, 0.dp, 8.dp, 0.dp))
                            .background(contentColor)
                            .align(Alignment.BottomEnd)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(displayHeight)
                        .padding(
                            minOf(width * 0.06f, height * 0.06f, 6.dp)
                        )
                        .offset(
                            y = displayOffSet
                        )
                ) {
                    if (course.week.week.contains(currentWeek) && courses.size > 1) {
                        Text(
                            text = stringResource(
                                Res.string.schedule_warning_multiple_courses,
                                courses.size
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = contentColor
                        )
                    } else {
                        Text(
                            text = course.name,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            color = contentColor
                        )
                        if (course.classroom.isNotBlank()) {
                            Text(
                                text = "@${course.classroom}",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = contentColor
                            )
                        }
                        if (course.teacher.isNotBlank()) {
                            Text(
                                text = course.teacher,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private object RightBottomTriangleShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width, size.height)
            arcTo(
                rect = Rect(
                    left = size.width,
                    top = size.height,
                    right = size.width,
                    bottom = size.height
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(size.width, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}
