/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.schedule.subpage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.ui.components.CustomScaffold
import xyz.hyli.timeflow.ui.components.bottomPadding
import xyz.hyli.timeflow.ui.pages.schedule.ReadOnlyScheduleTable
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
    var editedName by remember { mutableStateOf<String?>(null) }

    // Update editedName when schedule first arrives
    LaunchedEffect(aiState.extractedSchedule) {
        if (aiState.extractedSchedule != null && editedName == null) {
            editedName = aiState.extractedSchedule!!.name
        }
    }

    val handleBack: () -> Unit = {
        if (aiState.status == AiExtractionStatus.DONE) {
            showDiscardDialog = true
        } else {
            viewModel.clearAiExtractionState()
            navHostController.popBackStack()
        }
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
        title = {
            Text(
                when (aiState.status) {
                    AiExtractionStatus.EXTRACTING -> stringResource(Res.string.ai_title_extracting)
                    AiExtractionStatus.DONE -> stringResource(Res.string.ai_title_preview)
                    AiExtractionStatus.ERROR -> stringResource(Res.string.ai_title_extracting)
                    AiExtractionStatus.IDLE -> stringResource(Res.string.ai_title_extracting)
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = handleBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.back)
                )
            }
        },
        actions = {
            if (aiState.status == AiExtractionStatus.DONE && aiState.extractedSchedule != null) {
                IconButton(onClick = {
                    val schedule = aiState.extractedSchedule!!.let {
                        if (editedName != null) it.copy(name = editedName!!) else it
                    }
                    viewModel.confirmAiImport(schedule)
                    navHostController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = stringResource(Res.string.ai_title_confirm_import)
                    )
                }
            }
        }
    ) {
        when (aiState.status) {
            AiExtractionStatus.IDLE, AiExtractionStatus.EXTRACTING -> {
                ExtractingContent()
            }

            AiExtractionStatus.DONE -> {
                DoneContent(
                    viewModel = viewModel,
                    schedule = aiState.extractedSchedule!!,
                    editedName = editedName ?: "",
                    onNameChanged = { editedName = it }
                )
            }

            AiExtractionStatus.ERROR -> {
                ErrorContent(
                    error = aiState.error ?: "",
                    onRetry = {
                        // Cannot retry without the original bytes; navigate back
                        viewModel.clearAiExtractionState()
                        navHostController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun ExtractingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.ai_title_extracting),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DoneContent(
    viewModel: TimeFlowViewModel,
    schedule: xyz.hyli.timeflow.data.Schedule,
    editedName: String,
    onNameChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .bottomPadding()
    ) {
        // Schedule info section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = editedName,
                onValueChange = onNameChanged,
                label = { Text(stringResource(Res.string.ai_title_schedule_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${stringResource(Res.string.settings_title_schedule_term_start_date)}: ${schedule.termStartDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${stringResource(Res.string.settings_title_schedule_term_end_date)}: ${schedule.termEndDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${stringResource(Res.string.settings_title_schedule_total_weeks)}: ${schedule.totalWeeks}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Schedule preview table
        ReadOnlyScheduleTable(
            schedule = schedule,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.ai_value_extraction_error, error),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(Res.string.back))
        }
    }
}
