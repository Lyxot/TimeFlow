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
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import xyz.hyli.timeflow.api.models.ApiV1
import xyz.hyli.timeflow.server.database.DataRepository

fun Route.usersRoutes(repository: DataRepository) {
    authenticate("access-auth") {
        get<ApiV1.Users.Me> {
            val principal = call.principal<JWTPrincipal>()
            val authId = principal!!.payload.getClaim("authId").asString()

            val user = repository.findUserByAuthId(authId)

            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                // This case should ideally not happen if the token is valid and the user hasn't been deleted.
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }
        }
    }
}