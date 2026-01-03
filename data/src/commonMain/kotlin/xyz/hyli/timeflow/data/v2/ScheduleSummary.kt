/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.data.v2

import kotlinx.serialization.Serializable

/**
 * A summary of a Schedule, containing essential information without the full course list.
 */
@Serializable
data class ScheduleSummary(
    val name: String,
    val deleted: Boolean,
    val termStartDate: Date,
    val termEndDate: Date
)