/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.database

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.json.jsonb
import xyz.hyli.timeflow.data.LessonTimePeriodInfo
import xyz.hyli.timeflow.data.Range
import xyz.hyli.timeflow.data.Weekday

/**
 * 用户表
 */
object UsersTable : IntIdTable("users") {
    val authId = uuid("auth_id").uniqueIndex()
    val username = varchar("username", 50)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
}

/**
 * 刷新令牌表
 */
object RefreshTokensTable : LongIdTable("refresh_tokens") {
    val userId = reference("user_id", UsersTable.id).index()
    val jti = uuid("jti").uniqueIndex()
    val expiresAt = timestamp("expires_at")
}

/**
 * 课程表主表
 */
object SchedulesTable : LongIdTable("schedules") {
    val userId = reference("user_id", UsersTable.id)
    val localId = short("local_id")
    val name = varchar("name", 255)
    val termStartDate = date("term_start_date")
    val termEndDate = date("term_end_date")
    val displayWeekends = bool("display_weekends")
    val lessonTimePeriodInfo = jsonb<LessonTimePeriodInfo>("lesson_time_period_info", Json.Default)
    val deleted = bool("deleted").default(false)

    init {
        uniqueIndex(userId, localId)
    }
}

/**
 * 课程信息表
 */
object CoursesTable : LongIdTable("courses") {
    val scheduleId = reference("schedule_id", SchedulesTable.id)
    val localId = short("local_id")
    val name = varchar("name", 255)
    val teacher = varchar("teacher", 128).nullable()
    val classroom = varchar("classroom", 128).nullable()
    val time = jsonb<Range>("time", Json.Default)
    val weekday = enumeration("weekday", Weekday::class)
    val color = integer("color")
    val weeks = array<Int>("weeks")
    val note = text("note").nullable()

    init {
        uniqueIndex(scheduleId, localId)
    }
}
