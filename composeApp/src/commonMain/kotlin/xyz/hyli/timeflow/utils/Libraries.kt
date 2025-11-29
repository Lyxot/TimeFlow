package xyz.hyli.timeflow.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.mikepenz.aboutlibraries.Libs

@Composable
internal expect fun getLibrariesState(): State<Libs?>