package xyz.hyli.timeflow.datastore

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.use

@OptIn(ExperimentalSerializationApi::class)
internal object SettingsProtobufSerializer : OkioSerializer<Settings> {
    override val defaultValue = Settings()

    override suspend fun readFrom(source: BufferedSource) =
        try {
            ProtoBuf.decodeFromByteArray<Settings>(source.readByteArray())
        } catch (_: Exception) {
            defaultValue
        }

    override suspend fun writeTo(t: Settings, sink: BufferedSink) {
        sink.use {
            it.write(ProtoBuf.encodeToByteArray(t))
        }
    }

}

internal const val dataStoreFileName = "settings.pb"

class SettingsDataStore(
    private val produceFilePath: () -> String,
) {
    private val db = DataStoreFactory.create(
        storage = OkioStorage<Settings>(
            fileSystem = FileSystem.SYSTEM,
            serializer = SettingsProtobufSerializer,
            producePath = {
                produceFilePath().toPath()
            },
        )
    )
    val settings: Flow<Settings> = db.data
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
    suspend fun updateThemeColor(color: Long) {
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

