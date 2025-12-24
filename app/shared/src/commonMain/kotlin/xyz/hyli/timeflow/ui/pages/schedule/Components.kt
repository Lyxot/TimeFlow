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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.harmonize
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import kotlinx.serialization.ExperimentalSerializationApi
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.data.Course
import xyz.hyli.timeflow.data.Lesson
import xyz.hyli.timeflow.data.Range
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.WeekDescriptionEnum
import xyz.hyli.timeflow.data.WeekList
import xyz.hyli.timeflow.shared.generated.resources.Res
import xyz.hyli.timeflow.shared.generated.resources.export
import xyz.hyli.timeflow.shared.generated.resources.friday
import xyz.hyli.timeflow.shared.generated.resources.import
import xyz.hyli.timeflow.shared.generated.resources.monday
import xyz.hyli.timeflow.shared.generated.resources.saturday
import xyz.hyli.timeflow.shared.generated.resources.schedule_warning_multiple_courses
import xyz.hyli.timeflow.shared.generated.resources.sunday
import xyz.hyli.timeflow.shared.generated.resources.thursday
import xyz.hyli.timeflow.shared.generated.resources.tuesday
import xyz.hyli.timeflow.shared.generated.resources.wednesday
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.theme.NotoSans
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
                x = layoutParams.headerWidth.value + 4.dp + layoutParams.cellWidth * dayIndex,
                y = 0.dp
            )
                .height(layoutParams.headerHeight.value + 64.dp * layoutParams.rows)
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        for (dayIndex in 0 until layoutParams.columns) {
            SubcomposeLayout(
                modifier = Modifier.padding(end = 1.dp)
            ) { constraints ->
                val column = subcompose("day$dayIndex") {
                    Column(
                        modifier = Modifier
                            .width(layoutParams.cellWidth - 1.dp)
                            .height(IntrinsicSize.Max)
                            .offset(
                                x = layoutParams.headerWidth.value + 5.dp + layoutParams.cellWidth * dayIndex,
                                y = 0.dp
                            ),
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
                    .width(layoutParams.headerWidth.value + 4.dp)
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
                    x = layoutParams.headerWidth.value + 4.dp + layoutParams.cellWidth * dayIndex,
                )
        ) {
            for (lessonIndex in 0 until layoutParams.rows) {
                if (lessonIndex + 1 in layoutParams.noGridCells.value[dayIndex]) continue
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
                            onCreateNewCourse(
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
                                isClicked = 1 // 点击中
                            )
                        }
                )
            }
        }
    }
}

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
        // 按照距离当前周的最小差值排序, 优先显示在当前周之后的课程
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
                    val indicatorSize = minOf(20.dp, width / 4)
                    AnimatedContent(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        targetState = width / 4 < 20.dp,
                    ) { state ->
                        if (state) {
                            // 显示小圆点
                            Box(
                                modifier = Modifier
                                    .padding(maxOf(minOf(width * 0.04f, height * 0.04f), 4.dp))
                                    .size(minOf(8.dp, width / 8))
                                    .clip(RoundedCornerShape(50))
                                    .background(contentColor)
                            )
                        } else {
                            // 显示三角形
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
                        .offset(
                            y = displayOffSet
                        )
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSerializationApi::class)
@Composable
fun ScheduleFAB(
    modifier: Modifier = Modifier,
    viewModel: TimeFlowViewModel,
    visible: Boolean,
    showMessage: (String) -> Unit,
) {
    val schedule by viewModel.selectedSchedule.collectAsState()
    var showContent by remember { mutableStateOf(false) }
    val saver = rememberFileSaverLauncher { file ->
        if (file != null) {
            viewModel.exportScheduleToFile(
                file = file,
                showMessage = showMessage
            )
        }
    }
    val reader = rememberFilePickerLauncher(
        mode = FileKitMode.Single
    ) { file ->
        if (file != null) {
            viewModel.importScheduleFromFile(
                file = file,
                showMessage = showMessage
            )
        }
    }
    FloatingActionButtonMenu(
        modifier = modifier,
        button = {
            ToggleFloatingActionButton(
                modifier = Modifier.animateFloatingActionButton(
                    visible = visible,
                    alignment = Alignment.BottomCenter
                ),
                checked = showContent,
                onCheckedChange = { showContent = !showContent },
            ) {
                val imageVector by remember(checkedProgress) {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) Icons.Default.Close else Icons.Default.Share
                    }
                }
                Icon(
                    modifier = Modifier.animateIcon({ checkedProgress }),
                    imageVector = imageVector,
                    contentDescription = null
                )
            }
        },
        expanded = showContent
    ) {
        if (schedule != null) {
            FloatingActionButtonMenuItem(
                onClick = {
                    showContent = false
                    // TODO: 分享课程表Dialog
                    saver.launch(schedule!!.name, "pb")
                },
                text = {
                    Text(stringResource(Res.string.export))
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.IosShare,
                        contentDescription = null
                    )
                },
            )
        }
        FloatingActionButtonMenuItem(
            onClick = {
                showContent = false
                // TODO: 导入课程表Dialog
                reader.launch()
            },
            text = {
                Text(stringResource(Res.string.import))
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null
                )
            }
        )
    }
}