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
    override val defaultValue = Settings(
        theme = 0
    )

    override suspend fun readFrom(source: BufferedSource) =
        ProtoBuf.decodeFromByteArray<Settings>(source.readByteArray())

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
    suspend fun setTheme(theme: Int) {
        db.updateData { currentSettings ->
            currentSettings.copy(theme = theme)
        }
    }
}

