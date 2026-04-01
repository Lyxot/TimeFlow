/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import xyz.hyli.timeflow.ui.sync.SyncStatus
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@Composable
fun SyncIconButton(viewModel: TimeFlowViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    if (!isLoggedIn) return

    val syncState by viewModel.syncState.collectAsState()
    val isSyncing = syncState.status == SyncStatus.SYNCING

    val rotation = remember { Animatable(-90f) }
    var wasSyncing by remember { mutableStateOf(false) }
    LaunchedEffect(isSyncing) {
        if (isSyncing) {
            wasSyncing = true
            rotation.animateTo(
                targetValue = rotation.value - 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else if (wasSyncing) {
            wasSyncing = false
            // Continue counter-clockwise to the nearest ±90° stop
            val remainder = ((rotation.value % 180f) + 180f) % 180f  // 0..180
            val target = rotation.value - remainder - 90f
            rotation.animateTo(
                targetValue = target,
                animationSpec = tween(500, easing = LinearEasing)
            )
        }
    }

    IconButton(
        onClick = { viewModel.sync() },
        enabled = !isSyncing
    ) {
        Box {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                modifier = Modifier.graphicsLayer {
                    rotationZ = rotation.value
                }
            )
            if (syncState.status == SyncStatus.SUCCESS || syncState.status == SyncStatus.ERROR) {
                val color = if (syncState.status == SyncStatus.SUCCESS)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
                val icon = if (syncState.status == SyncStatus.SUCCESS)
                    Icons.Default.Done
                else
                    Icons.Default.Close
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(8.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
