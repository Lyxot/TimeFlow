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
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import xyz.hyli.timeflow.api.models.ApiV1
import xyz.hyli.timeflow.server.database.DataRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Route.schedulesRoutes(repository: DataRepository) {
    authenticate("access-auth") {
        get<ApiV1.Schedules> {
            val principal = call.principal<JWTPrincipal>()
            val authId = Uuid.parse(principal!!.payload.getClaim("authId").asString())
            val user = repository.findUserByAuthId(authId)!!
            val schedules = repository.getSchedules(user.id)
            call.respond(HttpStatusCode.OK, schedules)
        }

        get<ApiV1.Schedules.ScheduleId> { resource ->
            val principal = call.principal<JWTPrincipal>()
            val authId = Uuid.parse(principal!!.payload.getClaim("authId").asString())
            val user = repository.findUserByAuthId(authId)!!

            val schedule = repository.getSchedule(user.id, resource.scheduleId)
            if (schedule != null) {
                call.respond(HttpStatusCode.OK, schedule)
            } else {
                call.respond(HttpStatusCode.NotFound, "Schedule not found")
            }
        }

        put<ApiV1.Schedules.ScheduleId> { resource ->
            val principal = call.principal<JWTPrincipal>()
            val authId = Uuid.parse(principal!!.payload.getClaim("authId").asString())
            val user = repository.findUserByAuthId(authId)!!

            val scheduleData = call.receive<ApiV1.Schedules.ScheduleId.Payload>()
            val wasCreated = repository.upsertSchedule(user.id, resource.scheduleId, scheduleData)
            if (wasCreated) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }

        delete<ApiV1.Schedules.ScheduleId> { resource ->
            val principal = call.principal<JWTPrincipal>()
            val authId = Uuid.parse(principal!!.payload.getClaim("authId").asString())
            val user = repository.findUserByAuthId(authId)!!

            val wasDeleted = repository.deleteSchedule(user.id, resource.scheduleId, resource.permanent ?: false)
            if (wasDeleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Schedule not found")
            }
        }
    }
}
