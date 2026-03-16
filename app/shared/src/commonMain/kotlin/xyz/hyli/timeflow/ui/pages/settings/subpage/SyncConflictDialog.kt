/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.settings.subpage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.ui.sync.ConflictResolution
import xyz.hyli.timeflow.ui.sync.ScheduleConflict

@Composable
fun SyncConflictDialog(
    conflict: ScheduleConflict,
    onResolve: (ConflictResolution) -> Unit,
) {
    val localTime = conflict.localUpdatedAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val serverTime = conflict.serverUpdatedAt.toLocalDateTime(TimeZone.currentSystemDefault())

    AlertDialog(
        onDismissRequest = { },
        title = { Text(stringResource(Res.string.sync_title_conflict)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(Res.string.sync_subtitle_conflict, conflict.localSchedule.name)
                )
                Text(
                    stringResource(Res.string.sync_value_local_updated_at, localTime.toString()),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    stringResource(Res.string.sync_value_server_updated_at, serverTime.toString()),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onResolve(ConflictResolution.KeepLocal(conflict.scheduleId)) }
            ) {
                Text(stringResource(Res.string.sync_button_keep_local))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onResolve(ConflictResolution.KeepServer(conflict.scheduleId)) }
            ) {
                Text(stringResource(Res.string.sync_button_keep_server))
            }
        }
    )
}
