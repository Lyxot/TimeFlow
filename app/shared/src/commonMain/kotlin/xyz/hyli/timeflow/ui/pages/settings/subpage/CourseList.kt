/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.settings.subpage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.LocalNavSuiteType
import xyz.hyli.timeflow.data.*
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.ui.components.*
import xyz.hyli.timeflow.ui.navigation.Destination
import xyz.hyli.timeflow.ui.navigation.NavigationBarType
import xyz.hyli.timeflow.ui.pages.schedule.subpage.DeleteCourseButton
import xyz.hyli.timeflow.ui.pages.schedule.subpage.EditCourseContent
import xyz.hyli.timeflow.ui.pages.schedule.subpage.EditCourseStyle
import xyz.hyli.timeflow.ui.pages.schedule.subpage.confirmEditCourse
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@Composable
fun CourseListScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val schedule by viewModel.selectedSchedule.collectAsState()
    if (schedule == null) return

    val navSuiteType by LocalNavSuiteType.current

    val showEditCourseDialog = rememberDialogState()
    var selectCourseID by remember { mutableStateOf<Short?>(null) }
    var selectCourse by remember { mutableStateOf<Course?>(null) }

    fun editCourse(courseID: Short, course: Course) {
        if (navSuiteType !in NavigationBarType) {
            selectCourseID = courseID
            selectCourse = course
            showEditCourseDialog.show()
        } else {
            navHostController.navigate(Destination.Schedule.EditCourse(courseID, course))
        }
    }

    fun createNewCourse(course: Course) {
        val newCourseID = schedule!!.newCourseId()
        editCourse(newCourseID, course)
    }

    if (showEditCourseDialog.visible && selectCourse != null && selectCourseID != null) {
        val courseValue = remember(selectCourseID) { mutableStateOf(selectCourse!!) }
        var isConfirmEnabled by remember(selectCourseID) { mutableStateOf(false) }
        showEditCourseDialog.enableButton(
            button = DialogButtonType.Positive,
            enabled = isConfirmEnabled
        )
        MyDialog(
            state = showEditCourseDialog,
            title = {
                Text(
                    text =
                        if (selectCourse!! in schedule!!.courses.values)
                            stringResource(Res.string.schedule_title_edit_course)
                        else
                            stringResource(Res.string.schedule_title_add_course)
                )
            },
            buttons = DialogDefaults.buttons(
                positive = DialogButton(stringResource(Res.string.save)),
                negative = DialogButton(stringResource(Res.string.cancel)),
            ),
            onEvent = { event ->
                if (event.isPositiveButton) {
                    confirmEditCourse(
                        courseValue = courseValue,
                        courseID = selectCourseID!!,
                        viewModel = viewModel,
                        schedule = schedule!!
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                EditCourseContent(
                    style = EditCourseStyle.Dialog,
                    viewModel = viewModel,
                    courseID = selectCourseID!!,
                    initValue = selectCourse!!,
                    courseValue = courseValue,
                    enableConfirmAction = { isConfirmEnabled = it },
                )
                if (schedule!!.courses.contains(selectCourseID)) {
                    DeleteCourseButton(
                        onClick = {
                            viewModel.updateSchedule(
                                schedule = schedule!!.copy(
                                    courses = schedule!!.courses.toMutableMap().apply {
                                        remove(selectCourseID)
                                    }
                                )
                            )
                            showEditCourseDialog.dismiss()
                        }
                    )
                }
            }
        }
    }

    CourseListContent(
        schedule = schedule!!,
        navHostController = navHostController,
        onEditCourse = ::editCourse,
        onCreateNewCourse = ::createNewCourse
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListContent(
    schedule: Schedule,
    navHostController: NavHostController,
    onEditCourse: (Short, Course) -> Unit,
    onCreateNewCourse: (Course) -> Unit
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

    CustomScaffold(
        modifier = Modifier.fillMaxSize(),
        title = {
            Text(stringResource(Res.string.settings_title_course_list))
        },
        navigationIcon = {
            NavigationBackIcon(navHostController)
        },
        actions = {
            IconButton(
                onClick = {
                    onCreateNewCourse(
                        Course(
                            name = "",
                            time = Range(1, 2),
                            weekday = Weekday.MONDAY,
                            week = WeekList(
                                weekDescription = WeekDescriptionEnum.ALL,
                                totalWeeks = schedule.totalWeeks
                            ),
                            color = -1
                        )
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    ) {
        if (schedule.courses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.settings_value_course_list_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            val coursesByWeekday = remember(schedule.courses) {
                schedule.courses.toList()
                    .groupBy { (_, course) -> course.weekday }
                    .toSortedMap(compareBy { it.ordinal })
            }

            PreferenceScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .bottomPadding()
            ) {
                coursesByWeekday.entries.forEachIndexed { groupIndex, (weekday, coursesForDay) ->
                    val sortedCourses = coursesForDay.sortedBy { (_, course) -> course.time.start }

                    PreferenceSection(
                        title = weekdayNames[weekday.ordinal]
                    ) {
                        sortedCourses.forEach { (courseID, course) ->
                            val timePeriod = stringResource(
                                Res.string.schedule_value_course_time_period,
                                course.time.start,
                                course.time.end
                            )
                            val subtitle = buildString {
                                append(timePeriod)
                                if (course.classroom.isNotBlank()) {
                                    append(" | ")
                                    append(course.classroom)
                                }
                                if (course.teacher.isNotBlank()) {
                                    append(" | ")
                                    append(course.teacher)
                                }
                            }
                            BasePreference(
                                title = course.name.ifBlank { "—" },
                                subtitle = subtitle,
                            ) {
                                Button(
                                    onClick = { onEditCourse(courseID, course) }
                                ) {
                                    Text(
                                        text = stringResource(Res.string.schedule_button_edit),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    if (groupIndex < coursesByWeekday.size - 1) {
                        PreferenceDivider()
                    }
                }
            }
        }
    }
}
