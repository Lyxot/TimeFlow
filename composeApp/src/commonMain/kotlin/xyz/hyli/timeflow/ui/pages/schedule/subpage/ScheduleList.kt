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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Checkbox
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
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.back
import timeflow.composeapp.generated.resources.schedule_title_all_schedules
import timeflow.composeapp.generated.resources.schedule_title_other_schedules
import timeflow.composeapp.generated.resources.schedule_title_selected_schedule
import xyz.hyli.timeflow.ui.components.BasePreference
import xyz.hyli.timeflow.ui.components.DialogStateNoData
import xyz.hyli.timeflow.ui.components.PreferenceDivider
import xyz.hyli.timeflow.ui.components.PreferenceScreen
import xyz.hyli.timeflow.ui.components.PreferenceSection
import xyz.hyli.timeflow.ui.components.commonPadding
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.pages.schedule.ConfirmSelectScheduleDialog
import xyz.hyli.timeflow.ui.pages.schedule.DeleteSelectedSchedulesDialog
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@Composable
fun ScheduleListScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val settings by viewModel.settings.collectAsState()
    var multipleSelectionMode by remember { mutableStateOf(false) }
    val selectedSchedules = remember { mutableStateSetOf<String>() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .commonPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Start,
            ) {
                IconButton(
                    onClick = {
                        navHostController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(Res.string.back)
                    )
                }
            }
            Text(
                text = stringResource(Res.string.schedule_title_all_schedules)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize(),
                horizontalArrangement = Arrangement.End,
            ) {
                AnimatedVisibility(
                    visible = multipleSelectionMode,
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
                                settings.schedule[it]?.name ?: ""
                            },
                            onConfirm = {
                                selectedSchedules.forEach { uuid ->
                                    val schedule = settings.schedule[uuid]
                                    if (schedule != null) {
                                        viewModel.updateSchedule(
                                            uuid,
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
        PreferenceScreen(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (settings.selectedSchedule.isNotEmpty()
                && settings.schedule[settings.selectedSchedule]?.deleted == false
            ) {
                PreferenceSection(
                    title = stringResource(Res.string.schedule_title_selected_schedule)
                ) {
                    BasePreference(
                        title = settings.schedule[settings.selectedSchedule]?.name ?: "",
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
            if (settings.selectedSchedule.isNotEmpty()
                && settings.schedule.values.any {
                    !it.deleted && it != settings.schedule[settings.selectedSchedule]
                }
            ) {
                PreferenceDivider()
                PreferenceSection(
                    title = stringResource(Res.string.schedule_title_other_schedules)
                ) {
                    fun onClick(
                        uuid: String,
                        showConfirmSelectScheduleDialog: DialogStateNoData
                    ) {
                        if (multipleSelectionMode) {
                            if (uuid in selectedSchedules) {
                                selectedSchedules.remove(uuid)
                            } else {
                                selectedSchedules.add(uuid)
                            }
                        } else {
                            showConfirmSelectScheduleDialog.show()
                        }
                    }
                    settings.schedule.filter {
                        !it.value.deleted && it.key != settings.selectedSchedule
                    }.forEach { (uuid, schedule) ->
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
                                    checked = uuid in selectedSchedules,
                                    onCheckedChange = {
                                        onClick(uuid, showConfirmSelectScheduleDialog)
                                    }
                                )
                            }
                            BasePreference(
                                title = schedule.name,
                                onClick = { onClick(uuid, showConfirmSelectScheduleDialog) }
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
                                    viewModel.updateSelectedSchedule(uuid)
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