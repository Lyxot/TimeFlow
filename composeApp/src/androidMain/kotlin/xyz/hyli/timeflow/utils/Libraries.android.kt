package xyz.hyli.timeflow.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import xyz.hyli.timeflow.R

@Composable
internal actual fun getLibrariesState(): State<Libs?> {
    return produceLibraries(R.raw.libraries)
}