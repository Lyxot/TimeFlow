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
import okio.use
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.data.Settings

expect val platformFileSystem: FileSystem

@OptIn(ExperimentalSerializationApi::class)
internal object SettingsProtobufSerializer : OkioSerializer<Settings> {
    override val defaultValue = Settings()

    override suspend fun readFrom(source: BufferedSource) =
        source.readByteArray().toProtoBufData(defaultValue)!!

    override suspend fun writeTo(t: Settings, sink: BufferedSink) {
        sink.use {
            it.write(t.toProtoBufByteArray())
        }
    }

}

internal const val dataStoreFileName = "settings.pb"
lateinit var settingsFilePath: String

class SettingsDataStore(
    private val produceFilePath: () -> String,
) {
    init {
        settingsFilePath = produceFilePath()
    }

    private val db = DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = platformFileSystem,
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

    suspend fun updateTheme(theme: Int) {
        db.updateData { currentSettings ->
            currentSettings.copy(theme = theme)
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
    suspend fun updateSelectedSchedule(uuid: String) {
        db.updateData { currentSettings ->
            currentSettings.copy(selectedSchedule = uuid)
        }
    }
    suspend fun createSchedule(uuid: String, schedule: Schedule) {
        db.updateData { currentSettings ->
            val updatedSchedule = currentSettings.schedule.toMutableMap()
            updatedSchedule[uuid] = schedule
            currentSettings.copy(schedule = updatedSchedule)
        }
    }
    suspend fun updateSchedule(uuid: String, schedule: Schedule) {
        db.updateData { currentSettings ->
            val updatedSchedule = currentSettings.schedule.toMutableMap()
            updatedSchedule[uuid] = schedule
            currentSettings.copy(schedule = updatedSchedule)
        }
    }
    suspend fun reset() {
        db.updateData { currentSettings ->
            Settings()
        }
    }
}
