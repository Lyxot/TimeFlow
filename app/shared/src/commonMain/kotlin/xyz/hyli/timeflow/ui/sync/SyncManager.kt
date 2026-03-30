/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.sync

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.hyli.timeflow.api.models.ApiV1
import xyz.hyli.timeflow.api.models.SelectedSchedule
import xyz.hyli.timeflow.api.models.User
import xyz.hyli.timeflow.api.models.parseExtractionResult
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
    private val client: HttpClient,
) {
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _userInfo = MutableStateFlow<User?>(null)
    val userInfo: StateFlow<User?> = _userInfo.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var apiClient: ApiClient? = null
    private var currentEndpoint: String? = null

    private fun getOrCreateClient(): ApiClient? {
        val endpoint = settingsSnapshot().apiEndpoint
        if (endpoint.isNullOrBlank()) return null
        if (apiClient == null || currentEndpoint != endpoint) {
            apiClient?.close()
            apiClient = ApiClient(tokenManager, endpoint, client)
            currentEndpoint = endpoint
        }
        return apiClient
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        val client = getOrCreateClient() ?: return Result.failure(Exception(ERROR_API_NOT_CONFIGURED))
        return try {
            val response = client.login(ApiV1.Auth.Login.Payload(email, password))
            if (response.status == HttpStatusCode.OK) {
                fetchUserInfo()
                sync()
                Result.success(Unit)
            } else {
                Result.failure(Exception(errorMessage(response, isAuth = true)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ERROR_NETWORK, e))
        }
    }

    fun loadCachedUserInfo(settings: Settings) {
        if (_userInfo.value == null && settings.cachedUserInfo != null) {
            _userInfo.value = settings.cachedUserInfo
        }
    }

    suspend fun fetchUserInfo() {
        val client = getOrCreateClient() ?: return
        try {
            val response = client.me()
            if (response.status == HttpStatusCode.OK) {
                val user: User = response.body()
                _userInfo.value = user
                repository.updateCachedUserInfo(user)
            }
        } catch (_: Exception) {
            // Best effort
        }
    }

    suspend fun register(username: String, email: String, password: String, code: String?): Result<Unit> {
        val client = getOrCreateClient() ?: return Result.failure(Exception(ERROR_API_NOT_CONFIGURED))
        return try {
            val response = client.register(ApiV1.Auth.Register.Payload(username, email, password, code))
            if (response.status == HttpStatusCode.Created) {
                login(email, password)
            } else {
                Result.failure(Exception(errorMessage(response, isAuth = true)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ERROR_NETWORK, e))
        }
    }

    suspend fun logout() {
        try {
            apiClient?.logout()
        } catch (_: Exception) {
            // Best effort
        }
        tokenManager.clearTokens()
        _userInfo.value = null
        _syncState.value = SyncState()
        repository.updateSyncedAt(null)
        repository.updateCachedUserInfo(null)
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
                error = ERROR_API_NOT_CONFIGURED
            )
            return
        }

        val settings = settingsSnapshot()
        if (!tokenManager.hasTokens()) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatus.ERROR,
                error = ERROR_UNAUTHORIZED
            )
            return
        }

        _syncState.value = _syncState.value.copy(status = SyncStatus.SYNCING, error = null, conflicts = emptyList())

        try {
            // 1. Get server schedule summaries
            val schedulesResponse = client.schedules()
            if (schedulesResponse.status != HttpStatusCode.OK) {
                val error = errorMessage(schedulesResponse)
                if (error == ERROR_UNAUTHORIZED) {
                    forceLogout()
                    return
                }
                _syncState.value = _syncState.value.copy(
                    status = SyncStatus.ERROR,
                    error = error
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
            var quotaExceeded = false

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
                        val response = client.upsertSchedule(id, localSchedule)
                        if (response.status == HttpStatusCode.Forbidden) {
                            quotaExceeded = true
                        }
                    }
                    // Both exist: compare updatedAt against syncedAt
                    localSchedule != null && serverSummary != null -> {
                        val localChanged = lastSyncedAt == null || localSchedule.updatedAt > lastSyncedAt
                        val serverChanged = lastSyncedAt == null || serverSummary.updatedAt > lastSyncedAt

                        when {
                            localChanged && serverChanged -> {
                                if (localSchedule.updatedAt == serverSummary.updatedAt) {
                                    // Same timestamp — no real conflict, skip
                                } else {
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

            if (quotaExceeded) _events.tryEmit(ERROR_SCHEDULE_QUOTA_EXCEEDED)

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

            _syncState.value = when {
                quotaExceeded -> SyncState(status = SyncStatus.ERROR, lastSyncedAt = now, error = ERROR_SCHEDULE_QUOTA_EXCEEDED)
                conflicts.isNotEmpty() -> SyncState(status = SyncStatus.IDLE, lastSyncedAt = now, conflicts = conflicts)
                else -> SyncState(status = SyncStatus.SUCCESS, lastSyncedAt = now)
            }
        } catch (e: Exception) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatus.ERROR,
                error = ERROR_NETWORK
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

    suspend fun getAiInfo(): ApiV1.Ai.Info.Response? {
        val client = getOrCreateClient() ?: return null
        if (!tokenManager.hasTokens()) return null
        return try {
            val response = client.aiInfo()
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiV1.Ai.Info.Response>()
            } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun extractSchedule(imageBase64: String): Result<Schedule> {
        val client = getOrCreateClient()
            ?: return Result.failure(Exception(ERROR_API_NOT_CONFIGURED))
        if (!tokenManager.hasTokens()) {
            return Result.failure(Exception(ERROR_UNAUTHORIZED))
        }
        return try {
            // Use stream=true so the server sends data incrementally,
            // keeping the socket alive during LLM inference.
            val response = client.extractSchedule(
                ApiV1.Ai.ExtractSchedule.Payload(image = imageBase64, stream = true)
            )
            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(errorMessage(response)))
            }
            // bodyAsText() collects the full SSE response.
            val sseText = response.bodyAsText()
            // Strip SSE framing: remove "data: " prefixes and [DONE] marker,
            // then concatenate to reconstruct the raw LLM output.
            val llmOutput = stripSseFraming(sseText)
            val result = parseExtractionResult(llmOutput)
            Result.success(result.toSchedule())
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: ERROR_NETWORK, e))
        }
    }

    private fun stripSseFraming(sseText: String): String {
        return sseText.lines()
            .map { line ->
                when {
                    line.startsWith("data: ") -> line.removePrefix("data: ")
                    line.startsWith("data:") -> line.removePrefix("data:")
                    else -> line
                }
            }
            .filter { it != "[DONE]" }
            .joinToString("")
    }

    private suspend fun forceLogout() {
        tokenManager.clearTokens()
        _userInfo.value = null
        _syncState.value = SyncState()
        repository.updateSyncedAt(null)
        repository.updateCachedUserInfo(null)
        _events.tryEmit(ERROR_UNAUTHORIZED)
    }

    fun close() {
        apiClient?.close()
        apiClient = null
    }

    private suspend fun errorMessage(response: HttpResponse, isAuth: Boolean = false): String {
        val serverMessage = try {
            val body = response.bodyAsText()
            val match = Regex("\"error\"\\s*:\\s*\"(.+?)\"").find(body)
            match?.groupValues?.get(1)
        } catch (_: Exception) {
            null
        }
        return when (response.status) {
            HttpStatusCode.Unauthorized -> if (isAuth) ERROR_INVALID_CREDENTIALS else ERROR_UNAUTHORIZED
            HttpStatusCode.TooManyRequests -> ERROR_TOO_MANY_REQUESTS
            HttpStatusCode.Conflict -> ERROR_EMAIL_ALREADY_EXISTS
            HttpStatusCode.NotFound -> ERROR_NOT_FOUND
            HttpStatusCode.BadRequest -> serverMessage ?: ERROR_INVALID_INPUT
            else -> if (response.status.value >= 500) ERROR_SERVER
            else serverMessage ?: "${response.status}"
        }
    }

    companion object {
        const val ERROR_INVALID_CREDENTIALS = "error.invalid_credentials"
        const val ERROR_TOO_MANY_REQUESTS = "error.too_many_requests"
        const val ERROR_EMAIL_ALREADY_EXISTS = "error.email_already_exists"
        const val ERROR_NOT_FOUND = "error.not_found"
        const val ERROR_INVALID_INPUT = "error.invalid_input"
        const val ERROR_UNAUTHORIZED = "error.unauthorized"
        const val ERROR_SERVER = "error.server"
        const val ERROR_NETWORK = "error.network"
        const val ERROR_API_NOT_CONFIGURED = "error.api_not_configured"
        const val ERROR_SCHEDULE_QUOTA_EXCEEDED = "error.schedule_quota_exceeded"
    }
}
