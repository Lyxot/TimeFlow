/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.schedule.subpage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import timeflow.app.generated.resources.Res
import timeflow.app.generated.resources.schedule_title_all_schedules
import timeflow.app.generated.resources.schedule_title_other_schedules
import timeflow.app.generated.resources.schedule_title_selected_schedule
import xyz.hyli.timeflow.ui.components.BasePreference
import xyz.hyli.timeflow.ui.components.CustomScaffold
import xyz.hyli.timeflow.ui.components.DialogStateNoData
import xyz.hyli.timeflow.ui.components.NavigationBackIcon
import xyz.hyli.timeflow.ui.components.PreferenceDivider
import xyz.hyli.timeflow.ui.components.PreferenceScreen
import xyz.hyli.timeflow.ui.components.PreferenceSection
import xyz.hyli.timeflow.ui.components.bottomPadding
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.pages.schedule.ConfirmSelectScheduleDialog
import xyz.hyli.timeflow.ui.pages.schedule.DeleteSelectedSchedulesDialog
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleListScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val settings by viewModel.settings.collectAsState()
    var multipleSelectionMode by remember { mutableStateOf(false) }
    val selectedSchedules = remember { mutableStateSetOf<Short>() }
    CustomScaffold(
        modifier = Modifier.fillMaxSize(),
        title = {
            Text(
                text = stringResource(Res.string.schedule_title_all_schedules)
            )
        },
        navigationIcon = {
            NavigationBackIcon(navHostController)
        },
        actions = {
            Row(
                horizontalArrangement = Arrangement.End,
            ) {
                AnimatedVisibility(
                    visible = multipleSelectionMode && selectedSchedules.isNotEmpty(),
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally(),
                ) {
                    val showConfirmDeleteSelectedSchedulesDialog = rememberDialogState()
                    IconButton(
                        onClick = {
                            showConfirmDeleteSelectedSchedulesDialog.show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    if (showConfirmDeleteSelectedSchedulesDialog.visible) {
                        DeleteSelectedSchedulesDialog(
                            selectedScheduleName = selectedSchedules.map {
                                settings.schedules[it]?.name ?: ""
                            },
                            onConfirm = {
                                selectedSchedules.forEach { id ->
                                    val schedule = settings.schedules[id]
                                    if (schedule != null) {
                                        viewModel.updateSchedule(
                                            id,
                                            schedule.copy(deleted = true)
                                        )
                                    }
                                }
                                selectedSchedules.clear()
                                multipleSelectionMode = false
                            },
                            showConfirmDeleteSelectedSchedulesDialog = showConfirmDeleteSelectedSchedulesDialog
                        )
                    }
                }
                IconButton(
                    onClick = {
                        multipleSelectionMode = !multipleSelectionMode
                        if (!multipleSelectionMode) {
                            selectedSchedules.clear()
                        }
                    }
                ) {
                    AnimatedContent(
                        targetState = multipleSelectionMode
                    ) { state ->
                        Icon(
                            imageVector = if (state) Icons.Default.Done else Icons.Default.Checklist,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    ) {
        PreferenceScreen(
            modifier = Modifier
                .fillMaxWidth()
                .bottomPadding()
        ) {
            val schedule by viewModel.selectedSchedule.collectAsState()
            if (settings.isScheduleSelected
                && schedule?.deleted == false
                && schedule != null
            ) {
                PreferenceSection(
                    title = stringResource(Res.string.schedule_title_selected_schedule)
                ) {
                    BasePreference(
                        title = schedule!!.name,
                        subtitle = schedule!!.termStartDate.toString() + " ~ " +
                                schedule!!.termEndDate.toString(),
                        onClick = {
                            navHostController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                            contentDescription = null
                        )
                    }
                }
            }
            if (settings.isScheduleSelected
                && settings.nonSelectedSchedules.isNotEmpty()
            ) {
                PreferenceDivider()
                PreferenceSection(
                    title = stringResource(Res.string.schedule_title_other_schedules)
                ) {
                    fun onClick(
                        id: Short,
                        showConfirmSelectScheduleDialog: DialogStateNoData
                    ) {
                        if (multipleSelectionMode) {
                            if (id in selectedSchedules) {
                                selectedSchedules.remove(id)
                            } else {
                                selectedSchedules.add(id)
                            }
                        } else {
                            showConfirmSelectScheduleDialog.show()
                        }
                    }
                    settings.nonSelectedSchedules.forEach { (id, schedule) ->
                        val showConfirmSelectScheduleDialog = rememberDialogState()
                        Row(
                            modifier = Modifier.animateContentSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedVisibility(
                                visible = multipleSelectionMode,
                                enter = expandHorizontally(),
                                exit = shrinkHorizontally(),
                            ) {
                                Checkbox(
                                    checked = id in selectedSchedules,
                                    onCheckedChange = {
                                        onClick(id, showConfirmSelectScheduleDialog)
                                    }
                                )
                            }
                            BasePreference(
                                title = schedule.name,
                                subtitle = schedule.termStartDate.toString() + " ~ " +
                                        schedule.termEndDate.toString(),
                                onClick = { onClick(id, showConfirmSelectScheduleDialog) }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                                    contentDescription = null
                                )
                            }
                        }
                        if (showConfirmSelectScheduleDialog.visible) {
                            ConfirmSelectScheduleDialog(
                                name = schedule.name,
                                showConfirmSelectScheduleDialog = showConfirmSelectScheduleDialog,
                                onConfirm = {
                                    viewModel.updateSelectedSchedule(id)
                                    navHostController.popBackStack()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}