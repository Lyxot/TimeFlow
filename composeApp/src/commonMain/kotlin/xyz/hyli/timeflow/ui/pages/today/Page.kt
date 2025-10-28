package xyz.hyli.timeflow.ui.pages.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.friday_long
import timeflow.composeapp.generated.resources.monday_long
import timeflow.composeapp.generated.resources.saturday_long
import timeflow.composeapp.generated.resources.schedule_value_course_time
import timeflow.composeapp.generated.resources.schedule_value_course_time_period
import timeflow.composeapp.generated.resources.settings_subtitle_schedule_empty
import timeflow.composeapp.generated.resources.settings_subtitle_schedule_not_selected
import timeflow.composeapp.generated.resources.settings_title_lessons_time_afternoon
import timeflow.composeapp.generated.resources.settings_title_lessons_time_evening
import timeflow.composeapp.generated.resources.settings_title_lessons_time_morning
import timeflow.composeapp.generated.resources.sunday_long
import timeflow.composeapp.generated.resources.thursday_long
import timeflow.composeapp.generated.resources.today_value_date
import timeflow.composeapp.generated.resources.today_value_idle
import timeflow.composeapp.generated.resources.tuesday_long
import timeflow.composeapp.generated.resources.wednesday_long
import xyz.hyli.timeflow.datastore.Course
import xyz.hyli.timeflow.datastore.LessonTimePeriodInfo
import xyz.hyli.timeflow.datastore.Time
import xyz.hyli.timeflow.ui.theme.NotoSans
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun TodayScreen(
    viewModel: TimeFlowViewModel
) {
    val settings by viewModel.settings.collectAsState()
    val schedule = settings.schedule[settings.selectedSchedule]

    if (schedule == null) {
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

    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val todayCourses = schedule.courses.filter {
        it.week.week.contains((schedule.termStartDate.weeksTill())) && it.weekday.ordinal == today.dayOfWeek.ordinal
    }
    val morningCourses =
        todayCourses.filter { it.time.start <= schedule.lessonTimePeriodInfo.morning.size }
            .sortedBy { it.time.start }
    val afternoonCourses = todayCourses.filter {
        it.time.end > schedule.lessonTimePeriodInfo.morning.size &&
                it.time.start <= schedule.lessonTimePeriodInfo.morning.size + schedule.lessonTimePeriodInfo.afternoon.size
    }.sortedBy { it.time.start }
    val eveningCourses =
        todayCourses.filter { it.time.end > schedule.lessonTimePeriodInfo.morning.size + schedule.lessonTimePeriodInfo.afternoon.size }
            .sortedBy { it.time.start }

    val weekdays = listOf(
        stringResource(Res.string.monday_long),
        stringResource(Res.string.tuesday_long),
        stringResource(Res.string.wednesday_long),
        stringResource(Res.string.thursday_long),
        stringResource(Res.string.friday_long),
        stringResource(Res.string.saturday_long),
        stringResource(Res.string.sunday_long)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp)
    ) {
        if (currentPlatform().isDesktop()) {
            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }

        Text(
            text = stringResource(
                Res.string.today_value_date,
                today.month.number,
                today.day,
                weekdays[today.dayOfWeek.ordinal]
            ),
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        schedule.lessonTimePeriodInfo.let {
            if (it.morning.isNotEmpty()) {
                TodayCourseSection(
                    title = stringResource(Res.string.settings_title_lessons_time_morning),
                    start = 1,
                    end = it.morning.size,
                    courses = morningCourses,
                    timePeriodInfo = it
                )
            }
            if (it.afternoon.isNotEmpty()) {
                TodayCourseSection(
                    title = stringResource(Res.string.settings_title_lessons_time_afternoon),
                    start = it.morning.size + 1,
                    end = it.morning.size + it.afternoon.size,
                    courses = afternoonCourses,
                    timePeriodInfo = it
                )
            }
            if (it.evening.isNotEmpty()) {
                TodayCourseSection(
                    title = stringResource(Res.string.settings_title_lessons_time_evening),
                    start = it.morning.size + it.afternoon.size + 1,
                    end = it.getTotalLessons(),
                    courses = eveningCourses,
                    timePeriodInfo = it
                )
            }
        }
    }
}

@Composable
fun TodayCourseSection(
    title: String,
    start: Int,
    end: Int,
    courses: List<Course>,
    timePeriodInfo: LessonTimePeriodInfo
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        // 生成空闲时段和课程
        var currentLesson = start
        for (course in courses) {
            // 在课程前添加空闲时段
            if (currentLesson < course.time.start) {
                FreePeriodItem(
                    freeStart = currentLesson,
                    freeEnd = course.time.start - 1,
                    timePeriodInfo = timePeriodInfo
                )
            }

            // 添加课程
            TodayCourseItem(
                title = course.name,
                subtitle = listOfNotNull(
                    if (course.time.start == course.time.end)
                        stringResource(Res.string.schedule_value_course_time, course.time.start)
                    else
                        stringResource(
                            Res.string.schedule_value_course_time_period,
                            course.time.start,
                            course.time.end
                        ),
                    course.teacher.takeIf { it.isNotEmpty() },
                    course.classroom.takeIf { it.isNotEmpty() }
                ).joinToString(" | "),
                textColor = MaterialTheme.colorScheme.onBackground,
                barColor = Color(course.color),
                startTime = timePeriodInfo.getLessonByIndex(course.time.start).start,
                endTime = timePeriodInfo.getLessonByIndex(course.time.end).end,
                state = {
                    // TODO: 课程状态，"已结束", "还有 x 分钟上课", "还有 x 分钟下课"
                    Text(
                        text = "即将开始",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )

            currentLesson = course.time.end + 1
        }

        // 在最后一节课后添加空闲时段（包括该时间段完全没有课程的情况）
        if (currentLesson <= end) {
            FreePeriodItem(
                freeStart = currentLesson,
                freeEnd = end,
                timePeriodInfo = timePeriodInfo
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )
    }
}

@Composable
fun FreePeriodItem(
    freeStart: Int,
    freeEnd: Int,
    timePeriodInfo: LessonTimePeriodInfo
) {
    TodayCourseItem(
        title = stringResource(Res.string.today_value_idle),
        subtitle = if (freeStart == freeEnd)
            stringResource(Res.string.schedule_value_course_time, freeStart)
        else
            stringResource(Res.string.schedule_value_course_time_period, freeStart, freeEnd),
        textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        barColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        startTime = timePeriodInfo.getLessonByIndex(freeStart).start,
        endTime = timePeriodInfo.getLessonByIndex(freeEnd).end,
    )
}

@Composable
fun TodayCourseItem(
    title: String,
    subtitle: String,
    textColor: Color,
    barColor: Color,
    startTime: Time,
    endTime: Time,
    state: @Composable () -> Unit = {
        Text("")
    }
) {
    SubcomposeLayout { constraints ->
        val paddingPx = 8.dp.roundToPx()

        // 测量时间列
        val timePlaceable = subcompose("time") {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = startTime.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = NotoSans,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = endTime.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = NotoSans,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }[0].measure(constraints)

        // 测量颜色条宽度
        val barWidth = 4.dp.roundToPx()

        // 测量内容列（需要减去左侧已占用的宽度）
        val contentMaxWidth = constraints.maxWidth - timePlaceable.width - barWidth - paddingPx * 3
        val contentPlaceable = subcompose("content") {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                )
            }
        }[0].measure(
            constraints.copy(maxWidth = contentMaxWidth)
        )

        // 测量状态列
        val statePlaceable = subcompose("state") {
            state()
        }[0].measure(constraints)

        // 计算最大高度（不包括 padding）
        val maxContentHeight = maxOf(
            timePlaceable.height,
            contentPlaceable.height,
            statePlaceable.height
        )

        val barHeightIncrease = 8.dp.roundToPx()
        val barHeight = maxContentHeight + barHeightIncrease
        val barPlaceable = subcompose("bar") {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight.toDp())
                    .background(barColor, shape = androidx.compose.foundation.shape.CircleShape)
            )
        }[0].measure(
            constraints.copy(
                minHeight = barHeight,
                maxHeight = barHeight
            )
        )

        // 总高度包括上下 padding
        val totalHeight = maxContentHeight + paddingPx * 2
        val totalWidth = constraints.maxWidth
        val leftPadding = 8.dp.roundToPx()

        layout(totalWidth, totalHeight) {
            var xOffset = leftPadding
            val centerY = paddingPx + maxContentHeight / 2

            // 放置时间列（垂直居中）
            timePlaceable.placeRelative(
                x = xOffset,
                y = centerY - timePlaceable.height / 2
            )
            xOffset += timePlaceable.width + paddingPx

            // 放置颜色条（垂直居中）
            barPlaceable.placeRelative(
                x = xOffset,
                y = centerY - barPlaceable.height / 2
            )
            xOffset += barWidth + paddingPx

            // 放置内容列（垂直居中）
            contentPlaceable.placeRelative(
                x = xOffset,
                y = centerY - contentPlaceable.height / 2
            )

            // 放置状态列（右对齐，垂直居中）
            statePlaceable.let {
                it.placeRelative(
                    x = totalWidth - it.width,
                    y = centerY - it.height / 2
                )
            }
        }
    }
}

