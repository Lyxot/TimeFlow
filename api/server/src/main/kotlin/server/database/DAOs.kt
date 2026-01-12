/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.database

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import xyz.hyli.timeflow.api.models.User
import xyz.hyli.timeflow.data.Course
import xyz.hyli.timeflow.data.Date
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.WeekList
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

/**
 * 与 UsersTable 对应的 DAO 实体。
 * DAO (Data Access Object) 模式允许我们像操作普通对象一样操作数据库的行。
 */
class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(UsersTable)

    var authId by UsersTable.authId
    var username by UsersTable.username
    var email by UsersTable.email
    var passwordHash by UsersTable.passwordHash
    var selectedScheduleId by UsersTable.selectedScheduleId


    @OptIn(ExperimentalUuidApi::class)
    val user: User
        get() = User(
            id = id.value,
            authId = authId.toKotlinUuid(),
            username = username,
            email = email
        )
}

/**
 * 与 RefreshTokensTable 对应的 DAO 实体。
 */
class RefreshTokenEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<RefreshTokenEntity>(RefreshTokensTable)

    var user by UserEntity referencedOn RefreshTokensTable.userId
    var jti by RefreshTokensTable.jti
    var expiresAt by RefreshTokensTable.expiresAt
}

/**
 * 与 SchedulesTable 对应的 DAO 实体。
 */
class ScheduleEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ScheduleEntity>(SchedulesTable)

    var user by UserEntity referencedOn SchedulesTable.userId
    var localId by SchedulesTable.localId
    var name by SchedulesTable.name
    var termStartDate by SchedulesTable.termStartDate
    var termEndDate by SchedulesTable.termEndDate
    var displayWeekends by SchedulesTable.displayWeekends
    var lessonTimePeriodInfo by SchedulesTable.lessonTimePeriodInfo
    var deleted by SchedulesTable.deleted

    // 反向引用，获取属于此课程表的所有课程实体
    val courses by CourseEntity referrersOn CoursesTable.scheduleId


    val schedule: Schedule
        get() = Schedule(
            name = name,
            // 将课程实体列表转换为 DTO 所需的 Map<Short, Course>
            courses = courses.associateBy({ it.localId }, { it.course }),
            termStartDate = Date(termStartDate),
            termEndDate = Date(termEndDate),
            lessonTimePeriodInfo = lessonTimePeriodInfo,
            displayWeekends = displayWeekends,
            deleted = deleted
        )
}

/**
 * 与 CoursesTable 对应的 DAO 实体。
 */
class CourseEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<CourseEntity>(CoursesTable)

    var schedule by ScheduleEntity referencedOn CoursesTable.scheduleId
    var localId by CoursesTable.localId
    var name by CoursesTable.name
    var teacher by CoursesTable.teacher
    var classroom by CoursesTable.classroom
    var time by CoursesTable.time
    var weekday by CoursesTable.weekday
    var color by CoursesTable.color
    var weeks by CoursesTable.weeks // 直接映射到 integer[] 列
    var note by CoursesTable.note


    val course: Course
        get() = Course(
            name = name,
            teacher = teacher ?: "",
            classroom = classroom ?: "",
            time = time,
            weekday = weekday,
            week = WeekList(weeks), // 将 List<Int> 包装成 WeekList
            color = color,
            note = note ?: ""
        )
}
