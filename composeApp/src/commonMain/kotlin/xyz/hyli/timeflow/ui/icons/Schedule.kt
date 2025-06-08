package xyz.hyli.timeflow.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Schedule: ImageVector
    get() {
        if (_Schedule != null) {
            return _Schedule!!
        }
        _Schedule = ImageVector.Builder(
            name = "Schedule",
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
                moveTo(200f, 880f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(120f, 800f)
                verticalLineToRelative(-560f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(200f, 160f)
                horizontalLineToRelative(40f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(80f)
                horizontalLineToRelative(320f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(80f)
                horizontalLineToRelative(40f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(840f, 240f)
                verticalLineToRelative(560f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(760f, 880f)
                close()
                moveToRelative(0f, -80f)
                horizontalLineToRelative(560f)
                verticalLineToRelative(-400f)
                horizontalLineTo(200f)
                close()
                moveToRelative(0f, -480f)
                horizontalLineToRelative(560f)
                verticalLineToRelative(-80f)
                horizontalLineTo(200f)
                close()
                moveToRelative(0f, 0f)
                verticalLineToRelative(-80f)
                close()
                moveToRelative(80f, 240f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(400f)
                verticalLineToRelative(80f)
                close()
                moveToRelative(0f, 160f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(280f)
                verticalLineToRelative(80f)
                close()
            }
        }.build()
        return _Schedule!!
    }

@Suppress("ObjectPropertyName")
private var _Schedule: ImageVector? = null

val ScheduleFilled: ImageVector
    get() {
        if (_ScheduleFilled != null) {
            return _ScheduleFilled!!
        }
        _ScheduleFilled = ImageVector.Builder(
            name = "Schedule_filled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
            autoMirror = true
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(280f, 560f)
                lineTo(280f, 480f)
                lineTo(680f, 480f)
                lineTo(680f, 560f)
                lineTo(280f, 560f)
                close()
                moveTo(280f, 720f)
                lineTo(280f, 640f)
                lineTo(560f, 640f)
                lineTo(560f, 720f)
                lineTo(280f, 720f)
                close()
                moveTo(200f, 880f)
                quadTo(167f, 880f, 143.5f, 856.5f)
                quadTo(120f, 833f, 120f, 800f)
                lineTo(120f, 240f)
                quadTo(120f, 207f, 143.5f, 183.5f)
                quadTo(167f, 160f, 200f, 160f)
                lineTo(240f, 160f)
                lineTo(240f, 80f)
                lineTo(320f, 80f)
                lineTo(320f, 160f)
                lineTo(640f, 160f)
                lineTo(640f, 80f)
                lineTo(720f, 80f)
                lineTo(720f, 160f)
                lineTo(760f, 160f)
                quadTo(793f, 160f, 816.5f, 183.5f)
                quadTo(840f, 207f, 840f, 240f)
                lineTo(840f, 800f)
                quadTo(840f, 833f, 816.5f, 856.5f)
                quadTo(793f, 880f, 760f, 880f)
                lineTo(200f, 880f)
                close()
                moveTo(200f, 800f)
                lineTo(760f, 800f)
                quadTo(760f, 800f, 760f, 800f)
                quadTo(760f, 800f, 760f, 800f)
                lineTo(760f, 400f)
                lineTo(200f, 400f)
                lineTo(200f, 800f)
                quadTo(200f, 800f, 200f, 800f)
                quadTo(200f, 800f, 200f, 800f)
                close()
            }
        }.build()

        return _ScheduleFilled!!
    }

@Suppress("ObjectPropertyName")
private var _ScheduleFilled: ImageVector? = null
