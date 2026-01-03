/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.utils

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import xyz.hyli.timeflow.api.models.User
import xyz.hyli.timeflow.server.database.DataRepository
import xyz.hyli.timeflow.utils.toUuid
import kotlin.uuid.ExperimentalUuidApi

/**
 * A helper function that extracts the authenticated [User] from the JWT and passes it to the handler.
 * This function is used by the authed* routing wrappers below.
 * Routing handlers using this function must be inside an authenticate("access-auth") block.
 */
@OptIn(ExperimentalUuidApi::class)
inline fun <reified T : Any> authedRoutingBody(
    repository: DataRepository,
    noinline body: suspend RoutingContext.(T, User) -> Unit
): suspend RoutingContext.(T) -> Unit = { resource ->
    val principal = call.principal<JWTPrincipal>()!!
    val authId = principal.payload.getClaim("authId").asString().toUuid()
    val user = repository.findUserByAuthId(authId)!!

    body(resource, user)
}

/**
 * A wrapper for [get] that authenticates the request and provides the [User] object to the handler.
 */
inline fun <reified T : Any> Route.authedGet(
    repository: DataRepository,
    noinline body: suspend RoutingContext.(T, User) -> Unit
) = get<T>(authedRoutingBody(repository, body))

/**
 * A wrapper for [post] that authenticates the request and provides the [User] object to the handler.
 */
@OptIn(ExperimentalUuidApi::class)
inline fun <reified T : Any> Route.authedPost(
    repository: DataRepository,
    noinline body: suspend RoutingContext.(T, User) -> Unit
) = post<T>(authedRoutingBody(repository, body))

/**
 * A wrapper for [put] that authenticates the request and provides the [User] object to the handler.
 */
@OptIn(ExperimentalUuidApi::class)
inline fun <reified T : Any> Route.authedPut(
    repository: DataRepository,
    noinline body: suspend RoutingContext.(T, User) -> Unit
) = put<T>(authedRoutingBody(repository, body))

/**
 * A wrapper for [delete] that authenticates the request and provides the [User] object to the handler.
 */
@OptIn(ExperimentalUuidApi::class)
inline fun <reified T : Any> Route.authedDelete(
    repository: DataRepository,
    noinline body: suspend RoutingContext.(T, User) -> Unit
) = delete<T>(authedRoutingBody(repository, body))
