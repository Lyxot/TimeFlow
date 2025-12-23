/*
 * Copyright (c) 2025 Lyxot and contributors.
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
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.use
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.Settings
import xyz.hyli.timeflow.data.ThemeMode
import xyz.hyli.timeflow.data.readSettingsFromByteArray
import xyz.hyli.timeflow.data.toProtoBufByteArray

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

    suspend fun updateSelectedSchedule(id: Short) {
        db.updateData { currentSettings ->
            currentSettings.copy(selectedScheduleID = id)
        }
    }

    suspend fun createSchedule(id: Short, schedule: Schedule) {
        db.updateData { currentSettings ->
            val updatedSchedule = currentSettings.schedules.toMutableMap()
            updatedSchedule[id] = schedule
            currentSettings.copy(schedules = updatedSchedule)
        }
    }

    suspend fun updateSchedule(id: Short, schedule: Schedule) {
        db.updateData { currentSettings ->
            val updatedSchedule = currentSettings.schedules.toMutableMap()
            updatedSchedule[id] = schedule
            currentSettings.copy(schedules = updatedSchedule)
        }
    }

    suspend fun deleteSchedule(id: Short, permanently: Boolean) {
        db.updateData { currentSettings ->
            val updatedSchedule = currentSettings.schedules.toMutableMap()
            if (permanently) {
                updatedSchedule.remove(id)
            } else {
                val schedule = updatedSchedule[id]
                if (schedule != null) {
                    updatedSchedule[id] = schedule.copy(deleted = true)
                }
            }
            currentSettings.copy(schedules = updatedSchedule)
        }
    }

    suspend fun reset() {
        db.updateData { _ ->
            Settings()
        }
    }
}
