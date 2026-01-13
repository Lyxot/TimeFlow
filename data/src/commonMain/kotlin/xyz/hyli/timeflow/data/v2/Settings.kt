/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.data.v2

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import xyz.hyli.timeflow.data.newShortId
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.supportDynamicColor
import kotlin.time.Instant

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Settings(
    /**
     * 设置是否已初始化，始终为 true
     */
    @ProtoNumber(999) val initialized: Boolean = true,
    /**
     * 0: 未启动，版本号: 上次启动的版本号
     */
    @ProtoNumber(1) val firstLaunch: Int = 0,
    /**
     * 主题模式
     */
    @ProtoNumber(2) val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /**
     * 是否启用动态取色，仅适用于 Android 12 及以上版本或 Windows
     */
    @ProtoNumber(3) val themeDynamicColor: Boolean = currentPlatform().supportDynamicColor(),
    /**
     * RGB 格式的主题颜色
     */
    @ProtoNumber(4) val themeColor: Int = (0xFFCBDDEE).toInt(),
    /**
     * 课程表映射，键为课程表 ID，值为 Schedule 对象
     */
    @ProtoNumber(5) val schedules: Map<Short, Schedule> = emptyMap(),
    /**
     * 选中课程表的ID，如果没有选中则为0
     */
    @ProtoNumber(6) val selectedScheduleID: Short = ZERO_ID,
    @ProtoNumber(7) val selectedScheduleUpdatedAt: Instant? = null,
    @ProtoNumber(8) val syncedAt: Instant? = null,
    @ProtoNumber(9) val reserved9: String? = null,
    @ProtoNumber(10) val reserved10: String? = null,
    @ProtoNumber(11) val reserved11: String? = null,
) {
    companion object {
        const val ZERO_ID = 0.toShort()
    }

    /** 未被删除（仍在回收站外）的所有课程表。 */
    val nonDeletedSchedules: Map<Short, Schedule> =
        schedules.filterValues { !it.deleted }

    /** 未被删除且不是当前选中的所有课程表。 */
    val nonSelectedSchedules: Map<Short, Schedule> =
        schedules.filter { (id, schedule) ->
            id != selectedScheduleID && !schedule.deleted
        }

    /** 已被删除（在回收站内）的所有课程表。 */
    val deletedSchedules: Map<Short, Schedule> =
        schedules.filterValues { it.deleted }

    /** 检查当前是否有选中的、且未被删除的课程表。 */
    val isScheduleSelected: Boolean =
        selectedScheduleID != ZERO_ID && schedules.containsKey(selectedScheduleID) && !schedules[selectedScheduleID]!!.deleted

    /** 当前选中的课程表对象，如果没有选中则为 null。 */
    val selectedSchedule: Schedule? =
        if (isScheduleSelected) {
            schedules[selectedScheduleID]
        } else {
            null
        }

    /** 检查是否存在任何未被删除的课程表。 */
    val isScheduleEmpty: Boolean =
        nonDeletedSchedules.isEmpty()

    /**
     * 生成一个新的、不重复的课程表ID。
     * @return 新的课程表ID ([Short])。
     */
    fun newScheduleId(): Short =
        newShortId(schedules.keys)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
enum class ThemeMode {
    @ProtoNumber(1)
    SYSTEM,

    @ProtoNumber(2)
    LIGHT,

    @ProtoNumber(3)
    DARK,
}