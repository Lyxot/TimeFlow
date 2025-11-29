package xyz.hyli.timeflow.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import timeflow.composeapp.generated.resources.Res

@Composable
internal actual fun getLibrariesState(): State<Libs?> {
    return produceLibraries {
        Res.readBytes("files/libraries.json").decodeToString()
    }
}