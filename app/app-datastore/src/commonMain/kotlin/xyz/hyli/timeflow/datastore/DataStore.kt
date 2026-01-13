/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.datastore

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.ExperimentalSerializationApi
import okio.*
import okio.Path.Companion.toPath
import xyz.hyli.timeflow.data.*
import kotlin.time.Instant

@OptIn(ExperimentalSerializationApi::class)
internal object SettingsProtobufSerializer : OkioSerializer<Settings> {
    override val defaultValue = Settings()

    override suspend fun readFrom(source: BufferedSource) =
        readSettingsFromByteArray(source.readByteArray()) ?: defaultValue

    override suspend fun writeTo(t: Settings, sink: BufferedSink) {
        sink.use {
            it.write(t.toProtoBufByteArray())
        }
    }

}

const val dataStoreFileName = "settings.pb"
lateinit var settingsFilePath: String

class SettingsDataStore(
    private val produceFilePath: () -> String,
) {
    init {
        settingsFilePath = produceFilePath()
    }

    private val db = DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = SettingsProtobufSerializer,
            producePath = {
                produceFilePath().toPath()
            },
        )
    )
    val settings: Flow<Settings> = db.data
    suspend fun updateFirstLaunch(versionCode: Int) {
        db.updateData { currentSettings ->
            currentSettings.copy(firstLaunch = versionCode)
        }
    }

    suspend fun updateTheme(themeMode: ThemeMode) {
        db.updateData { currentSettings ->
            currentSettings.copy(themeMode = themeMode)
        }
    }

    suspend fun updateThemeDynamicColor(themeDynamicColor: Boolean) {
        db.updateData { currentSettings ->
            currentSettings.copy(themeDynamicColor = themeDynamicColor)
        }
    }

    suspend fun updateThemeColor(color: Int) {
        db.updateData { currentSettings ->
            currentSettings.copy(themeColor = color)
        }
    }

    suspend fun updateSelectedScheduleID(id: Short) {
        db.updateData { currentSettings ->
            currentSettings.copy(selectedScheduleID = id)
        }
    }

    suspend fun updateSelectedScheduleUpdatedAt(updatedAt: Instant?) {
        db.updateData { currentSettings ->
            currentSettings.copy(selectedScheduleUpdatedAt = updatedAt)
        }
    }

    suspend fun upsertSchedule(id: Short, schedule: Schedule) {
        db.updateData { currentSettings ->
            val updatedSchedule = currentSettings.schedules.toMutableMap().apply {
                set(id, schedule)
            }
            currentSettings.copy(schedules = updatedSchedule)
        }
    }

    suspend fun deleteSchedule(id: Short) {
        db.updateData { currentSettings ->
            val updatedSchedule = currentSettings.schedules.toMutableMap()
            updatedSchedule.remove(id)
            currentSettings.copy(schedules = updatedSchedule)
        }
    }

    suspend fun updateSyncedAt(syncedAt: Instant?) {
        db.updateData { currentSettings ->
            currentSettings.copy(syncedAt = syncedAt)
        }
    }

    suspend fun reset() {
        db.updateData { _ ->
            Settings()
        }
    }
}
