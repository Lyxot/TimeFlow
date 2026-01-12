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
import xyz.hyli.timeflow.server.utils.authedGet
import xyz.hyli.timeflow.server.utils.authedPut

fun Route.usersRoutes(repository: DataRepository) {
    authenticate("access-auth") {
        authedGet<ApiV1.Users.Me>(repository) { _, user ->
            call.respond(HttpStatusCode.OK, user)
        }

        // GET /users/me/selected-schedule
        authedGet<ApiV1.Users.Me.SelectedSchedule>(repository) { _, user ->
            val scheduleId = repository.getSelectedScheduleId(user.id)
            call.respond(HttpStatusCode.OK, ApiV1.Users.Me.SelectedSchedule.Response(scheduleId))
        }

        // PUT /users/me/selected-schedule
        authedPut<ApiV1.Users.Me.SelectedSchedule>(repository) { _, user ->
            val payload = call.receive<ApiV1.Users.Me.SelectedSchedule.Payload>()
            repository.setSelectedScheduleId(user.id, payload.scheduleId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}