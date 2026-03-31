/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.viewmodel

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import org.jetbrains.compose.resources.getString
import xyz.hyli.timeflow.ai.ScheduleExtractor
import xyz.hyli.timeflow.ai.resizeImage
import xyz.hyli.timeflow.api.models.ApiV1
import xyz.hyli.timeflow.api.models.isImageBytes
import xyz.hyli.timeflow.client.HttpEngine
import xyz.hyli.timeflow.data.*
import xyz.hyli.timeflow.di.IAppContainer
import xyz.hyli.timeflow.di.IDataRepository
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.ui.sync.*
import xyz.hyli.timeflow.utils.writeBytesToFile
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

enum class AiExtractionStatus {
    IDLE, EXTRACTING, DONE, ERROR
}

data class AiExtractionState(
    val status: AiExtractionStatus = AiExtractionStatus.IDLE,
    val extractedSchedule: Schedule? = null,
    val editedSchedule: Schedule? = null,
    val error: String? = null
)

class TimeFlowViewModel(
    private val repository: IDataRepository
) : ViewModel() {
    val settings: StateFlow<Settings> =
        repository.settings
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = Settings(initialized = false)
            )

    val selectedSchedule: StateFlow<Schedule?> =
        settings.map { settings -> settings.selectedSchedule }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = null
            )

    private val secureTokenStorage = createSecureTokenStorage()
    private val tokenManager = RepositoryTokenManager(secureTokenStorage)
    private val httpClient = HttpClient(HttpEngine)
    private val syncManager = SyncManager(repository, { settings.value }, tokenManager, viewModelScope, httpClient)

    val syncState: StateFlow<SyncState> = syncManager.syncState
    val userInfo = syncManager.userInfo
    val snackbarHostState = SnackbarHostState()

    val isLoggedIn: StateFlow<Boolean> =
        settings.map { tokenManager.hasTokens() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = tokenManager.hasTokens()
            )

    private var cachedAiInfo: ApiV1.Ai.Info.Response? = null
    private var cachedAiInfoKey: String? = null

    private suspend fun getOrFetchAiInfo(): ApiV1.Ai.Info.Response? {
        val key = "${settings.value.apiEndpoint}:${tokenManager.hasTokens()}"
        if (cachedAiInfoKey == key && cachedAiInfo != null) return cachedAiInfo
        val info = syncManager.getAiInfo()
        cachedAiInfo = info
        cachedAiInfoKey = key
        return info
    }

    val aiExtractionState: StateFlow<AiExtractionState>
        field = MutableStateFlow(AiExtractionState())

    @OptIn(ExperimentalEncodingApi::class)
    fun startAiExtraction(imageBytes: ByteArray) {
        aiExtractionState.value = AiExtractionState(status = AiExtractionStatus.EXTRACTING)
        viewModelScope.launch {
            val result = if (settings.value.aiConfig?.enabled == true) {
                val imageBase64 = Base64.encode(imageBytes)
                extractWithCustomAi(imageBase64)
            } else {
                // Compress image using server-advertised limits
                val info = getOrFetchAiInfo()
                val maxSize = info?.maxImageSizeBytes ?: DEFAULT_MAX_IMAGE_SIZE
                val maxRes = info?.maxImageResolution ?: DEFAULT_MAX_IMAGE_RESOLUTION
                val resized = resizeImage(imageBytes, maxSize, maxRes)
                val imageBase64 = Base64.encode(resized.data)
                syncManager.extractSchedule(imageBase64)
            }
            result.onSuccess { schedule ->
                    if (schedule.courses.isEmpty()) {
                        aiExtractionState.value = AiExtractionState(
                            status = AiExtractionStatus.ERROR,
                            error = getString(Res.string.ai_value_no_courses)
                        )
                    } else {
                        aiExtractionState.value = AiExtractionState(
                            status = AiExtractionStatus.DONE,
                            extractedSchedule = schedule,
                            editedSchedule = schedule
                        )
                    }
                }
                .onFailure { e ->
                    aiExtractionState.value = AiExtractionState(
                        status = AiExtractionStatus.ERROR,
                        error = e.message
                    )
                }
            cachedAiInfo = null
            cachedAiInfoKey = null
        }
    }

    private suspend fun extractWithCustomAi(imageBase64: String): Result<Schedule> {
        val cfg = settings.value.aiConfig ?: return Result.failure(Exception("AI config not set"))
        val providerStr = cfg.provider.name.lowercase()
        val extractor = ScheduleExtractor(
            provider = providerStr,
            apiKey = cfg.apiKey,
            model = cfg.model,
            endpoint = cfg.endpoint.ifBlank { null },
            httpClient = httpClient
        )
        return try {
            val extraction = extractor.extractFull(imageBase64)
            Result.success(extraction.toSchedule())
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "AI extraction failed", e))
        } finally {
            extractor.close()
        }
    }

    /**
     * Check AI availability: custom config enabled, or server enabled with remaining quota.
     * @param onResult receives null if unavailable, or error message string if blocked, or empty string if OK.
     */
    fun checkAiAvailable(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val aiConfig = settings.value.aiConfig
            if (aiConfig?.enabled == true) {
                if (aiConfig.model.isEmpty()) {
                    onResult(getString(Res.string.ai_value_custom_model_not_set))
                } else {
                    onResult("")
                }
            } else {
                val info = getOrFetchAiInfo()
                if (info == null) {
                    onResult(null)
                } else if (!info.enabled) {
                    onResult(getString(Res.string.ai_value_not_enabled))
                } else if (info.quotaLimit != null && info.quotaUsed >= info.quotaLimit!!) {
                    onResult(getString(Res.string.ai_value_quota_exceeded, info.quotaUsed, info.quotaLimit!!))
                } else {
                    onResult("")
                }
            }
        }
    }

    fun updateEditedSchedule(schedule: Schedule) {
        aiExtractionState.value = aiExtractionState.value.copy(editedSchedule = schedule)
    }

    fun confirmAiImport(schedule: Schedule) {
        createSchedule(schedule)
        clearAiExtractionState()
    }

    fun clearAiExtractionState() {
        aiExtractionState.value = AiExtractionState()
    }

    init {
        viewModelScope.launch {
            var initialSyncDone = false
            settings.collect { s ->
                if (tokenManager.hasTokens() && !s.apiEndpoint.isNullOrBlank()) {
                    syncManager.loadCachedUserInfo(s)
                    if (!initialSyncDone) {
                        initialSyncDone = true
                        syncManager.fetchUserInfo()
                        syncManager.sync()
                    }
                }
            }
        }
        viewModelScope.launch {
            syncManager.events.collect { message ->
                val text = when (message) {
                    SyncManager.ERROR_UNAUTHORIZED -> getString(Res.string.error_unauthorized)
                    SyncManager.ERROR_SCHEDULE_QUOTA_EXCEEDED -> getString(Res.string.error_schedule_quota_exceeded)
                    else -> return@collect
                }
                snackbarHostState.showSnackbar(text)
            }
        }
    }

    fun updateFirstLaunch(versionCode: Int) {
        viewModelScope.launch {
            repository.updateFirstLaunch(versionCode)
        }
    }

    fun updateTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            repository.updateThemeMode(themeMode)
        }
    }

    fun updateThemeDynamicColor(themeDynamicColor: Boolean) {
        viewModelScope.launch {
            repository.updateThemeDynamicColor(themeDynamicColor)
        }
    }

    fun updateThemeColor(color: Int) {
        viewModelScope.launch {
            repository.updateThemeColor(color)
        }
    }

    fun updateSelectedSchedule(id: Short, updatedAt: Instant? = Clock.System.now()) {
        viewModelScope.launch {
            repository.updateSelectedScheduleID(id)
            repository.updateSelectedScheduleUpdatedAt(updatedAt)
            syncIfLoggedIn()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun createSchedule(schedule: Schedule) {
        val id = settings.value.newScheduleId()
        viewModelScope.launch {
            repository.upsertSchedule(id, schedule)
            repository.updateSelectedScheduleID(id)
            repository.updateSelectedScheduleUpdatedAt(schedule.updatedAt)
            syncIfLoggedIn()
        }
    }

    fun updateSchedule(
        id: Short = settings.value.selectedScheduleID, schedule: Schedule, updatedAt: Instant = Clock.System.now()
    ) {
        viewModelScope.launch {
            repository.upsertSchedule(id, schedule.copy(updatedAt = updatedAt))
            syncIfLoggedIn()
        }
    }

    fun deleteSchedule(id: Short, permanently: Boolean = false) {
        viewModelScope.launch {
            if (permanently) {
                repository.deleteSchedule(id)
                syncManager.deleteScheduleOnServer(id)
            } else {
                val schedule = settings.value.schedules[id]
                if (schedule != null) {
                    repository.upsertSchedule(id, schedule.copy(deleted = true, updatedAt = Clock.System.now()))
                }
                syncIfLoggedIn()
            }
            if (settings.value.selectedScheduleID == id) {
                repository.updateSelectedScheduleID(Settings.ZERO_ID)
                repository.updateSelectedScheduleUpdatedAt(Clock.System.now())
            }
        }
    }

    private fun syncIfLoggedIn() {
        if (tokenManager.hasTokens()) {
            syncManager.sync()
        }
    }

    fun updateSyncedAt(syncedAt: Instant? = Clock.System.now()) {
        viewModelScope.launch {
            repository.updateSyncedAt(syncedAt)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun exportScheduleToFile(
        id: Short = settings.value.selectedScheduleID,
        file: PlatformFile,
        showMessage: (String) -> Unit
    ) {
        viewModelScope.launch {
            val schedule = settings.value.schedules[id]
            if (schedule == null) {
                showMessage(
                    getString(
                        Res.string.schedule_value_export_schedule_failed,
                        "Schedule not found"
                    )
                )
                return@launch
            }
            try {
                writeBytesToFile(schedule.toProtoBufByteArray(), file)
                showMessage(
                    getString(
                        Res.string.schedule_value_export_schedule_success,
                        file.name
                    )
                )
            } catch (e: Exception) {
                showMessage(
                    getString(
                        Res.string.schedule_value_export_schedule_failed,
                        e.message ?: ""
                    )
                )
                e.printStackTrace()
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun importScheduleFromFile(
        file: PlatformFile,
        showMessage: (String) -> Unit,
        onAiExtractionAvailable: (suspend (ByteArray) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val bytes = file.readBytes()
                val importedSchedule = if (file.name.endsWith(".png", ignoreCase = true)) {
                    readScheduleFromPng(bytes) ?: readScheduleFromByteArray(bytes)
                } else {
                    readScheduleFromByteArray(bytes) ?: readScheduleFromPng(bytes)
                }
                if (importedSchedule != null) {
                    createSchedule(importedSchedule)
                    showMessage(
                        getString(
                            Res.string.schedule_value_import_schedule_success,
                            importedSchedule.name
                        )
                    )
                } else if (onAiExtractionAvailable != null && isImageBytes(bytes)) {
                    onAiExtractionAvailable(bytes)
                } else {
                    showMessage(
                        getString(
                            Res.string.schedule_value_import_schedule_failed,
                            "Unsupported file format"
                        )
                    )
                }
            } catch (e: Exception) {
                showMessage(
                    getString(
                        Res.string.schedule_value_import_schedule_failed,
                        e.message ?: ""
                    )
                )
                e.printStackTrace()
            }
        }
    }

    fun login(email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(syncManager.login(email, password))
        }
    }

    fun register(username: String, email: String, password: String, code: String?, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(syncManager.register(username, email, password, code))
        }
    }

    fun logout() {
        viewModelScope.launch {
            syncManager.logout()
        }
    }

    fun sync() {
        syncManager.sync()
    }

    fun resolveConflict(resolution: ConflictResolution) {
        viewModelScope.launch {
            syncManager.resolveConflict(resolution)
        }
    }

    fun updateApiEndpoint(endpoint: String?) {
        viewModelScope.launch {
            repository.updateApiEndpoint(endpoint)
        }
    }

    fun updateAiConfig(config: AiProviderConfig?) {
        viewModelScope.launch {
            repository.updateAiConfig(config)
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            syncManager.logout()
            repository.resetAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        syncManager.close()
        httpClient.close()
    }

    companion object {
        val APP_CONTAINER_KEY = CreationExtras.Key<IAppContainer>()
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContainer = this[APP_CONTAINER_KEY] as IAppContainer
                val repository = appContainer.dataRepository
                TimeFlowViewModel(repository = repository)
            }
        }

        fun newCreationExtras(appContainer: IAppContainer): CreationExtras =
            MutableCreationExtras().apply {
                set(APP_CONTAINER_KEY, appContainer)
            }
    }
}

private const val TIMEOUT_MILLIS = 5_000L
private const val DEFAULT_MAX_IMAGE_SIZE = 2_097_152L // 2 MB
private const val DEFAULT_MAX_IMAGE_RESOLUTION = 2048