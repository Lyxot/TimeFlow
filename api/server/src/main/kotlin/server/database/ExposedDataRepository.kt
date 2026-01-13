/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.database

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import xyz.hyli.timeflow.api.models.User
import xyz.hyli.timeflow.data.Course
import xyz.hyli.timeflow.data.CourseSummary
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.ScheduleSummary
import xyz.hyli.timeflow.server.database.DatabaseFactory.dbQuery
import xyz.hyli.timeflow.utils.InputValidation.truncateClassroom
import xyz.hyli.timeflow.utils.InputValidation.truncateEmail
import xyz.hyli.timeflow.utils.InputValidation.truncateName
import xyz.hyli.timeflow.utils.InputValidation.truncateNote
import xyz.hyli.timeflow.utils.InputValidation.truncateTeacher
import xyz.hyli.timeflow.utils.InputValidation.truncateUsername
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toKotlinInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/**
 * DataRepository 接口的 Exposed 实现，完全使用 DAO 模式。
 */
@OptIn(ExperimentalUuidApi::class)
class ExposedDataRepository : DataRepository {

    override suspend fun findUserByAuthId(authId: Uuid): User? = dbQuery {
        UserEntity
            .find { UsersTable.authId eq authId.toJavaUuid() }
            .singleOrNull()
            ?.user
    }

    override suspend fun findUserByEmail(email: String): User? = dbQuery {
        UserEntity
            .find { UsersTable.email eq email }
            .singleOrNull()
            ?.user
    }

    override suspend fun findPasswordHashByEmail(email: String): Pair<String, User>? = dbQuery {
        // 使用 DAO 模式查找实体，然后只返回 passwordHash 属性
        UserEntity
            .find { UsersTable.email eq email }
            .singleOrNull()
            ?.let {
                Pair(it.passwordHash, it.user)
            }
    }

    override suspend fun createUser(authId: Uuid, username: String, email: String, passwordHash: String): User =
        dbQuery {
            UserEntity.new {
                this.authId = authId.toJavaUuid()
                this.username = username.truncateUsername()
                this.email = email.truncateEmail()
                this.passwordHash = passwordHash
            }.user
        }

    override suspend fun addRefreshToken(userId: Int, jti: Uuid, expiresAt: java.time.Instant) = dbQuery {
        RefreshTokenEntity.new {
            this.user = UserEntity[userId]
            this.jti = jti.toJavaUuid()
            this.expiresAt = expiresAt.toKotlinInstant()
        }
    }


    override suspend fun isRefreshTokenValid(userId: Int, jti: Uuid): Boolean = dbQuery {
        RefreshTokenEntity
            .find {
                (RefreshTokensTable.userId eq userId) and
                        (RefreshTokensTable.jti eq jti.toJavaUuid())
            }
            .firstOrNull()
            ?.expiresAt
            ?.let {
                if (it > Clock.System.now()) true
                else {
                    revokeRefreshToken(jti)
                    false
                }
            } ?: false
    }

    override suspend fun revokeRefreshToken(jti: Uuid) {
        dbQuery {
            RefreshTokenEntity
                .find { RefreshTokensTable.jti eq jti.toJavaUuid() }
                .firstOrNull()
                ?.delete()
        }
    }

    override suspend fun getSelectedScheduleId(userId: Int): Pair<Short?, Instant?> = dbQuery {
        val userEntity = UserEntity[userId]
        val selectedId = userEntity.selectedScheduleId
        val updatedAt = userEntity.selectedScheduleUpdatedAt

        // Validate that the selected schedule exists and is not deleted
        val validatedId = if (selectedId != null && getScheduleEntity(userId, selectedId, false)?.schedule != null) {
            selectedId
        } else {
            null
        }

        Pair(validatedId, updatedAt)
    }

    override suspend fun setSelectedScheduleId(userId: Int, scheduleId: Short?, updatedAt: kotlin.time.Instant?) {
        dbQuery {
            val userEntity = UserEntity[userId]
            userEntity.selectedScheduleId = scheduleId
            userEntity.selectedScheduleUpdatedAt = updatedAt
        }
    }

    override suspend fun getSchedules(userId: Int, deleted: Boolean?): Map<Short, ScheduleSummary> = dbQuery {
        val deletedStatus = deleted ?: false
        ScheduleEntity
            .find {
                (SchedulesTable.userId eq userId) and
                        (SchedulesTable.deleted eq deletedStatus)
            }
            .associate { it.localId to it.schedule.summary }
    }

    override suspend fun getSchedule(userId: Int, localId: Short): Schedule? = dbQuery {
        getScheduleEntity(userId, localId)?.schedule
    }

    /**
     * Private helper to get ScheduleEntity by userId and localId.
     * @param userId The ID of the user.
     * @param localId The local ID of the schedule.
     * @param deleted If null, finds schedules regardless of deleted status. If false (default), only non-deleted. If true, only deleted.
     * @return The ScheduleEntity if found, otherwise null.
     */
    private fun getScheduleEntity(userId: Int, localId: Short, deleted: Boolean? = false): ScheduleEntity? =
        ScheduleEntity
            .find {
                val baseCondition = (SchedulesTable.userId eq userId) and (SchedulesTable.localId eq localId)
                if (deleted == null) {
                    baseCondition
                } else {
                    baseCondition and (SchedulesTable.deleted eq deleted)
                }
            }
            .singleOrNull()

    override suspend fun upsertSchedule(userId: Int, localId: Short, schedule: Schedule): Boolean = dbQuery {
        try {
            var wasCreated = false

            // 1. Find existing schedule (including soft-deleted) or create a new one
            val scheduleEntity = getScheduleEntity(userId, localId, deleted = null)
                ?.apply {
                    // It exists, so update its properties
                    this.name = schedule.name.truncateName()
                    this.termStartDate = schedule.termStartDate.toLocalDate()
                    this.termEndDate = schedule.termEndDate.toLocalDate()
                    this.displayWeekends = schedule.displayWeekends
                    this.lessonTimePeriodInfo = schedule.lessonTimePeriodInfo
                    this.deleted = schedule.deleted
                    this.createdAt = schedule.createdAt
                    this.updatedAt = schedule.updatedAt
                } ?: ScheduleEntity.new {
                // It does not exist, so create it
                wasCreated = true
                this.user = UserEntity[userId]
                this.localId = localId
                this.name = schedule.name.truncateName()
                this.termStartDate = schedule.termStartDate.toLocalDate()
                this.termEndDate = schedule.termEndDate.toLocalDate()
                this.displayWeekends = schedule.displayWeekends
                this.lessonTimePeriodInfo = schedule.lessonTimePeriodInfo
                this.deleted = schedule.deleted
                this.createdAt = schedule.createdAt
                this.updatedAt = schedule.updatedAt
            }

            // 2. Upsert courses using the helper function
            // If any course fails, the entire transaction will rollback
            scheduleEntity.schedule.courses.keys.filter { it !in schedule.courses.keys }.forEach { courseLocalId ->
                // Delete courses that are no longer present
                getCourseEntity(scheduleEntity, courseLocalId)?.delete()
            }
            // Upsert or create courses
            schedule.courses.forEach { (courseLocalId, course) ->
                upsertCourse(scheduleEntity, courseLocalId, course)
            }

            wasCreated
        } catch (e: Exception) {
            // Log error and rethrow to trigger transaction rollback
            throw IllegalStateException("Failed to upsert schedule: ${e.message}", e)
        }
    }

    override suspend fun deleteSchedule(userId: Int, localId: Short): Boolean = dbQuery {
        val scheduleEntity = ScheduleEntity.find {
            (SchedulesTable.userId eq userId) and (SchedulesTable.localId eq localId)
        }.singleOrNull()

        if (scheduleEntity != null) {
            scheduleEntity.delete()
            true
        } else {
            false
        }
    }

    override suspend fun getCourses(userId: Int, scheduleLocalId: Short): Map<Short, CourseSummary>? = dbQuery {
        getScheduleEntity(userId, scheduleLocalId)
            ?.courses
            ?.associate { it.localId to it.course.summary }
    }

    /**
     * Private helper to get CourseEntity by ScheduleEntity and course local ID.
     * @return The CourseEntity if found, otherwise null.
     */
    private fun getCourseEntity(
        scheduleEntity: ScheduleEntity?,
        courseLocalId: Short
    ): CourseEntity? = scheduleEntity?.let {
        CourseEntity.find {
            (CoursesTable.scheduleId eq it.id) and
                    (CoursesTable.localId eq courseLocalId)
        }.singleOrNull()
    }

    override suspend fun getCourse(userId: Int, scheduleLocalId: Short, courseLocalId: Short): Course? = dbQuery {
        val scheduleEntity = getScheduleEntity(userId, scheduleLocalId)
        getCourseEntity(scheduleEntity, courseLocalId)?.course
    }

    /**
     * Private helper to perform the actual course upsert logic, assuming the parent entity is already validated.
     * @return True if a new course was created, false if updated.
     */
    private fun upsertCourse(scheduleEntity: ScheduleEntity, localId: Short, course: Course): Boolean {
        var wasCreated = false
        getCourseEntity(scheduleEntity, localId)?.apply {
            // Course exists, update it
            this.name = course.name.truncateName()
            this.teacher = course.teacher.truncateTeacher()
            this.classroom = course.classroom.truncateClassroom()
            this.time = course.time
            this.weekday = course.weekday
            this.color = course.color
            this.weeks = course.week.weeks
            this.note = course.note.truncateNote()
        } ?: CourseEntity.new {
            // Course does not exist, create it
            wasCreated = true
            this.schedule = scheduleEntity
            this.localId = localId
            this.name = course.name.truncateName()
            this.teacher = course.teacher.truncateTeacher()
            this.classroom = course.classroom.truncateClassroom()
            this.time = course.time
            this.weekday = course.weekday
            this.color = course.color
            this.weeks = course.week.weeks
            this.note = course.note.truncateNote()
        }
        return wasCreated
    }

    override suspend fun upsertCourse(
        userId: Int, scheduleLocalId: Short, courseLocalId: Short, course: Course
    ): Boolean? = dbQuery {
        val scheduleEntity = getScheduleEntity(userId, scheduleLocalId)
        scheduleEntity?.let { upsertCourse(it, courseLocalId, course) }
    }

    override suspend fun deleteCourse(userId: Int, scheduleLocalId: Short, courseLocalId: Short): Boolean = dbQuery {
        val scheduleEntity = getScheduleEntity(userId, scheduleLocalId)
        val courseEntity = getCourseEntity(scheduleEntity, courseLocalId)

        if (courseEntity != null) {
            courseEntity.delete()
            true
        } else {
            false
        }
    }
}
