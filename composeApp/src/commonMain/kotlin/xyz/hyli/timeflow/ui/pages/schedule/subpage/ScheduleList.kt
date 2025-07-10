package xyz.hyli.timeflow.ui.pages.schedule.subpage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.back
import timeflow.composeapp.generated.resources.schedule_title_all_schedules
import timeflow.composeapp.generated.resources.schedule_title_other_schedules
import timeflow.composeapp.generated.resources.schedule_title_selected_schedule
import xyz.hyli.timeflow.ui.components.BasePreference
import xyz.hyli.timeflow.ui.components.PreferenceDivider
import xyz.hyli.timeflow.ui.components.PreferenceScreen
import xyz.hyli.timeflow.ui.components.PreferenceSection
import xyz.hyli.timeflow.ui.components.rememberDialogState
import xyz.hyli.timeflow.ui.pages.schedule.ConfirmSelectScheduleDialog
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop

@Composable
fun ScheduleListScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val settings by viewModel.settings.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .then(
                if (currentPlatform().isDesktop())
                    Modifier.padding(start = 4.dp, top = 4.dp)
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (currentPlatform().isDesktop())
                        Modifier.padding(end = 4.dp)
                    else Modifier
                ),
            verticalAlignment = Alignment.CenterVertically,
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
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(Res.string.schedule_title_all_schedules)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    // TODO: Multiple selection
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Checklist,
                    contentDescription = null
                )
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
                PreferenceDivider()
            }
            if (settings.selectedSchedule.isNotEmpty()
                && settings.schedule.values.any { !it.deleted }
            ) {
                PreferenceSection(
                    title = stringResource(Res.string.schedule_title_other_schedules)
                ) {
                    settings.schedule.filter {
                        !it.value.deleted && it.key != settings.selectedSchedule
                    }.forEach { (uuid, schedule) ->
                        val showConfirmSelectScheduleDialog = rememberDialogState()
                        BasePreference(
                            title = schedule.name,
                            onClick = {
                                showConfirmSelectScheduleDialog.show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                                contentDescription = null
                            )
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

            Spacer(
                modifier = Modifier.height(
                    maxOf(
                        WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding(),
                        24.dp
                    )
                )
            )
        }
    }
}