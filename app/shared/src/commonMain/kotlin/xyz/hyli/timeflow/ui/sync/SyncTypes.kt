/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.sync

import xyz.hyli.timeflow.data.Schedule
import kotlin.time.Instant

enum class SyncStatus { IDLE, SYNCING, SUCCESS, ERROR }

data class SyncState(
    val status: SyncStatus = SyncStatus.IDLE,
    val lastSyncedAt: Instant? = null,
    val error: String? = null,
    val conflicts: List<ScheduleConflict> = emptyList(),
)

data class ScheduleConflict(
    val scheduleId: Short,
    val localSchedule: Schedule,
    val serverSchedule: Schedule,
    val localUpdatedAt: Instant,
    val serverUpdatedAt: Instant,
)

sealed class ConflictResolution {
    data class KeepLocal(val scheduleId: Short) : ConflictResolution()
    data class KeepServer(val scheduleId: Short) : ConflictResolution()
}
