/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server

import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import xyz.hyli.timeflow.server.routes.authRoutes
import xyz.hyli.timeflow.server.routes.usersRoutes
import xyz.hyli.timeflow.server.routes.utilRoutes

fun Application.configureRouting() {
    install(Resources)
    val tokenManager = TokenManager(environment.config)

    routing {
        utilRoutes()
        authRoutes(tokenManager)
        usersRoutes()
    }
}
