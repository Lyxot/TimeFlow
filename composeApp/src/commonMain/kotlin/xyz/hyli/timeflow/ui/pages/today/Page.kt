/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.today

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.harmonize
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.friday_long
import timeflow.composeapp.generated.resources.monday_long
import timeflow.composeapp.generated.resources.saturday_long
import timeflow.composeapp.generated.resources.schedule_value_course_time
import timeflow.composeapp.generated.resources.schedule_value_course_time_period
import timeflow.composeapp.generated.resources.schedule_value_course_week
import timeflow.composeapp.generated.resources.settings_subtitle_schedule_empty
import timeflow.composeapp.generated.resources.settings_subtitle_schedule_not_selected
import timeflow.composeapp.generated.resources.sunday_long
import timeflow.composeapp.generated.resources.thursday_long
import timeflow.composeapp.generated.resources.today_title_no_courses
import timeflow.composeapp.generated.resources.today_value_date
import timeflow.composeapp.generated.resources.tuesday_long
import timeflow.composeapp.generated.resources.wednesday_long
import xyz.hyli.timeflow.data.Course
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.Time
import xyz.hyli.timeflow.ui.components.CustomScaffold
import xyz.hyli.timeflow.ui.components.bottomPadding
import xyz.hyli.timeflow.ui.theme.NotoSans
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
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
    val currentWeek = remember { schedule.termStartDate.weeksTill(today) }
    val todayCourses = schedule.courses.filter {
        it.week.week.contains(currentWeek) && it.weekday.ordinal == today.dayOfWeek.ordinal
    }.sortedBy { it.time.start }

    val weekdays = listOf(
        Res.string.monday_long,
        Res.string.tuesday_long,
        Res.string.wednesday_long,
        Res.string.thursday_long,
        Res.string.friday_long,
        Res.string.saturday_long,
        Res.string.sunday_long
    )

    CustomScaffold(
        modifier = Modifier.fillMaxSize(),
        title = {
            Text(
                text = stringResource(
                    Res.string.today_value_date,
                    today.month.number,
                    today.day,
                    stringResource(weekdays[today.dayOfWeek.ordinal])
                ) + " " + stringResource(Res.string.schedule_value_course_week, currentWeek),
            )
        }
    ) {
        if (todayCourses.isEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(Res.string.today_title_no_courses),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        } else {
            TimelineCourseList(
                schedule = schedule,
                courses = todayCourses
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun TimelineCourseList(
    schedule: Schedule,
    courses: List<Course>,
) {
    val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
        Time(it.hour, it.minute)
    }
    var currentLessonFlag by remember { mutableStateOf(false) }
    val lessonTimePeriodInfo = schedule.lessonTimePeriodInfo
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .bottomPadding()
    ) {
        itemsIndexed(courses) { index, course ->
            val startTime = lessonTimePeriodInfo.getLessonByIndex(course.time.start).start
            val endTime = lessonTimePeriodInfo.getLessonByIndex(course.time.end).end
            val isPast = currentTime >= endTime
            val isCurrent = if (!currentLessonFlag && !isPast && currentTime <= endTime) {
                currentLessonFlag = true
                true
            } else false
            val previousColor = if (index > 0) {
                if (isCurrent || isPast) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                else Color(courses[index - 1].color).harmonize(
                    MaterialTheme.colorScheme.primary,
                    true
                )
            } else null
            TimelineCourseItem(
                course = course,
                startTime = startTime,
                endTime = endTime,
                isCurrent = isCurrent,
                isPast = isPast,
                isFirst = index == 0,
                isLast = index == courses.lastIndex,
                previousColor = previousColor,
            )
        }
    }
}


@Composable
fun TimelineCourseItem(
    course: Course,
    startTime: Time,
    endTime: Time,
    isCurrent: Boolean,
    isPast: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    previousColor: Color? = null
) {
    val containerColor = if (isPast) MaterialTheme.colorScheme.surface.copy(alpha = 0.38f)
    else if (isCurrent) MaterialTheme.colorScheme.primaryContainer
    else Color.Unspecified
    val contentColor = if (isPast) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    else if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer
    else Color.Unspecified
    val timelineColor = if (isPast) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    else Color(course.color).harmonize(MaterialTheme.colorScheme.primary, true)
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { 2.dp.toPx() }
    val outerRadiusPx = with(density) { 6.dp.toPx() }
    val innerRadiusPx = with(density) { 3.dp.toPx() }
    val cardPaddingPx = with(density) { 16.dp.toPx() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .width(48.dp)
                .padding(vertical = 16.dp)
        ) {
            val startColor: Color
            val startStyle: TextStyle
            val endColor: Color
            val endStyle: TextStyle

            when {
                isPast -> {
                    startColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    startStyle = MaterialTheme.typography.labelMedium
                    endColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    endStyle = MaterialTheme.typography.labelMedium
                }

                isCurrent -> {
                    startColor = MaterialTheme.colorScheme.onBackground
                    startStyle = MaterialTheme.typography.labelLarge
                    endColor = MaterialTheme.colorScheme.onBackground
                    endStyle = MaterialTheme.typography.labelLarge
                }

                else -> {
                    startColor = MaterialTheme.colorScheme.onBackground
                    startStyle = MaterialTheme.typography.labelLarge
                    endColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    endStyle = MaterialTheme.typography.labelMedium
                }
            }

            Text(
                text = "$startTime",
                fontFamily = NotoSans,
                style = startStyle,
                color = startColor
            )
            Text(
                text = "$endTime",
                fontFamily = NotoSans,
                style = endStyle,
                color = endColor
            )
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(16.dp)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val nodeX = size.width * 0.6f
                val nodeCenterY = cardPaddingPx + (size.height - cardPaddingPx * 2) / 5f
                val nodeTop = nodeCenterY - outerRadiusPx
                val nodeBottom = nodeCenterY + outerRadiusPx

                if (!isFirst && previousColor != null) {
                    drawLine(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(previousColor, timelineColor),
                            startY = 0f,
                            endY = nodeTop
                        ),
                        start = Offset(nodeX, 0f),
                        end = Offset(nodeX, nodeTop),
                        strokeWidth = strokeWidthPx
                    )
                } else if (!isFirst) {
                    drawLine(
                        color = timelineColor,
                        start = Offset(nodeX, 0f),
                        end = Offset(nodeX, nodeTop),
                        strokeWidth = strokeWidthPx
                    )
                }

                if (!isLast) {
                    drawLine(
                        color = timelineColor,
                        start = Offset(nodeX, nodeBottom),
                        end = Offset(nodeX, size.height),
                        strokeWidth = strokeWidthPx
                    )
                }

                drawCircle(
                    color = timelineColor,
                    radius = outerRadiusPx,
                    center = Offset(nodeX, nodeCenterY),
                    style = Stroke(width = strokeWidthPx)
                )
                if (isCurrent) {
                    drawCircle(
                        color = timelineColor,
                        radius = innerRadiusPx,
                        center = Offset(nodeX, nodeCenterY)
                    )
                }
            }
        }


        Card(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .requiredHeightIn(min = 100.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(containerColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    CourseCardContentRow(
                        icon = Icons.Rounded.Schedule,
                        content = if (course.time.start == course.time.end) stringResource(
                            Res.string.schedule_value_course_time,
                            course.time.start
                        )
                        else stringResource(
                            Res.string.schedule_value_course_time_period,
                            course.time.start,
                            course.time.end
                        ),
                        color = contentColor
                    )
                    if (course.classroom.isNotBlank()) {
                        CourseCardContentRow(
                            icon = Icons.Default.LocationOn,
                            content = course.classroom,
                            color = contentColor
                        )
                    }
                    if (course.teacher.isNotBlank()) {
                        CourseCardContentRow(
                            icon = Icons.Rounded.School,
                            content = course.teacher,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCardContentRow(
    icon: ImageVector,
    content: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(end = 4.dp)
                .size(14.dp),
            tint = color
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}