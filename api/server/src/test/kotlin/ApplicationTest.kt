/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow

import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import xyz.hyli.timeflow.api.models.ApiV1
import xyz.hyli.timeflow.api.models.Ping
import xyz.hyli.timeflow.api.models.User
import xyz.hyli.timeflow.api.models.Version
import xyz.hyli.timeflow.client.ApiClient
import xyz.hyli.timeflow.data.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun `test api`() = testApplication {
        environment {
            config = ApplicationConfig("application.yaml").mergeWith(
                MapApplicationConfig(
                    "postgres.embedded" to "true"
                )
            )
        }

        val tokenManager = FakeTokenManager()
        val apiClient = ApiClient(
            tokenManager = tokenManager,
            client = client,
        )

        apiClient.use { client ->
            // --- Health Check and Version API Tests ---

            /**
             * Test: GET /ping
             * Checks if the health check endpoint is alive and returns the correct response.
             */
            client.ping().apply {
                val message = "[GET /ping]"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertEquals(Ping.Response(), body<Ping.Response>(), "$message Body should be default Ping.Response")
                contentType()?.let {
                    assertTrue(
                        it.match(ContentType.Application.ProtoBuf),
                        "$message Content-Type should be ProtoBuf"
                    )
                }
            }

            /**
             * Test: GET /version
             * Checks if the version endpoint returns the correct build information.
             */
            client.version().apply {
                val message = "[GET /version]"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertEquals(
                    Version.Response(
                        version = BuildConfig.APP_VERSION_NAME,
                        buildTime = BuildConfig.BUILD_TIME,
                        commitHash = BuildConfig.GIT_COMMIT_HASH
                    ), body<Version.Response>(), "$message Body should match BuildConfig"
                )
            }

            // --- Authentication API Tests ---

            /**
             * Test: GET /auth/check-email (Non-existing)
             * Checks if a non-registered email returns exists=false.
             */
            client.checkEmail("test@test.com").apply {
                val message = "[GET /auth/check-email] Non-existing email"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertEquals(false, body<ApiV1.Auth.CheckEmail.Response>().exists, "$message 'exists' should be false")
            }

            /**
             * Test: POST /auth/send-verification-code
             * Checks if the server accepts a request to send a verification code.
             */
            client.sendVerificationCode(ApiV1.Auth.SendVerificationCode.Payload(email = "1@2.com")).apply {
                assertEquals(
                    HttpStatusCode.Accepted,
                    status,
                    "[POST /auth/send-verification-code] Status should be Accepted"
                )
            }

            /**
             * Test: POST /auth/register
             * Checks if a new user can be registered successfully.
             */
            client.register(
                ApiV1.Auth.Register.Payload(
                    username = "testuser",
                    email = "test@test.com",
                    password = "password",
                    code = "000000"
                )
            ).apply {
                assertEquals(HttpStatusCode.Created, status, "[POST /auth/register] Status should be Created")
            }

            /**
             * Test: POST /auth/login (Invalid)
             * Checks if logging in with incorrect credentials fails.
             */
            client.login(ApiV1.Auth.Login.Payload(email = "1@2.com", password = "123")).apply {
                assertEquals(
                    HttpStatusCode.Unauthorized,
                    status,
                    "[POST /auth/login] Invalid credentials should be Unauthorized"
                )
            }

            /**
             * Test: POST /auth/login (Valid)
             * Checks if a registered user can log in successfully and receives tokens.
             */
            client.login(ApiV1.Auth.Login.Payload(email = "test@test.com", password = "password")).apply {
                assertEquals(HttpStatusCode.OK, status, "[POST /auth/login] Valid credentials should be OK")
                // Further token checks could be added here
            }

            /**
             * Test: GET /auth/check-email (Existing)
             * Checks if a registered email returns exists=true.
             */
            client.checkEmail("test@test.com").apply {
                val message = "[GET /auth/check-email] Existing email"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertEquals(true, body<ApiV1.Auth.CheckEmail.Response>().exists, "$message 'exists' should be true")
            }

            /**
             * Test: GET /users/me
             * Checks if the endpoint returns the correct information for the logged-in user.
             */
            client.me().apply {
                val message = "[GET /users/me]"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                val body = body<User>()
                assertEquals("testuser", body.username, "$message Username should be correct")
                assertEquals("test@test.com", body.email, "$message Email should be correct")
            }

            // --- Selected Schedule API Tests ---

            /**
             * Test: GET /users/me/selected-schedule (Initial)
             * A new user should have no selected schedule.
             */
            client.getSelectedSchedule().apply {
                val message = "[GET /users/me/selected-schedule] Initial fetch"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertEquals(
                    null,
                    body<ApiV1.Users.Me.SelectedSchedule.Response>().scheduleId,
                    "$message Should be null"
                )
            }

            // --- Schedules API Tests ---

            /**
             * Test: GET /schedules (Initial)
             * A new user should have an empty map of schedules.
             */
            client.schedules().apply {
                val message = "[GET /schedules] Initial fetch"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertTrue(body<Map<Short, ScheduleSummary>>().isEmpty(), "$message Map should be empty")
            }

            val testScheduleId = 1145.toShort()
            val testSchedule = xyz.hyli.timeflow.data.Schedule(
                name = "Test Schedule", courses = emptyMap(),
                termStartDate = xyz.hyli.timeflow.data.Date(2025, 9, 1),
                termEndDate = xyz.hyli.timeflow.data.Date(2026, 1, 15),
                lessonTimePeriodInfo = xyz.hyli.timeflow.data.LessonTimePeriodInfo(
                    emptyList(),
                    emptyList(),
                    emptyList()
                ),
                displayWeekends = true, deleted = false
            )

            /**
             * Test: PUT /schedules/{id} (Create)
             * Creates a new schedule via the PUT endpoint.
             */
            client.upsertSchedule(testScheduleId, testSchedule).apply {
                assertEquals(HttpStatusCode.Created, status, "[PUT /schedules/{id}] Create should return 201 Created")
            }

            /**
             * Test: GET /schedules (After Create)
             * The list should now contain one schedule summary.
             */
            client.schedules().apply {
                val message = "[GET /schedules] After creation"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                val body = body<Map<Short, ScheduleSummary>>()
                assertEquals(1, body.size, "$message Map size should be 1")
                assertEquals(testSchedule.name, body[testScheduleId]?.name, "$message Schedule name should match")
            }

            /**
             * Test: GET /schedules/{id}
             * Fetches the full details of the newly created schedule.
             */
            client.getSchedule(testScheduleId).apply {
                val message = "[GET /schedules/{id}]"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertEquals(testSchedule.name, body<Schedule>().name, "$message Schedule name should match")
            }

            /**
             * Test: PUT /schedules/{id} (Update)
             * Updates an existing schedule.
             */
            val updatedSchedule = testSchedule.copy(name = "Updated Schedule Name")
            client.upsertSchedule(testScheduleId, updatedSchedule).apply {
                assertEquals(
                    HttpStatusCode.NoContent,
                    status,
                    "[PUT /schedules/{id}] Update should return 204 No Content"
                )
            }

            /**
             * Test: Verify Update
             * Fetches the schedule again to confirm its name has been updated.
             */
            client.getSchedule(testScheduleId).apply {
                val message = "[PUT /schedules/{id}] Verify update"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertEquals("Updated Schedule Name", body<Schedule>().name, "$message Name should be updated")
            }

            // --- Selected Schedule Tests (with existing schedule) ---

            /**
             * Test: PUT /users/me/selected-schedule
             * Sets the selected schedule to an existing schedule.
             */
            client.setSelectedSchedule(testScheduleId).apply {
                assertEquals(
                    HttpStatusCode.NoContent,
                    status,
                    "[PUT /users/me/selected-schedule] Should return 204 No Content"
                )
            }

            /**
             * Test: GET /users/me/selected-schedule (After setting)
             * Should return the selected schedule ID.
             */
            client.getSelectedSchedule().apply {
                val message = "[GET /users/me/selected-schedule] After setting"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertEquals(
                    testScheduleId,
                    body<ApiV1.Users.Me.SelectedSchedule.Response>().scheduleId,
                    "$message Should return the selected schedule ID"
                )
            }

            /**
             * Test: Soft delete selected schedule
             * After soft deleting, the selected schedule should return null.
             */
            client.deleteSchedule(testScheduleId, permanent = false).apply {
                assertEquals(
                    HttpStatusCode.NoContent,
                    status,
                    "[DELETE /schedules/{id}] Soft delete should return 204 No Content"
                )
            }

            client.getSelectedSchedule().apply {
                val message = "[GET /users/me/selected-schedule] After soft delete"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertEquals(
                    null,
                    body<ApiV1.Users.Me.SelectedSchedule.Response>().scheduleId,
                    "$message Should return null after schedule is deleted"
                )
            }

            /**
             * Test: Restore schedule (create with same ID)
             * Re-create the schedule to continue with course tests.
             */
            client.upsertSchedule(testScheduleId, updatedSchedule.copy(deleted = false)).apply {
                assertEquals(
                    HttpStatusCode.NoContent,
                    status,
                    "[PUT /schedules/{id}] Restore should return 204 No Content"
                )
            }

            /**
             * Test: Set selected schedule after restore
             * Should be able to select the restored schedule.
             */
            client.setSelectedSchedule(testScheduleId).apply {
                assertEquals(
                    HttpStatusCode.NoContent,
                    status,
                    "[PUT /users/me/selected-schedule] Should set restored schedule"
                )
            }

            client.getSelectedSchedule().apply {
                val message = "[GET /users/me/selected-schedule] After restore"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertEquals(
                    testScheduleId,
                    body<ApiV1.Users.Me.SelectedSchedule.Response>().scheduleId,
                    "$message Should return the selected schedule ID after restore"
                )
            }

            // --- Courses API Tests (on the existing schedule) ---

            /**
             * Test: GET /schedules/{id}/courses (Initial)
             * The new schedule should have an empty map of courses.
             */
            client.courses(testScheduleId).apply {
                val message = "[GET /.../courses] Initial fetch"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertTrue(body<Map<Short, CourseSummary>>().isEmpty(), "$message Map should be empty")
            }

            val testCourseId = 201.toShort()
            val testCourse = Course(
                name = "Test Course", teacher = "Mr. Ktor", classroom = "Server Room",
                time = Range(3, 4),
                weekday = Weekday.FRIDAY,
                week = WeekList(listOf(1, 2, 3, 4, 5)),
                color = 1, note = ""
            )

            /**
             * Test: PUT /schedules/{id}/courses/{id} (Create)
             * Creates a new course in the schedule.
             */
            client.upsertCourse(testScheduleId, testCourseId, testCourse).apply {
                assertEquals(HttpStatusCode.Created, status, "[PUT /.../courses/{id}] Create should return 201 Created")
            }

            /**
             * Test: GET /schedules/{id}/courses (After Create)
             * The course list should now contain one summary.
             */
            client.courses(testScheduleId).apply {
                val message = "[GET /.../courses] After creation"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                val body = body<ApiV1.Schedules.ScheduleId.Courses.Response>()
                assertEquals(1, body.size, "$message Map size should be 1")
                assertEquals(testCourse.name, body[testCourseId]?.name, "$message Course name should match")
            }

            /**
             * Test: GET /schedules/{id}/courses/{id}
             * Fetches the full details of the newly created course.
             */
            client.getCourse(testScheduleId, testCourseId).apply {
                val message = "[GET /.../courses/{id}]"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertEquals(testCourse.name, body<Course>().name, "$message Course name should match")
            }

            /**
             * Test: DELETE /schedules/{id}/courses/{id}
             * Deletes the course.
             */
            client.deleteCourse(testScheduleId, testCourseId).apply {
                assertEquals(
                    HttpStatusCode.NoContent,
                    status,
                    "[DELETE /.../courses/{id}] Delete should return 204 No Content"
                )
            }

            /**
             * Test: Verify Course Deletion
             * The course list should be empty again.
             */
            client.courses(testScheduleId).apply {
                val message = "[GET /.../courses] After deletion"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertTrue(body<Map<Short, CourseSummary>>().isEmpty(), "$message Map should be empty")
            }

            // --- Soft Delete Tests ---

            /**
             * Test: DELETE /schedules/{id} (Soft Delete)
             * Soft deletes the schedule (default behavior).
             */
            client.deleteSchedule(testScheduleId, permanent = false).apply {
                assertEquals(
                    HttpStatusCode.NoContent,
                    status,
                    "[DELETE /schedules/{id}] Soft delete should return 204 No Content"
                )
            }

            /**
             * Test: GET /schedules (After Soft Delete)
             * Non-deleted schedules list should be empty.
             */
            client.schedules(deleted = null).apply {
                val message = "[GET /schedules] After soft delete (default)"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertTrue(body<Map<Short, ScheduleSummary>>().isEmpty(), "$message Map should be empty")
            }

            /**
             * Test: GET /schedules?deleted=false
             * Explicitly requesting non-deleted schedules should also return empty.
             */
            client.schedules(deleted = false).apply {
                val message = "[GET /schedules?deleted=false]"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertTrue(body<Map<Short, ScheduleSummary>>().isEmpty(), "$message Map should be empty")
            }

            /**
             * Test: GET /schedules?deleted=true
             * Should return the soft-deleted schedule.
             */
            client.schedules(deleted = true).apply {
                val message = "[GET /schedules?deleted=true]"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                val body = body<Map<Short, ScheduleSummary>>()
                assertEquals(1, body.size, "$message Map size should be 1")
                assertEquals("Updated Schedule Name", body[testScheduleId]?.name, "$message Schedule name should match")
            }

            // --- Final Cleanup ---

            /**
             * Test: DELETE /schedules/{id} (Permanent)
             * Permanently deletes the parent schedule.
             */
            client.deleteSchedule(testScheduleId, permanent = true).apply {
                assertEquals(
                    HttpStatusCode.NoContent,
                    status,
                    "[DELETE /schedules/{id}] Permanent delete should return 204 No Content"
                )
            }

            /**
             * Test: Verify Permanent Schedule Deletion
             * Both non-deleted and deleted lists should be empty.
             */
            client.schedules(deleted = null).apply {
                val message = "[GET /schedules] After permanent deletion"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertTrue(body<Map<Short, ScheduleSummary>>().isEmpty(), "$message Map should be empty")
            }

            client.schedules(deleted = true).apply {
                val message = "[GET /schedules?deleted=true] After permanent deletion"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertTrue(body<Map<Short, ScheduleSummary>>().isEmpty(), "$message Map should be empty")
            }

            /**
             * Test: GET /users/me/selected-schedule (After permanent delete)
             * Should return null after the selected schedule is permanently deleted.
             */
            client.getSelectedSchedule().apply {
                val message = "[GET /users/me/selected-schedule] After permanent delete"
                assertEquals(HttpStatusCode.OK, status, "$message Status should be OK")
                assertEquals(
                    null,
                    body<ApiV1.Users.Me.SelectedSchedule.Response>().scheduleId,
                    "$message Should return null after schedule is permanently deleted"
                )
            }

            /**
             * Test: Automatic Token Refresh
             * Simulates an expired access token to ensure the ApiClient's interceptor handles refreshing it.
             */
            tokenManager.setAccessToken("invalid_token")
            client.me().apply {
                assertEquals(HttpStatusCode.OK, status, "[GET /users/me] Auto refresh access token should succeed")
            }
        }
    }
}

