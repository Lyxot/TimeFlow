/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.schedule.subpage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.ui.components.*
import xyz.hyli.timeflow.ui.navigation.Destination
import xyz.hyli.timeflow.ui.pages.schedule.ReadOnlyScheduleTable
import xyz.hyli.timeflow.ui.pages.settings.ScheduleSettingsContent
import xyz.hyli.timeflow.ui.pages.settings.subpage.LessonsPerDayContent
import xyz.hyli.timeflow.ui.viewmodel.AiExtractionStatus
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPreviewScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController,
) {
    val aiState by viewModel.aiExtractionState.collectAsState()
    var showDiscardDialog by remember { mutableStateOf(false) }

    // If not DONE (e.g. navigated here but state was cleared), go back
    if (aiState.status != AiExtractionStatus.DONE || aiState.editedSchedule == null) {
        return
    }

    val schedule = aiState.editedSchedule!!
    val isValid = schedule.name.isNotBlank()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val nameEmptyHint = stringResource(Res.string.ai_value_name_empty_hint)

    val handleBack: () -> Unit = {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(Res.string.ai_title_confirm_discard)) },
            text = { Text(stringResource(Res.string.ai_subtitle_confirm_discard)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    viewModel.clearAiExtractionState()
                    navHostController.popBackStack()
                }) {
                    Text(stringResource(Res.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    CustomScaffold(
        modifier = Modifier.fillMaxSize(),
        title = { Text(stringResource(Res.string.ai_title_result)) },
        navigationIcon = {
            IconButton(onClick = handleBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.back)
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    if (isValid) {
                        viewModel.confirmAiImport(schedule)
                        navHostController.popBackStack()
                    } else {
                        scope.launch { snackbarHostState.showSnackbar(nameEmptyHint) }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = stringResource(Res.string.ai_title_confirm_import)
                )
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
            PreferenceSection(
                title = stringResource(Res.string.settings_title_schedule)
            ) {
                ScheduleSettingsContent(
                    schedule = schedule,
                    onScheduleChanged = { viewModel.updateEditedSchedule(it) },
                    lessonsPerDayContent = {
                        BasePreference(
                            title = stringResource(Res.string.settings_title_schedule_lessons_per_day),
                            subtitle = stringResource(Res.string.settings_subtitle_schedule_lessons_per_day),
                            onClick = {
                                navHostController.navigate(Destination.Schedule.AiLessonsPerDay)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
            PreferenceSection(
                title = stringResource(Res.string.preview)
            ) {
                ReadOnlyScheduleTable(
                    schedule = schedule,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * LessonsPerDay screen for AI Preview, reads/writes to ViewModel's editedSchedule.
 */
@Composable
fun AiLessonsPerDayScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val aiState by viewModel.aiExtractionState.collectAsState()
    val schedule = aiState.editedSchedule ?: return

    LessonsPerDayContent(
        initialInfo = schedule.lessonTimePeriodInfo,
        navHostController = navHostController,
        onSave = { info ->
            viewModel.updateEditedSchedule(schedule.copy(lessonTimePeriodInfo = info))
        }
    )
}
