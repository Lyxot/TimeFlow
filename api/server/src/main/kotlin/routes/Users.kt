/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import xyz.hyli.timeflow.models.ApiV1
import xyz.hyli.timeflow.models.UserInfoResponse

fun Route.usersRoutes() {
    // Routes protected by ACCESS token validation
    authenticate("access-auth") {
        // GET /api/v1/users/me
        get<ApiV1.Users.Me> { _ ->
            val principal = call.principal<JWTPrincipal>()
            val userId = principal!!.payload.getClaim("userId").asString()

            // TODO: Fetch the user's full profile from the database using the userId.
            val userEmail = "test@test.com"

            call.respond(HttpStatusCode.OK, UserInfoResponse(id = userId, email = userEmail))
        }
    }
}