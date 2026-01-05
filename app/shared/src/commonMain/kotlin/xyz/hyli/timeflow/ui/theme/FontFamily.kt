/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import xyz.hyli.timeflow.shared.generated.resources.NotoSans
import xyz.hyli.timeflow.shared.generated.resources.Res

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