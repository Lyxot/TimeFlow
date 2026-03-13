/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import xyz.hyli.timeflow.api.models.ApiV1
import xyz.hyli.timeflow.server.database.DataRepository
import xyz.hyli.timeflow.server.utils.authedDelete
import xyz.hyli.timeflow.server.utils.authedGet
import xyz.hyli.timeflow.server.utils.authedPut
import xyz.hyli.timeflow.utils.InputValidation

fun Route.coursesRoutes(repository: DataRepository) {
    authenticate("access-auth") {
        authedGet<ApiV1.Schedules.ScheduleId.Courses>(repository) { resource, user ->
            val courses = repository.getCourses(user.id, resource.parent.scheduleId)
            if (courses != null) {
                call.respond(HttpStatusCode.OK, courses)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Schedule not found"))
            }
        }

        authedGet<ApiV1.Schedules.ScheduleId.Courses.CourseId>(repository) { resource, user ->
            val course = repository.getCourse(
                user.id, resource.parent.parent.scheduleId, resource.courseId
            )
            if (course != null) {
                call.respond(HttpStatusCode.OK, course)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Schedule or Course not found"))
            }
        }

        authedPut<ApiV1.Schedules.ScheduleId.Courses.CourseId>(repository) { resource, user ->
            val courseData = call.receive<xyz.hyli.timeflow.data.Course>()
            InputValidation.validateName(courseData.name, "Course name")?.let { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
                return@authedPut
            }
            InputValidation.validateTeacher(courseData.teacher)?.let { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
                return@authedPut
            }
            InputValidation.validateClassroom(courseData.classroom)?.let { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
                return@authedPut
            }
            InputValidation.validateNote(courseData.note)?.let { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
                return@authedPut
            }
            val wasCreated = repository.upsertCourse(
                user.id, resource.parent.parent.scheduleId, resource.courseId, courseData
            )
            when (wasCreated) {
                true -> call.respond(HttpStatusCode.Created)
                false -> call.respond(HttpStatusCode.NoContent)
                null -> call.respond(HttpStatusCode.NotFound, mapOf("error" to "Schedule not found"))
            }
        }

        authedDelete<ApiV1.Schedules.ScheduleId.Courses.CourseId>(repository) { resource, user ->
            val wasDeleted = repository.deleteCourse(
                user.id, resource.parent.parent.scheduleId, resource.courseId
            )
            if (wasDeleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Course not found"))
            }
        }
    }
}
