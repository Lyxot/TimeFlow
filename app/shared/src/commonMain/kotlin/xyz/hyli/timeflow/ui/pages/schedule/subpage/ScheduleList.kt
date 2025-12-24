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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.shared.generated.resources.Res
import xyz.hyli.timeflow.shared.generated.resources.schedule_title_all_schedules
import xyz.hyli.timeflow.shared.generated.resources.schedule_title_deleted_schedule
import xyz.hyli.timeflow.shared.generated.resources.schedule_title_other_schedules
import xyz.hyli.timeflow.shared.generated.resources.schedule_title_recycle_bin
import xyz.hyli.timeflow.shared.generated.resources.schedule_title_restore_schedule
import xyz.hyli.timeflow.shared.generated.resources.schedule_title_selected_schedule
import xyz.hyli.timeflow.shared.generated.resources.schedule_title_update_selected_schedule
import xyz.hyli.timeflow.shared.generated.resources.schedule_value_deleted_schedule_permanently_success
import xyz.hyli.timeflow.shared.generated.resources.schedule_value_deleted_schedule_success
import xyz.hyli.timeflow.shared.generated.resources.schedule_value_recycle_bin_empty
import xyz.hyli.timeflow.shared.generated.resources.schedule_value_restore_schedule
import xyz.hyli.timeflow.shared.generated.resources.schedule_value_update_selected_schedule
import xyz.hyli.timeflow.shared.generated.resources.undo
import xyz.hyli.timeflow.ui.components.BasePreference
import xyz.hyli.timeflow.ui.components.CustomScaffold
import xyz.hyli.timeflow.ui.components.Dependency
import xyz.hyli.timeflow.ui.components.DialogStateNoData
import xyz.hyli.timeflow.ui.components.NavigationBackIcon
import xyz.hyli.timeflow.ui.components.PreferenceDivider
import xyz.hyli.timeflow.ui.components.PreferenceScreen
import xyz.hyli.timeflow.ui.components.PreferenceSection
import xyz.hyli.timeflow.ui.components.bottomPadding
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.pages.schedule.ConfirmActionDialog
import xyz.hyli.timeflow.ui.pages.schedule.DeleteSelectedSchedulesDialog
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScheduleListScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val settings by viewModel.settings.collectAsState()
    val recycleBinModeState = remember { mutableStateOf(false) }
    var recycleBinMode by recycleBinModeState
    var multipleSelectionMode by remember { mutableStateOf(false) }
    val selectedSchedules = remember { mutableStateSetOf<Short>() }
    val showConfirmDeleteSelectedSchedulesDialog = rememberDialogState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    CustomScaffold(
        modifier = Modifier.fillMaxSize(),
        title = {
            AnimatedContent(
                targetState = recycleBinMode
            ) {
                if (it) {
                    Text(stringResource(Res.string.schedule_title_recycle_bin))
                } else {
                    Text(stringResource(Res.string.schedule_title_all_schedules))
                }
            }
        },
        navigationIcon = {
            if (recycleBinMode) {
                NavigationBackIcon(
                    navHostController = navHostController,
                    onClick = {
                        recycleBinMode = false
                        multipleSelectionMode = false
                        selectedSchedules.clear()
                    },
                )
            } else {
                NavigationBackIcon(navHostController)
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.fillMaxWidth(0.75f)
            )
        }
    ) {
        PreferenceScreen(
            modifier = Modifier
                .fillMaxWidth()
                .bottomPadding()
        ) {
            val schedule by viewModel.selectedSchedule.collectAsState()
            val selectedScheduleDependency = Dependency.State(
                recycleBinModeState
            ) {
                !it && settings.isScheduleSelected
            }
            PreferenceSection(
                title = stringResource(Res.string.schedule_title_selected_schedule),
                visible = selectedScheduleDependency
            ) {
                BasePreference(
                    title = schedule!!.name,
                    subtitle = schedule!!.termStartDate.toString() + " ~ " +
                            schedule!!.termEndDate.toString(),
                    onClick = {
                        navHostController.popBackStack()
                    }
                )
            }
            if (recycleBinMode && settings.deletedSchedules.isEmpty()) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(Res.string.schedule_value_recycle_bin_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }

            val nonSelectedScheduleDependency = Dependency.State(
                recycleBinModeState
            ) {
                (it && settings.deletedSchedules.isNotEmpty()) ||
                        (!it && settings.nonSelectedSchedules.isNotEmpty())
            }

            if (selectedScheduleDependency.asState().value && nonSelectedScheduleDependency.asState().value) {
                PreferenceDivider()
            }

            PreferenceSection(
                title = if (recycleBinMode) {
                    stringResource(Res.string.schedule_title_deleted_schedule)
                } else {
                    stringResource(Res.string.schedule_title_other_schedules)
                },
                visible = nonSelectedScheduleDependency
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
                (if (recycleBinMode) settings.deletedSchedules
                else settings.nonSelectedSchedules).forEach { (id, schedule) ->
                    val showConfirmActionDialog = rememberDialogState()
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
                                    onClick(id, showConfirmActionDialog)
                                }
                            )
                        }
                        BasePreference(
                            title = schedule.name,
                            subtitle = schedule.termStartDate.toString() + " ~ " +
                                    schedule.termEndDate.toString(),
                            onClick = { onClick(id, showConfirmActionDialog) }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                                contentDescription = null
                            )
                        }
                    }
                    if (showConfirmActionDialog.visible) {
                        ConfirmActionDialog(
                            showConfirmSelectScheduleDialog = showConfirmActionDialog,
                            title = if (recycleBinMode) {
                                stringResource(Res.string.schedule_title_restore_schedule)
                            } else {
                                stringResource(Res.string.schedule_title_update_selected_schedule)
                            },
                            content = if (recycleBinMode) {
                                stringResource(
                                    Res.string.schedule_value_restore_schedule,
                                    schedule.name
                                )
                            } else {
                                stringResource(
                                    Res.string.schedule_value_update_selected_schedule,
                                    schedule.name
                                )
                            },
                            onConfirm = {
                                if (recycleBinMode) {
                                    viewModel.updateSchedule(
                                        id = id,
                                        schedule = schedule.copy(deleted = false)
                                    )
                                } else {
                                    viewModel.updateSelectedSchedule(id)
                                    navHostController.popBackStack()
                                }
                            },
                        )
                    }
                }
            }
        }

        fun showSnackbar(recycleBinMode: Boolean, ids: Set<Short>) {
            scope.launch {
                if (recycleBinMode) {
                    snackbarHostState.showSnackbar(
                        getString(
                            Res.string.schedule_value_deleted_schedule_permanently_success,
                            ids.joinToString(", ") {
                                "\"${settings.schedules[it]?.name}\""
                            }
                        )
                    )
                } else {
                    val result = snackbarHostState.showSnackbar(
                        message = getString(
                            Res.string.schedule_value_deleted_schedule_success,
                            ids.joinToString(", ") {
                                "\"${settings.schedules[it]?.name}\""
                            }
                        ),
                        actionLabel = getString(Res.string.undo)
                    )
                    when (result) {
                        SnackbarResult.Dismissed -> { /* Do nothing */
                        }

                        SnackbarResult.ActionPerformed -> {
                            ids.forEach { id ->
                                println("Restoring schedule ID $id")
                                viewModel.updateSchedule(
                                    id = id,
                                    schedule = settings.schedules[id]!!.copy(deleted = false)
                                )
                            }
                        }
                    }
                }
            }
        }
        if (showConfirmDeleteSelectedSchedulesDialog.visible) {
            DeleteSelectedSchedulesDialog(
                selectedScheduleName = selectedSchedules.map {
                    settings.schedules[it]?.name ?: ""
                },
                onConfirm = {
                    selectedSchedules.forEach { id ->
                        viewModel.deleteSchedule(id, recycleBinMode)
                    }
                    showSnackbar(recycleBinMode, selectedSchedules.toSet())
                    selectedSchedules.clear()
                    multipleSelectionMode = false
                    recycleBinMode = false
                },
                permanently = recycleBinMode,
                showConfirmDeleteSelectedSchedulesDialog = showConfirmDeleteSelectedSchedulesDialog
            )
        }
        HorizontalFloatingToolbar(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding()
                .align(Alignment.BottomCenter),
            expanded = true,
        ) {
            IconButton(
                onClick = {
                    if (multipleSelectionMode) {
                        if (selectedSchedules.isNotEmpty()) {
                            showConfirmDeleteSelectedSchedulesDialog.show()
                        }
                    } else {
                        recycleBinMode = !recycleBinMode
                    }
                }
            ) {
                val iconColor by animateColorAsState(
                    targetValue = if (multipleSelectionMode) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    tint = iconColor,
                    contentDescription = null
                )
            }
            AnimatedVisibility(
                visible = selectedSchedules.size == 1 && multipleSelectionMode && !recycleBinMode,
            ) {
                val id = selectedSchedules.firstOrNull() ?: return@AnimatedVisibility
                val saver = rememberFileSaverLauncher { file ->
                    if (file != null) {
                        viewModel.exportScheduleToFile(
                            id = id,
                            file = file,
                            showMessage = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    }
                    multipleSelectionMode = false
                }
                IconButton(
                    onClick = {
                        saver.launch(settings.schedules[id]!!.name, "pb")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.IosShare,
                        contentDescription = null
                    )
                }
                // TODO: More share methods
            }
            FilledTonalIconToggleButton(
                checked = multipleSelectionMode,
                onCheckedChange = {
                    multipleSelectionMode = it
                    if (!multipleSelectionMode) {
                        selectedSchedules.clear()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Checklist,
                    contentDescription = null
                )
            }
        }
    }
}