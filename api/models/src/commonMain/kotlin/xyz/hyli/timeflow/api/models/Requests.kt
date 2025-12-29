/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.api.models

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/api/v1")
class ApiV1 {

    @Serializable
    @Resource("auth")
    class Auth(val parent: ApiV1 = ApiV1()) {

        @Serializable
        @Resource("send-verification-code")
        class SendVerificationCode(val parent: Auth = Auth(), val email: String)

        @Serializable
        @Resource("register")
        class Register(val parent: Auth = Auth(), val email: String, val password: String, val code: String)

        @Serializable
        @Resource("login")
        class Login(val parent: Auth = Auth(), val email: String, val password: String)

        @Serializable
        @Resource("refresh")
        class Refresh(val parent: Auth = Auth(), val rotate: Boolean? = false)

        @Serializable
        @Resource("check-email")
        class CheckEmail(val parent: Auth = Auth(), val email: String)
    }

    @Serializable
    @Resource("users")
    class Users(val parent: ApiV1 = ApiV1()) {
        @Serializable
        @Resource("me")
        class Me(val parent: Users = Users())
    }
}