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
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import xyz.hyli.timeflow.LocalNavSuiteType
import xyz.hyli.timeflow.data.Course
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.navigation.Destination
import xyz.hyli.timeflow.ui.navigation.NavigationBarType

@Composable
fun ScheduleTable(
    scheduleParams: ScheduleParams,
    rows: Int,
    columns: Int,
    modifier: Modifier = Modifier
) {
    val schedule = scheduleParams.schedule
    val config = ScheduleDisplayConfig(
        rows = rows,
        columns = columns,
        showDates = true,
        dateList = schedule.dateList(scheduleParams.currentWeek),
        lessons = schedule.lessonTimePeriodInfo.lessons
    )
    val state = remember { mutableStateOf(TableState()) }

    // Auto-reset click state after timeout
    LaunchedEffect(state.value) {
        if (state.value.isClicked == 1) {
            val current = state.value
            delay(10000)
            if (state.value == current) {
                state.value = TableState()
            }
        }
    }

    ScheduleTableLayout(
        config = config,
        tableDataFactory = { cellWidth ->
            rememberWeeklyTableData(schedule, config, cellWidth)
        },
        modifier = modifier
    ) { dayIndex, tableData ->
        WeeklyColumnContent(
            tableData = tableData,
            scheduleParams = scheduleParams,
            state = state,
            dayIndex = dayIndex
        )
    }
}

@Composable
private fun WeeklyColumnContent(
    tableData: ScheduleTableData,
    scheduleParams: ScheduleParams,
    state: MutableState<TableState>,
    dayIndex: Int
) {
    val navSuiteType by LocalNavSuiteType.current
    val showEditCourseDialog = rememberDialogState()
    val renderedTimeSlots = mutableSetOf<Int>()
    var selectCourse by remember { mutableStateOf<Course?>(null) }
    var selectCourseID by remember { mutableStateOf<Short?>(null) }

    val (timeSlotsForCurrentWeek, timeSlotsForOtherWeek, _) = remember(
        scheduleParams.schedule,
        scheduleParams.currentWeek
    ) {
        scheduleParams.schedule.getTimeSlotsFor(weekdays[dayIndex], scheduleParams.currentWeek)
    }

    val emptySlots = remember(tableData.config.rows, renderedTimeSlots) {
        (1..tableData.config.rows).filterNot { it in renderedTimeSlots }
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

    // Current week courses
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
        val cellHeight = tableData.rowYOffsets[time.end] - tableData.rowYOffsets[time.start - 1]
        AnimatedContent(
            targetState = courses.isNotEmpty(),
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(cellHeight)
                .padding(bottom = 1.dp)
                .offset(y = tableData.rowYOffsets[time.start - 1] - 1.dp)
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
                    onEditCourse = { courseID, course -> editCourse(courseID, course) },
                    onCreateNewCourse = { course ->
                        val newCourseID = scheduleParams.schedule.newCourseId()
                        editCourse(newCourseID, course)
                    }
                )
            }
        }
    }

    // Other week courses
    timeSlotsForOtherWeek.forEach { time ->
        if ((time.start..time.end).all { it in renderedTimeSlots }) {
            return@forEach
        }
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
        val spanHeight = tableData.rowYOffsets[time.end] - tableData.rowYOffsets[time.start - 1]
        AnimatedContent(
            targetState = courses.isNotEmpty(),
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(spanHeight)
                .padding(bottom = 1.dp)
                .offset(y = tableData.rowYOffsets[time.start - 1] - 1.dp)
                .zIndex(99 - time.start + 1.0f / (time.end - time.start)),
            contentAlignment = Alignment.Center
        ) {
            if (it) {
                CourseCell(
                    schedule = scheduleParams.schedule,
                    courses = courses,
                    coursesForThisTime = coursesForThisTime,
                    currentWeek = scheduleParams.currentWeek,
                    totalWeeks = scheduleParams.schedule.totalWeeks,
                    displayOffSet = tableData.rowYOffsets[firstSpaceToDisplay - 1] - tableData.rowYOffsets[time.start - 1],
                    displayHeight = tableData.rowYOffsets[lastSpaceToDisplay] - tableData.rowYOffsets[firstSpaceToDisplay - 1],
                    onEditCourse = { courseID, course -> editCourse(courseID, course) },
                    onCreateNewCourse = { course ->
                        val newCourseID = scheduleParams.schedule.newCourseId()
                        editCourse(newCourseID, course)
                    }
                )
            }
        }
    }

    // Empty cells
    emptySlots.forEach { index ->
        Box(
            modifier = Modifier
                .height(tableData.rowHeights[index - 1])
                .padding(bottom = 1.dp)
                .offset(y = tableData.rowYOffsets[index - 1] - 1.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyTableCell(
                state = state,
                index = index,
                dayIndex = dayIndex,
                totalWeeks = scheduleParams.schedule.totalWeeks,
                onCreateNewCourse = { course ->
                    state.value = state.value.copy(isClicked = 2)
                    val courseID = scheduleParams.schedule.newCourseId()
                    editCourse(courseID, course)
                }
            )
        }
    }
}
