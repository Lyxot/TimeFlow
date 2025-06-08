package xyz.hyli.timeflow.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Settings: ImageVector
    get() {
        if (_Settings != null) {
            return _Settings!!
        }
        _Settings = ImageVector.Builder(
            name = "Settings",
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
                moveTo(370f, 880f)
                lineToRelative(-16f, -128f)
                quadToRelative(-13f, -5f, -24.5f, -12f)
                reflectiveQuadTo(307f, 725f)
                lineToRelative(-119f, 50f)
                lineTo(78f, 585f)
                lineToRelative(103f, -78f)
                quadToRelative(-1f, -7f, -1f, -13.5f)
                verticalLineToRelative(-27f)
                quadToRelative(0f, -6.5f, 1f, -13.5f)
                lineTo(78f, 375f)
                lineToRelative(110f, -190f)
                lineToRelative(119f, 50f)
                quadToRelative(11f, -8f, 23f, -15f)
                reflectiveQuadToRelative(24f, -12f)
                lineToRelative(16f, -128f)
                horizontalLineToRelative(220f)
                lineToRelative(16f, 128f)
                quadToRelative(13f, 5f, 24.5f, 12f)
                reflectiveQuadToRelative(22.5f, 15f)
                lineToRelative(119f, -50f)
                lineToRelative(110f, 190f)
                lineToRelative(-103f, 78f)
                quadToRelative(1f, 7f, 1f, 13.5f)
                verticalLineToRelative(27f)
                quadToRelative(0f, 6.5f, -2f, 13.5f)
                lineToRelative(103f, 78f)
                lineToRelative(-110f, 190f)
                lineToRelative(-118f, -50f)
                quadToRelative(-11f, 8f, -23f, 15f)
                reflectiveQuadToRelative(-24f, 12f)
                lineTo(590f, 880f)
                close()
                moveToRelative(70f, -80f)
                horizontalLineToRelative(79f)
                lineToRelative(14f, -106f)
                quadToRelative(31f, -8f, 57.5f, -23.5f)
                reflectiveQuadTo(639f, 633f)
                lineToRelative(99f, 41f)
                lineToRelative(39f, -68f)
                lineToRelative(-86f, -65f)
                quadToRelative(5f, -14f, 7f, -29.5f)
                reflectiveQuadToRelative(2f, -31.5f)
                reflectiveQuadToRelative(-2f, -31.5f)
                reflectiveQuadToRelative(-7f, -29.5f)
                lineToRelative(86f, -65f)
                lineToRelative(-39f, -68f)
                lineToRelative(-99f, 42f)
                quadToRelative(-22f, -23f, -48.5f, -38.5f)
                reflectiveQuadTo(533f, 266f)
                lineToRelative(-13f, -106f)
                horizontalLineToRelative(-79f)
                lineToRelative(-14f, 106f)
                quadToRelative(-31f, 8f, -57.5f, 23.5f)
                reflectiveQuadTo(321f, 327f)
                lineToRelative(-99f, -41f)
                lineToRelative(-39f, 68f)
                lineToRelative(86f, 64f)
                quadToRelative(-5f, 15f, -7f, 30f)
                reflectiveQuadToRelative(-2f, 32f)
                quadToRelative(0f, 16f, 2f, 31f)
                reflectiveQuadToRelative(7f, 30f)
                lineToRelative(-86f, 65f)
                lineToRelative(39f, 68f)
                lineToRelative(99f, -42f)
                quadToRelative(22f, 23f, 48.5f, 38.5f)
                reflectiveQuadTo(427f, 694f)
                close()
                moveToRelative(42f, -180f)
                quadToRelative(58f, 0f, 99f, -41f)
                reflectiveQuadToRelative(41f, -99f)
                reflectiveQuadToRelative(-41f, -99f)
                reflectiveQuadToRelative(-99f, -41f)
                quadToRelative(-59f, 0f, -99.5f, 41f)
                reflectiveQuadTo(342f, 480f)
                reflectiveQuadToRelative(40.5f, 99f)
                reflectiveQuadToRelative(99.5f, 41f)
                moveToRelative(-2f, -140f)
            }
        }.build()
        return _Settings!!
    }

@Suppress("ObjectPropertyName")
private var _Settings: ImageVector? = null

val SettingsFilled: ImageVector
    get() {
        if (_SettingsFilled != null) {
            return _SettingsFilled!!
        }
        _SettingsFilled = ImageVector.Builder(
            name = "Settings_filled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(370f, 880f)
                lineTo(354f, 752f)
                quadTo(341f, 747f, 329.5f, 740f)
                quadTo(318f, 733f, 307f, 725f)
                lineTo(188f, 775f)
                lineTo(78f, 585f)
                lineTo(181f, 507f)
                quadTo(180f, 500f, 180f, 493.5f)
                quadTo(180f, 487f, 180f, 480f)
                quadTo(180f, 473f, 180f, 466.5f)
                quadTo(180f, 460f, 181f, 453f)
                lineTo(78f, 375f)
                lineTo(188f, 185f)
                lineTo(307f, 235f)
                quadTo(318f, 227f, 330f, 220f)
                quadTo(342f, 213f, 354f, 208f)
                lineTo(370f, 80f)
                lineTo(590f, 80f)
                lineTo(606f, 208f)
                quadTo(619f, 213f, 630.5f, 220f)
                quadTo(642f, 227f, 653f, 235f)
                lineTo(772f, 185f)
                lineTo(882f, 375f)
                lineTo(779f, 453f)
                quadTo(780f, 460f, 780f, 466.5f)
                quadTo(780f, 473f, 780f, 480f)
                quadTo(780f, 487f, 780f, 493.5f)
                quadTo(780f, 500f, 778f, 507f)
                lineTo(881f, 585f)
                lineTo(771f, 775f)
                lineTo(653f, 725f)
                quadTo(642f, 733f, 630f, 740f)
                quadTo(618f, 747f, 606f, 752f)
                lineTo(590f, 880f)
                lineTo(370f, 880f)
                close()
                moveTo(482f, 620f)
                quadTo(540f, 620f, 581f, 579f)
                quadTo(622f, 538f, 622f, 480f)
                quadTo(622f, 422f, 581f, 381f)
                quadTo(540f, 340f, 482f, 340f)
                quadTo(423f, 340f, 382.5f, 381f)
                quadTo(342f, 422f, 342f, 480f)
                quadTo(342f, 538f, 382.5f, 579f)
                quadTo(423f, 620f, 482f, 620f)
                close()
            }
        }.build()

        return _SettingsFilled!!
    }

@Suppress("ObjectPropertyName")
private var _SettingsFilled: ImageVector? = null

