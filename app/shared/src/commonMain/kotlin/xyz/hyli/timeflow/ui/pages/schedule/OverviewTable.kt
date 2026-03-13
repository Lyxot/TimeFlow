/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import xyz.hyli.timeflow.LocalNavSuiteType
import xyz.hyli.timeflow.data.Course
import xyz.hyli.timeflow.data.Range
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.navigation.Destination
import xyz.hyli.timeflow.ui.navigation.NavigationBarType
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@Composable
fun OverviewScheduleTable(
    schedule: Schedule,
    navHostController: NavHostController,
    viewModel: TimeFlowViewModel,
    rows: Int,
    columns: Int,
    modifier: Modifier = Modifier
) {
    val config = ScheduleDisplayConfig(
        rows = rows,
        columns = columns,
        showDates = false,
        dateList = null,
        lessons = schedule.lessonTimePeriodInfo.lessons
    )

    val overviewData = remember(schedule) {
        List(columns) { dayIndex ->
            schedule.getOverviewTimeSlotsFor(weekdays[dayIndex])
        }
    }

    // Editing state
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

    ScheduleTableLayout(
        config = config,
        tableDataFactory = { cellWidth ->
            rememberOverviewTableData(schedule, config, cellWidth)
        },
        modifier = modifier
    ) { dayIndex, tableData ->
        overviewData[dayIndex].forEach { (range, courses) ->
            if (courses.isEmpty()) return@forEach
            val cellHeight = tableData.rowYOffsets[range.end] - tableData.rowYOffsets[range.start - 1]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellHeight)
                    .padding(bottom = 1.dp)
                    .offset(y = tableData.rowYOffsets[range.start - 1] - 1.dp)
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
