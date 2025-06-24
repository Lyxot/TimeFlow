package xyz.hyli.timeflow.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import timeflow.composeapp.generated.resources.NotoSans
import timeflow.composeapp.generated.resources.Res

@Suppress("ObjectPropertyName")
private var _NotoSans: FontFamily? = null

val NotoSans: FontFamily
    @Composable
    get() {
        if (_NotoSans != null) return _NotoSans!!
        _NotoSans = FontFamily(
            Font(
                Res.font.NotoSans
            )
        )
        return _NotoSans!!
    }