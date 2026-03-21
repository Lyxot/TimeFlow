/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.viewmodel

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import org.jetbrains.compose.resources.getString
import xyz.hyli.timeflow.api.models.isImageBytes
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
    private val syncManager = SyncManager(repository, { settings.value }, tokenManager, viewModelScope)

    val syncState: StateFlow<SyncState> = syncManager.syncState
    val userInfo = syncManager.userInfo

    val isLoggedIn: StateFlow<Boolean> =
        settings.map { tokenManager.hasTokens() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = tokenManager.hasTokens()
            )

    private val _aiExtractionState = MutableStateFlow(AiExtractionState())
    val aiExtractionState: StateFlow<AiExtractionState> = _aiExtractionState.asStateFlow()

    @OptIn(ExperimentalEncodingApi::class)
    fun startAiExtraction(imageBytes: ByteArray) {
        _aiExtractionState.value = AiExtractionState(status = AiExtractionStatus.EXTRACTING)
        viewModelScope.launch {
            val imageBase64 = Base64.encode(imageBytes)
            syncManager.extractSchedule(imageBase64)
                .onSuccess { schedule ->
                    _aiExtractionState.value = AiExtractionState(
                        status = AiExtractionStatus.DONE,
                        extractedSchedule = schedule
                    )
                }
                .onFailure { e ->
                    _aiExtractionState.value = AiExtractionState(
                        status = AiExtractionStatus.ERROR,
                        error = e.message
                    )
                }
        }
    }

    fun confirmAiImport(schedule: Schedule) {
        createSchedule(schedule)
        clearAiExtractionState()
    }

    fun clearAiExtractionState() {
        _aiExtractionState.value = AiExtractionState()
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
            } else {
                val schedule = settings.value.schedules[id]
                if (schedule != null) {
                    repository.upsertSchedule(id, schedule.copy(deleted = true, updatedAt = Clock.System.now()))
                }
            }
            if (settings.value.selectedScheduleID == id) {
                repository.updateSelectedScheduleID(Settings.ZERO_ID)
                repository.updateSelectedScheduleUpdatedAt(Clock.System.now())
            }
            syncIfLoggedIn()
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

    override fun onCleared() {
        super.onCleared()
        syncManager.close()
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