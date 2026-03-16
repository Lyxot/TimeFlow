/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.sync

import io.ktor.client.call.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.hyli.timeflow.api.models.ApiV1
import xyz.hyli.timeflow.api.models.SelectedSchedule
import xyz.hyli.timeflow.client.ApiClient
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.ScheduleSummary
import xyz.hyli.timeflow.data.Settings
import xyz.hyli.timeflow.di.IDataRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SyncManager(
    private val repository: IDataRepository,
    private val settingsSnapshot: () -> Settings,
    private val tokenManager: RepositoryTokenManager,
    private val scope: CoroutineScope,
) {
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var apiClient: ApiClient? = null
    private var currentEndpoint: String? = null

    private fun getOrCreateClient(): ApiClient? {
        val endpoint = settingsSnapshot().apiEndpoint
        if (endpoint.isNullOrBlank()) return null
        if (apiClient == null || currentEndpoint != endpoint) {
            apiClient?.close()
            apiClient = ApiClient(tokenManager, endpoint)
            currentEndpoint = endpoint
        }
        return apiClient
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        val client = getOrCreateClient() ?: return Result.failure(IllegalStateException("API endpoint not configured"))
        return try {
            val response = client.login(ApiV1.Auth.Login.Payload(email, password))
            if (response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Login failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, email: String, password: String, code: String?): Result<Unit> {
        val client = getOrCreateClient() ?: return Result.failure(IllegalStateException("API endpoint not configured"))
        return try {
            val response = client.register(ApiV1.Auth.Register.Payload(username, email, password, code))
            if (response.status == HttpStatusCode.Created) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Registration failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            apiClient?.logout()
        } catch (_: Exception) {
            // Best effort
        }
        tokenManager.clearTokens()
        _syncState.value = SyncState()
    }

    suspend fun checkEmail(email: String): Result<Boolean> {
        val client = getOrCreateClient() ?: return Result.failure(IllegalStateException("API endpoint not configured"))
        return try {
            val response = client.checkEmail(email)
            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body<ApiV1.Auth.CheckEmail.Response>().exists)
            } else {
                Result.failure(Exception("Check email failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun emailVerification(): Result<Boolean> {
        val client = getOrCreateClient() ?: return Result.failure(IllegalStateException("API endpoint not configured"))
        return try {
            val response = client.emailVerification()
            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body<ApiV1.Auth.EmailVerification.Response>().enabled)
            } else {
                Result.failure(Exception("Email verification check failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendVerificationCode(email: String, turnstileToken: String? = null): Result<Unit> {
        val client = getOrCreateClient() ?: return Result.failure(IllegalStateException("API endpoint not configured"))
        return try {
            val response = client.sendVerificationCode(
                ApiV1.Auth.SendVerificationCode.Payload(email, turnstileToken)
            )
            if (response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Send verification code failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun sync() {
        scope.launch {
            performSync()
        }
    }

    private suspend fun performSync() {
        val client = getOrCreateClient() ?: run {
            _syncState.value = _syncState.value.copy(
                status = SyncStatus.ERROR,
                error = "API endpoint not configured"
            )
            return
        }

        val settings = settingsSnapshot()
        if (!settings.isLoggedIn) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatus.ERROR,
                error = "Not logged in"
            )
            return
        }

        _syncState.value = _syncState.value.copy(status = SyncStatus.SYNCING, error = null, conflicts = emptyList())

        try {
            // 1. Get server schedule summaries
            val schedulesResponse = client.schedules()
            if (schedulesResponse.status != HttpStatusCode.OK) {
                _syncState.value = _syncState.value.copy(
                    status = SyncStatus.ERROR,
                    error = "Failed to fetch schedules: ${schedulesResponse.status}"
                )
                return
            }
            val serverSummaries: Map<Short, ScheduleSummary> = schedulesResponse.body()

            // 2. Get server selected schedule (204 No Content = no selection)
            val selectedResponse = client.getSelectedSchedule()
            val serverSelected: SelectedSchedule? = if (selectedResponse.status == HttpStatusCode.OK) {
                selectedResponse.body()
            } else null

            // 3. Compute union of schedule IDs
            val localSchedules = settings.schedules
            val lastSyncedAt = settings.syncedAt
            val allIds = localSchedules.keys + serverSummaries.keys
            val conflicts = mutableListOf<ScheduleConflict>()

            for (id in allIds) {
                val localSchedule = localSchedules[id]
                val serverSummary = serverSummaries[id]

                when {
                    // Server-only: download
                    localSchedule == null && serverSummary != null -> {
                        val fullResponse = client.getSchedule(id)
                        if (fullResponse.status == HttpStatusCode.OK) {
                            val schedule: Schedule = fullResponse.body()
                            repository.upsertSchedule(id, schedule)
                        }
                    }
                    // Local-only: upload
                    localSchedule != null && serverSummary == null -> {
                        client.upsertSchedule(id, localSchedule)
                    }
                    // Both exist: compare updatedAt against syncedAt
                    localSchedule != null && serverSummary != null -> {
                        val localChanged = lastSyncedAt == null || localSchedule.updatedAt > lastSyncedAt
                        val serverChanged = lastSyncedAt == null || serverSummary.updatedAt > lastSyncedAt

                        when {
                            localChanged && serverChanged -> {
                                // Both changed - conflict
                                val fullResponse = client.getSchedule(id)
                                if (fullResponse.status == HttpStatusCode.OK) {
                                    val serverSchedule: Schedule = fullResponse.body()
                                    conflicts.add(
                                        ScheduleConflict(
                                            scheduleId = id,
                                            localSchedule = localSchedule,
                                            serverSchedule = serverSchedule,
                                            localUpdatedAt = localSchedule.updatedAt,
                                            serverUpdatedAt = serverSummary.updatedAt,
                                        )
                                    )
                                }
                            }

                            localChanged -> {
                                // Only local changed - upload
                                client.upsertSchedule(id, localSchedule)
                            }

                            serverChanged -> {
                                // Only server changed - download
                                val fullResponse = client.getSchedule(id)
                                if (fullResponse.status == HttpStatusCode.OK) {
                                    val schedule: Schedule = fullResponse.body()
                                    repository.upsertSchedule(id, schedule)
                                }
                            }
                            // Neither changed - skip
                        }
                    }
                }
            }

            // 4. Sync selected schedule (newer wins, unless conflict)
            if (serverSelected != null) {
                val localSelectedUpdatedAt = settings.selectedScheduleUpdatedAt
                val serverSelectedUpdatedAt = serverSelected.updatedAt

                val serverScheduleId = serverSelected.scheduleId
                if (serverScheduleId != null) {
                    when {
                        serverSelectedUpdatedAt != null && (localSelectedUpdatedAt == null || serverSelectedUpdatedAt > localSelectedUpdatedAt) -> {
                            repository.updateSelectedScheduleID(serverScheduleId)
                            repository.updateSelectedScheduleUpdatedAt(serverSelectedUpdatedAt)
                        }

                        else -> {
                            client.setSelectedSchedule(settings.selectedScheduleID, localSelectedUpdatedAt)
                        }
                    }
                }
            } else {
                // Server has no selection, push local
                if (settings.selectedScheduleID != Settings.ZERO_ID) {
                    client.setSelectedSchedule(settings.selectedScheduleID, settings.selectedScheduleUpdatedAt)
                }
            }

            // 5. Update syncedAt
            val now = Clock.System.now()
            repository.updateSyncedAt(now)

            _syncState.value = if (conflicts.isEmpty()) {
                SyncState(status = SyncStatus.SUCCESS, lastSyncedAt = now)
            } else {
                SyncState(status = SyncStatus.IDLE, lastSyncedAt = now, conflicts = conflicts)
            }
        } catch (e: Exception) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatus.ERROR,
                error = e.message ?: "Sync failed"
            )
        }
    }

    suspend fun resolveConflict(resolution: ConflictResolution) {
        val client = getOrCreateClient() ?: return
        val currentConflicts = _syncState.value.conflicts.toMutableList()

        when (resolution) {
            is ConflictResolution.KeepLocal -> {
                val conflict = currentConflicts.find { it.scheduleId == resolution.scheduleId } ?: return
                client.upsertSchedule(conflict.scheduleId, conflict.localSchedule)
                currentConflicts.removeAll { it.scheduleId == resolution.scheduleId }
            }

            is ConflictResolution.KeepServer -> {
                val conflict = currentConflicts.find { it.scheduleId == resolution.scheduleId } ?: return
                repository.upsertSchedule(conflict.scheduleId, conflict.serverSchedule)
                currentConflicts.removeAll { it.scheduleId == resolution.scheduleId }
            }
        }

        _syncState.value = _syncState.value.copy(conflicts = currentConflicts)
    }

    fun close() {
        apiClient?.close()
        apiClient = null
    }
}
