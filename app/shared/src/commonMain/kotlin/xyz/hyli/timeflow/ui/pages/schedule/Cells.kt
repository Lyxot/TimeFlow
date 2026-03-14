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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.materialkolor.ktx.harmonize
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.data.*
import xyz.hyli.timeflow.shared.generated.resources.Res
import xyz.hyli.timeflow.shared.generated.resources.schedule_value_course_time_period
import xyz.hyli.timeflow.shared.generated.resources.schedule_value_course_week
import xyz.hyli.timeflow.shared.generated.resources.schedule_warning_multiple_courses
import xyz.hyli.timeflow.ui.components.rememberDialogState

@Composable
fun CourseCell(
    schedule: Schedule,
    courses: Map<Short, Course>,
    coursesForThisTime: Map<Short, Course>,
    currentWeek: Int,
    totalWeeks: Int,
    displayOffSet: Dp = 0.dp,
    displayHeight: Dp = courses.values.first().time.let { (it.end - it.start + 1) * 64 }.dp,
    onEditCourse: (Short, Course) -> Unit,
    onCreateNewCourse: (Course) -> Unit
) {
    val showCourseListDialog = rememberDialogState()
    val course = courses.values.minBy { course ->
        course.week.weeks.minOfOrNull { week ->
            (week - currentWeek).let {
                if (it < 0) it + totalWeeks else it
            }
        } ?: Int.MAX_VALUE
    }
    var containerColor by remember { mutableStateOf(Color.Unspecified) }
    var contentColor by remember { mutableStateOf(Color.Unspecified) }
    if (course.isInWeek(currentWeek) && courses.size > 1) {
        containerColor = MaterialTheme.colorScheme.error
        contentColor = MaterialTheme.colorScheme.onError
    } else if (course.isInWeek(currentWeek)) {
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
            schedule = schedule,
            courses = coursesForThisTime,
            currentWeek = currentWeek,
            totalWeeks = totalWeeks,
            time = course.time,
            showCourseListDialog = showCourseListDialog,
            onEditCourse = onEditCourse,
            onCreateNewCourse = onCreateNewCourse
        )
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
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
                    val indicatorSize = minOf(20.dp, width / 4)
                    AnimatedContent(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        targetState = width / 4 < 20.dp,
                    ) { state ->
                        if (state) {
                            Box(
                                modifier = Modifier
                                    .padding(maxOf(minOf(width * 0.04f, height * 0.04f), 4.dp))
                                    .size(minOf(8.dp, width / 8))
                                    .clip(RoundedCornerShape(50))
                                    .background(contentColor)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .padding(maxOf(minOf(width * 0.02f, height * 0.02f), 2.dp))
                                    .size(indicatorSize)
                                    .clip(RightBottomTriangleShape)
                                    .clip(RoundedCornerShape(0.dp, 0.dp, 12.dp, 0.dp))
                                    .background(contentColor)
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(displayHeight)
                        .padding(
                            minOf(width * 0.06f, height * 0.06f, 4.dp)
                        )
                        .offset(y = displayOffSet)
                ) {
                    if (course.isInWeek(currentWeek) && courses.size > 1) {
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
                            modifier = Modifier.weight(1f, fill = false),
                            style = MaterialTheme.typography.labelMedium,
                            overflow = TextOverflow.Ellipsis,
                            color = contentColor
                        )
                        if (course.classroom.isNotBlank()) {
                            Text(
                                text = "@${course.classroom}",
                                style = MaterialTheme.typography.labelSmall,
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

@Composable
fun OverviewCourseCell(
    courses: Map<Short, Course>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val sortedCourses = remember(courses) {
        courses.entries.sortedBy { it.value.week.weeks.minOrNull() ?: 0 }
    }
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val nameStyle = MaterialTheme.typography.labelMedium
    val infoStyle = MaterialTheme.typography.labelSmall

    // Pre-resolve formatted strings at composable level
    val weekTexts = sortedCourses.map { (_, course) ->
        stringResource(Res.string.schedule_value_course_week, course.week.toString())
    }
    val periodTexts = sortedCourses.map { (_, course) ->
        stringResource(Res.string.schedule_value_course_time_period, course.time.start, course.time.end)
    }

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(2.dp),
        onClick = onClick ?: {}
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val contentWidthPx = with(density) { (maxWidth - 8.dp).toPx() }
                .toInt().coerceAtLeast(1)
            val textConstraints = Constraints(maxWidth = contentWidthPx)

            // Pre-compute per-course entry heights
            val courseHeights = remember(courses, contentWidthPx, nameStyle, infoStyle) {
                sortedCourses.mapIndexed { index, (_, course) ->
                    var heightPx = 0
                    heightPx += textMeasurer.measure(
                        course.name, nameStyle, maxLines = 3, constraints = textConstraints
                    ).size.height
                    heightPx += textMeasurer.measure(
                        weekTexts[index], infoStyle, constraints = textConstraints
                    ).size.height
                    heightPx += textMeasurer.measure(
                        periodTexts[index], infoStyle, constraints = textConstraints
                    ).size.height
                    if (course.classroom.isNotBlank()) {
                        heightPx += textMeasurer.measure(
                            "@${course.classroom}", infoStyle, constraints = textConstraints
                        ).size.height
                    }
                    if (course.teacher.isNotBlank()) {
                        heightPx += textMeasurer.measure(
                            course.teacher, infoStyle, maxLines = 1, constraints = textConstraints
                        ).size.height
                    }
                    with(density) { heightPx.toDp() } + 8.dp // 4dp padding top + bottom
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                sortedCourses.forEachIndexed { index, (_, course) ->
                    val containerColor = Color(course.color)
                        .harmonize(MaterialTheme.colorScheme.secondaryContainer, true)
                    val contentColor = Color(course.color)
                        .harmonize(MaterialTheme.colorScheme.onSecondaryContainer, true)

                    if (index > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Column(
                        modifier = (if (index == sortedCourses.lastIndex) {
                            Modifier.weight(1f)
                        } else {
                            Modifier.height(courseHeights[index])
                        })
                            .fillMaxWidth()
                            .background(containerColor)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = course.name,
                            modifier = Modifier.weight(1f, fill = false),
                            style = nameStyle,
                            overflow = TextOverflow.Ellipsis,
                            color = contentColor
                        )
                        Text(
                            text = weekTexts[index],
                            style = infoStyle,
                            color = contentColor
                        )
                        Text(
                            text = periodTexts[index],
                            style = infoStyle,
                            color = contentColor
                        )
                        if (course.classroom.isNotBlank()) {
                            Text(
                                text = "@${course.classroom}",
                                style = infoStyle,
                                overflow = TextOverflow.Ellipsis,
                                color = contentColor
                            )
                        }
                        if (course.teacher.isNotBlank()) {
                            Text(
                                text = course.teacher,
                                style = infoStyle,
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

@Composable
fun EmptyTableCell(
    state: MutableState<TableState>,
    index: Int,
    dayIndex: Int,
    totalWeeks: Int,
    onCreateNewCourse: (Course) -> Unit
) {
    val isActive =
        state.value.row == index && state.value.column == dayIndex + 1 && state.value.isClicked != 0

    AnimatedContent(
        targetState = isActive,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
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
                            onCreateNewCourse(
                                Course(
                                    name = "",
                                    time = Range(index, index),
                                    weekday = weekdays[dayIndex],
                                    week = WeekList(
                                        weekDescription = WeekDescriptionEnum.ALL,
                                        totalWeeks = totalWeeks
                                    ),
                                    color = -1
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
                                isClicked = 1
                            )
                        }
                )
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
