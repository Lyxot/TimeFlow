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
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import xyz.hyli.timeflow.api.models.ApiV1
import xyz.hyli.timeflow.server.config.SyncConfig
import xyz.hyli.timeflow.server.database.DataRepository
import xyz.hyli.timeflow.server.utils.authedDelete
import xyz.hyli.timeflow.server.utils.authedGet
import xyz.hyli.timeflow.server.utils.authedPut
import xyz.hyli.timeflow.utils.InputValidation

fun Route.schedulesRoutes(repository: DataRepository, syncConfig: SyncConfig) {
    authenticate("access-auth") {
        authedGet<ApiV1.Schedules>(repository) { resource, user ->
            val schedules = repository.getSchedules(user.id, resource.deleted)
            call.respond(HttpStatusCode.OK, schedules)
        }

        authedGet<ApiV1.Schedules.ScheduleId>(repository) { resource, user ->
            val schedule = repository.getSchedule(user.id, resource.scheduleId)
            if (schedule != null) {
                call.respond(HttpStatusCode.OK, schedule)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Schedule not found"))
            }
        }

        authedPut<ApiV1.Schedules.ScheduleId>(repository) { resource, user ->
            val scheduleData = call.receive<ApiV1.Schedules.ScheduleId.Payload>()
            InputValidation.validateName(scheduleData.name)?.let { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error))
                return@authedPut
            }
            if (!repository.scheduleExists(user.id, resource.scheduleId)) {
                if (!repository.isSyncUnlimited(user.id)) {
                    val used = repository.countSchedules(user.id)
                    if (used >= syncConfig.scheduleQuota) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            buildJsonObject {
                                put("error", "error_schedule_quota_exceeded")
                                put("used", used)
                                put("limit", syncConfig.scheduleQuota)
                            }
                        )
                        return@authedPut
                    }
                }
            }
            val wasCreated = repository.upsertSchedule(user.id, resource.scheduleId, scheduleData)
            if (wasCreated) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }

        authedDelete<ApiV1.Schedules.ScheduleId>(repository) { resource, user ->
            val wasDeleted = repository.deleteSchedule(user.id, resource.scheduleId)
            if (wasDeleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Schedule not found"))
            }
        }
    }
}
