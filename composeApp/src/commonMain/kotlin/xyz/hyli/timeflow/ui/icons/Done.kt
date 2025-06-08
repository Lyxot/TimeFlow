package xyz.hyli.timeflow.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Done: ImageVector
    get() {
        if (_Done != null) return _Done!!

        _Done = ImageVector.Builder(
            name = "Done",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black)
            ) {
                moveTo(382f, 720f)
                lineTo(154f, 492f)
                lineToRelative(57f, -57f)
                lineToRelative(171f, 171f)
                lineToRelative(367f, -367f)
                lineToRelative(57f, 57f)
                close()
            }
        }.build()

        return _Done!!
    }

@Suppress("ObjectPropertyName")
private var _Done: ImageVector? = null
