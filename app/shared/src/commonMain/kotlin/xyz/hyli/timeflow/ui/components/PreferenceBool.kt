/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

// ==================== Preference Bool ====================

object PreferenceBoolStyle {
    enum class Style { Switch, Checkbox }
}

@Composable
fun PreferenceBool(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    title: String,
    subtitle: String? = null,
    style: PreferenceBoolStyle.Style = PreferenceBoolStyle.Style.Switch,
    enabled: Dependency = Dependency.Enabled,
    visible: Dependency = Dependency.Enabled
) {
    val isEnabled by enabled.asState()

    BasePreference(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        visible = visible,
        onClick = if (isEnabled) {
            { onValueChange(!value) }
        } else null,
        trailingContent = {
            when (style) {
                PreferenceBoolStyle.Style.Switch -> {
                    Switch(
                        checked = value,
                        onCheckedChange = if (isEnabled) onValueChange else null,
                        enabled = isEnabled
                    )
                }

                PreferenceBoolStyle.Style.Checkbox -> {
                    Checkbox(
                        checked = value,
                        onCheckedChange = if (isEnabled) onValueChange else null,
                        enabled = isEnabled
                    )
                }
            }
        }
    )
} 