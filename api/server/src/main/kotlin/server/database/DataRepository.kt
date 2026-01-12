/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.database

import xyz.hyli.timeflow.api.models.User
import xyz.hyli.timeflow.data.Course
import xyz.hyli.timeflow.data.CourseSummary
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.ScheduleSummary
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * 数据仓库的接口，定义了所有数据库操作。
 */
@OptIn(ExperimentalUuidApi::class)
interface DataRepository {
    /**
     * 根据认证ID查找用户。
     * @param authId 来自认证系统（如JWT）的用户唯一ID。
     * @return 如果找到则返回 [User] 对象，否则返回 null。
     */
    suspend fun findUserByAuthId(authId: Uuid): User?

    /**
     * 根据邮箱地址查找用户。
     * @param email 要查找的用户邮箱。
     * @return 如果找到则返回 [User] 对象，否则返回 null。
     */
    suspend fun findUserByEmail(email: String): User?

    /**
     * 根据邮箱地址查找用户的密码哈希。
     * @param email 要查找的用户邮箱。
     * @return 如果找到则返回密码哈希字符串，否则返回 null。
     */
    suspend fun findPasswordHashByEmail(email: String): Pair<String, User>?

    /**
     * 创建一个新用户。
     * @param authId 新用户的认证ID。
     * @param username 新用户的用户名。
     * @param email 新用户的邮箱。
     * @param passwordHash 经过哈希处理的密码。
     * @return 创建成功后的 [User] 对象。
     */
    suspend fun createUser(authId: Uuid, username: String, email: String, passwordHash: String): User

    /**
     * Adds a new refresh token JTI to the database. 
     * @param userId The ID of the user owning the token.
     * @param jti The unique ID (JTI) of the JWT refresh token.
     * @param expiresAt The expiration timestamp of the refresh token.
     */
    suspend fun addRefreshToken(userId: Int, jti: Uuid, expiresAt: java.time.Instant): RefreshTokenEntity

    /**
     * Checks if a given refresh token JTI is valid (i.e., exists in the database).
     * @param jti The JTI to check.
     * @return True if the JTI is found, false otherwise.
     */
    suspend fun isRefreshTokenValid(userId: Int, jti: Uuid): Boolean

    /**
     * Revokes a refresh token by deleting its JTI from the database.
     * @param jti The JTI of the token to revoke.
     */
    suspend fun revokeRefreshToken(jti: Uuid)

    /**
     * Retrieves all schedules for a given user.
     * @param userId The ID of the user.
     * @param deleted If null (default), returns only non-deleted schedules. If true, returns only deleted schedules. If false, returns only non-deleted schedules.
     * @return A map of local schedule IDs to [ScheduleSummary] objects.
     */
    suspend fun getSchedules(userId: Int, deleted: Boolean? = null): Map<Short, ScheduleSummary>

    /**
     * Retrieves a single, full schedule for a given user by its local ID.
     * @param userId The ID of the user.
     * @param localId The local ID of the schedule.
     * @return The full [Schedule] object if found, otherwise null.
     */
    suspend fun getSchedule(userId: Int, localId: Short): Schedule?

    /**
     * Creates or updates a schedule for a given user.
     * @param userId The ID of the user.
     * @param localId The local ID of the schedule to upsert.
     * @param schedule The full schedule object to create or update.
     * @return True if a new schedule was created, false if an existing one was updated.
     */
    suspend fun upsertSchedule(userId: Int, localId: Short, schedule: Schedule): Boolean

    /**
     * Deletes a schedule for a given user by its local ID.
     * @param userId The ID of the user.
     * @param localId The local ID of the schedule to delete.
     * @param permanent If true, permanently delete the record; otherwise, performs a soft delete.
     * @return True if a schedule was found and deleted (soft or hard), false otherwise.
     */
    suspend fun deleteSchedule(userId: Int, localId: Short, permanent: Boolean = false): Boolean

    /**
     * Retrieves all course summaries for a given schedule.
     * @param userId The ID of the user.
     * @param scheduleLocalId The local ID of the schedule.
     * @return A map of local course IDs to [CourseSummary] objects, or null if the schedule is not found.
     */
    suspend fun getCourses(userId: Int, scheduleLocalId: Short): Map<Short, CourseSummary>?

    /**
     * Retrieves a single, full course for a given user by its schedule and course local IDs.
     * @param userId The ID of the user.
     * @param scheduleLocalId The local ID of the schedule containing the course.
     * @param courseLocalId The local ID of the course.
     * @return The full [Course] object if found, otherwise null.
     */
    suspend fun getCourse(userId: Int, scheduleLocalId: Short, courseLocalId: Short): Course?

    /**
     * Creates or updates a course within a given schedule. This is the public-facing method.
     * @param userId The ID of the user performing the action.
     * @param scheduleLocalId The local ID of the parent schedule.
     * @param courseLocalId The local ID of the course to upsert.
     * @param course The full course object.
     * @return True if a new course was created, false if updated, or null if the parent schedule was not found.
     */
    suspend fun upsertCourse(userId: Int, scheduleLocalId: Short, courseLocalId: Short, course: Course): Boolean?

    /**
     * Deletes a course for a given user by its schedule and course local IDs.
     * @param userId The ID of the user.
     * @param scheduleLocalId The local ID of the schedule containing the course.
     * @param courseLocalId The local ID of the course to delete.
     * @return True if a course was found and deleted, false otherwise.
     */
    suspend fun deleteCourse(userId: Int, scheduleLocalId: Short, courseLocalId: Short): Boolean
}
