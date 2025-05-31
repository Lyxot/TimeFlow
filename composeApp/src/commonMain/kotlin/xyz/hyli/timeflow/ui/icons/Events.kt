package xyz.hyli.timeflow.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Events: ImageVector
    get() {
        if (_Events != null) {
            return _Events!!
        }
        _Events = ImageVector.Builder(
            name = "Event_list",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(640f, 840f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(560f, 760f)
                verticalLineToRelative(-160f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(640f, 520f)
                horizontalLineToRelative(160f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(880f, 600f)
                verticalLineToRelative(160f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(800f, 840f)
                close()
                moveToRelative(0f, -80f)
                horizontalLineToRelative(160f)
                verticalLineToRelative(-160f)
                horizontalLineTo(640f)
                close()
                moveTo(80f, 720f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(360f)
                verticalLineToRelative(80f)
                close()
                moveToRelative(560f, -280f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(560f, 360f)
                verticalLineToRelative(-160f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(640f, 120f)
                horizontalLineToRelative(160f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(880f, 200f)
                verticalLineToRelative(160f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(800f, 440f)
                close()
                moveToRelative(0f, -80f)
                horizontalLineToRelative(160f)
                verticalLineToRelative(-160f)
                horizontalLineTo(640f)
                close()
                moveTo(80f, 320f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(360f)
                verticalLineToRelative(80f)
                close()
                moveToRelative(640f, -40f)
            }
        }.build()
        return _Events!!
    }

@Suppress("ObjectPropertyName")
private var _Events: ImageVector? = null

val EventsFilled: ImageVector
    get() {
        if (_EventsFilled != null) {
            return _EventsFilled!!
        }
        _EventsFilled = ImageVector.Builder(
            name = "IconName",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(640f, 840f)
                quadTo(607f, 840f, 583.5f, 816.5f)
                quadTo(560f, 793f, 560f, 760f)
                lineTo(560f, 600f)
                quadTo(560f, 567f, 583.5f, 543.5f)
                quadTo(607f, 520f, 640f, 520f)
                lineTo(800f, 520f)
                quadTo(833f, 520f, 856.5f, 543.5f)
                quadTo(880f, 567f, 880f, 600f)
                lineTo(880f, 760f)
                quadTo(880f, 793f, 856.5f, 816.5f)
                quadTo(833f, 840f, 800f, 840f)
                lineTo(640f, 840f)
                close()
                moveTo(80f, 720f)
                lineTo(80f, 640f)
                lineTo(440f, 640f)
                lineTo(440f, 720f)
                lineTo(80f, 720f)
                close()
                moveTo(640f, 440f)
                quadTo(607f, 440f, 583.5f, 416.5f)
                quadTo(560f, 393f, 560f, 360f)
                lineTo(560f, 200f)
                quadTo(560f, 167f, 583.5f, 143.5f)
                quadTo(607f, 120f, 640f, 120f)
                lineTo(800f, 120f)
                quadTo(833f, 120f, 856.5f, 143.5f)
                quadTo(880f, 167f, 880f, 200f)
                lineTo(880f, 360f)
                quadTo(880f, 393f, 856.5f, 416.5f)
                quadTo(833f, 440f, 800f, 440f)
                lineTo(640f, 440f)
                close()
                moveTo(80f, 320f)
                lineTo(80f, 240f)
                lineTo(440f, 240f)
                lineTo(440f, 320f)
                lineTo(80f, 320f)
                close()
            }
        }.build()

        return _EventsFilled!!
    }

@Suppress("ObjectPropertyName")
private var _EventsFilled: ImageVector? = null
