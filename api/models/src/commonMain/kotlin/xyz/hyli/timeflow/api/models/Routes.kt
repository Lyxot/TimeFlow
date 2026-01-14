/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

@file:Suppress("Unused")

package xyz.hyli.timeflow.api.models

import io.ktor.resources.*
import kotlinx.serialization.Serializable
import xyz.hyli.timeflow.data.Course
import xyz.hyli.timeflow.data.CourseSummary
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.ScheduleSummary

@Serializable
@Resource("/ping")
class Ping {
    @Serializable
    data class Response(val status: String = "ok")
}

@Serializable
@Resource("/version")
class Version {
    @Serializable
    data class Response(val version: String, val buildTime: Long, val commitHash: String)
}

@Serializable
@Resource("/api/v1")
class ApiV1 {
    @Serializable
    @Resource("auth")
    class Auth(val parent: ApiV1 = ApiV1()) {
        @Serializable
        @Resource("send-verification-code")
        class SendVerificationCode(val parent: Auth = Auth()) {
            @Serializable
            data class Payload(val email: String)
        }

        @Serializable
        @Resource("register")
        class Register(val parent: Auth = Auth()) {
            @Serializable
            data class Payload(val username: String, val email: String, val password: String, val code: String)
        }

        @Serializable
        @Resource("login")
        class Login(val parent: Auth = Auth()) {
            @Serializable
            data class Payload(val email: String, val password: String)
            typealias Response = Tokens
        }

        @Serializable
        @Resource("refresh")
        class Refresh(val parent: Auth = Auth(), val rotate: Boolean? = false) {
            typealias Response = Tokens
        }

        @Serializable
        @Resource("check-email")
        class CheckEmail(val parent: Auth = Auth(), val email: String = "") {
            @Serializable
            data class Response(val exists: Boolean)
        }

        @Serializable
        @Resource("logout")
        class Logout(val parent: Auth = Auth(), val all: Boolean? = null)
    }

    @Serializable
    @Resource("users")
    class Users(val parent: ApiV1 = ApiV1()) {
        @Serializable
        @Resource("me")
        class Me(val parent: Users = Users()) {
            typealias Response = User

            @Serializable
            @Resource("selected-schedule")
            class SelectedSchedule(val parent: Me = Me()) {
                typealias Payload = xyz.hyli.timeflow.api.models.SelectedSchedule
                typealias Response = xyz.hyli.timeflow.api.models.SelectedSchedule
            }
        }
    }

    @Serializable
    @Resource("schedules")
    class Schedules(val parent: ApiV1 = ApiV1(), val deleted: Boolean? = null) {
        // GET /schedules?deleted=true to get only deleted schedules
        // GET /schedules (default) to get only non-deleted schedules
        typealias Response = Map<Short, ScheduleSummary>

        @Serializable
        @Resource("{scheduleId}")
        class ScheduleId(val parent: Schedules = Schedules(), val scheduleId: Short) {
            // GET /schedules/{scheduleId}
            typealias Response = Schedule

            // PUT /schedules/{scheduleId}
            typealias Payload = Schedule

            @Serializable
            @Resource("courses")
            class Courses(val parent: ScheduleId) {
                // GET /schedules/{scheduleId}/courses
                typealias Response = Map<Short, CourseSummary>

                @Serializable
                @Resource("{courseId}")
                class CourseId(val parent: Courses, val courseId: Short) {
                    // GET /schedules/{scheduleId}/courses/{courseId}
                    typealias Response = Course

                    // PUT /schedules/{scheduleId}/courses/{courseId}
                    typealias Payload = Course
                }
            }
        }
    }
}
