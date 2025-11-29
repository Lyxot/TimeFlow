/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp

// 核心组件 (PreferenceCore.kt)
// - PreferenceItemPosition, LocalPreferenceItemPosition, getPreferenceItemShape
// - PreferenceScreen, PreferenceSection, AutoPositionedPreferenceGroup
// - PreferenceDivider, BasePreference
// - PreferenceScope, PreferenceScopeImpl, Dependency
// - DialogInputValidator, rememberDialogInputValidator

// 布尔值组件 (PreferenceBool.kt)
// - PreferenceBoolStyle, PreferenceBool

// 列表选择组件 (PreferenceList.kt)
// - PreferenceListStyle, PreferenceList

// 颜色选择组件 (PreferenceColor.kt)
// - PreferenceColor, ColorPickerDialog

// 日期选择组件 (PreferenceDate.kt)
// - PreferenceDate, DatePickerDialog

// 文本输入组件 (PreferenceInputText.kt)
// - PreferenceInputText, TextInputDialog

// 数字输入组件 (PreferenceNumber.kt)
// - PreferenceNumberStyle, PreferenceNumber, NumberInputDialog

// ==================== 使用示例 ====================
/*

现在所有偏好设置组件已经按功能模块分拆到不同文件中：

1. PreferenceCore.kt - 核心组件和基础设施
   - 位置管理、屏幕容器、分组容器、基础组件、依赖系统等

2. PreferenceBool.kt - 布尔值偏好设置
   - Switch和Checkbox两种样式的布尔值设置

3. PreferenceList.kt - 列表选择偏好设置
   - Dialog和SegmentedButtons两种样式的列表选择

4. PreferenceColor.kt - 颜色选择偏好设置
   - 颜色选择器和颜色选择对话框

5. PreferenceDate.kt - 日期选择偏好设置
   - 日期选择器和日期选择对话框

6. PreferenceInputText.kt - 文本输入偏好设置
   - 文本输入框和验证功能

7. PreferenceNumber.kt - 数字输入偏好设置
   - Slider和TextField两种样式的数字输入

所有组件都保持原有功能和API，包括：
- 完整的动画支持（可视性变化、大小变化、透明度动画）
- 自动位置计算（iOS风格的圆角）
- enabled和visible参数支持
- MyDialog集成
- 依赖管理系统

基本使用方式：

PreferenceScreen {
    PreferenceSection(title = "基础设置") {
        PreferenceBool(
            value = switchValue,
            onValueChange = { switchValue = it },
            title = "开关设置",
            style = PreferenceBoolStyle.Style.Switch
        )

        PreferenceList(
            value = selectedOption,
            onValueChange = { selectedOption = it },
            items = listOf("选项1", "选项2", "选项3"),
            itemTextProvider = { it },
            title = "列表选择",
            style = PreferenceListStyle.Style.SegmentedButtons
        )

        PreferenceNumber(
            value = numberValue,
            onValueChange = { numberValue = it },
            min = 0,
            max = 100,
            title = "数字设置",
            style = PreferenceNumberStyle.Slider(showValueBelow = true)
        )
    }

    PreferenceSection(title = "高级设置") {
        PreferenceColor(
            value = colorValue,
            onValueChange = { colorValue = it },
            title = "颜色选择"
        )

        PreferenceDate(
            value = dateValue,
            onValueChange = { dateValue = it },
            title = "日期选择"
        )

        PreferenceInputText(
            value = textValue,
            onValueChange = { textValue = it },
            title = "文本输入",
            validator = rememberDialogInputValidator { input ->
                if (input.length >= 3) {
                    DialogInputValidator.Result.Valid
                } else {
                    DialogInputValidator.Result.Error("至少输入3个字符")
                }
            }
        )
    }
}

依赖管理：
- Dependency.Enabled - 启用状态
- Dependency.Disabled - 禁用状态
- Dependency.State - 基于状态的条件依赖

*/

// ==================== Position Context ====================

enum class PreferenceItemPosition {
    Single,    // 只有一个项目
    First,     // 第一个项目
    Middle,    // 中间项目
    Last       // 最后一个项目
}

val LocalPreferenceItemPosition = compositionLocalOf { PreferenceItemPosition.Single }

@Composable
fun getPreferenceItemShape(position: PreferenceItemPosition = LocalPreferenceItemPosition.current): androidx.compose.ui.graphics.Shape {
    val cornerSize = 12.dp
    return when (position) {
        PreferenceItemPosition.Single -> RoundedCornerShape(cornerSize)
        PreferenceItemPosition.First -> RoundedCornerShape(
            topStart = cornerSize,
            topEnd = cornerSize,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )

        PreferenceItemPosition.Middle -> RoundedCornerShape(0.dp)
        PreferenceItemPosition.Last -> RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = cornerSize,
            bottomEnd = cornerSize
        )
    }
}

// ==================== Preference Screen ====================

@Composable
fun PreferenceScreen(
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    content: @Composable PreferenceScope.() -> Unit
) {
    val preferenceScope = remember { PreferenceScopeImpl() }

    Column(
        modifier = Modifier
            .then(
                if (scrollable) {
                    Modifier.verticalScroll(rememberScrollState())
                } else Modifier
            )
            .then(modifier)
    ) {
        preferenceScope.content()
    }
}

// ==================== Preference Section ====================

@Composable
fun PreferenceSection(
    title: String,
    subtitle: String? = null,
    enabled: Dependency = Dependency.Enabled,
    visible: Dependency = Dependency.Enabled,
    content: @Composable PreferenceScope.() -> Unit
) {
    val isEnabled by enabled.asState()
    val isVisible by visible.asState()

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = 300f
            )
        ) + fadeIn(
            animationSpec = tween(300)
        ),
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = 300f
            )
        ) + fadeOut(
            animationSpec = tween(300)
        )
    ) {
        val sectionAlphaValue by animateFloatAsState(
            targetValue = if (isEnabled) 1f else 0.38f,
            animationSpec = tween(200),
            label = "section_alpha_animation"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = 300f
                    )
                )
                .alpha(sectionAlphaValue)
        ) {
            // Section Header
            Spacer(
                modifier = Modifier.height(8.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                )
            }

            // 自动定位的内容
            Box(
                modifier = if (!isEnabled) {
                    Modifier.pointerInput(Unit) {
                        // 拦截所有点击事件，防止交互
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                            }
                        }
                    }
                } else Modifier
            ) {
                AutoPositionedPreferenceGroup {
                    val preferenceScope = remember { PreferenceScopeImpl() }
                    preferenceScope.content()
                }
            }
        }
    }
}

@Composable
fun AutoPositionedPreferenceGroup(
    content: @Composable () -> Unit
) {
    SubcomposeLayout { constraints ->
        // 第一次subcompose用来计算子项数量
        val slots = subcompose("measure") { content() }
        val totalCount = slots.size

        // 为每个子项重新subcompose，提供正确的位置信息
        val placeableList = (0 until totalCount).mapNotNull { index ->
            val position = when {
                totalCount == 1 -> PreferenceItemPosition.Single
                index == 0 -> PreferenceItemPosition.First
                index == totalCount - 1 -> PreferenceItemPosition.Last
                else -> PreferenceItemPosition.Middle
            }

            subcompose("item_$index") {
                CompositionLocalProvider(LocalPreferenceItemPosition provides position) {
                    content()
                }
            }.getOrElse(index) { null }?.measure(constraints)
        }

        // 布局
        val totalHeight = placeableList.sumOf { it.height }
        layout(constraints.maxWidth, totalHeight) {
            var yOffset = 0
            placeableList.forEach { placeable ->
                placeable.placeRelative(0, yOffset)
                yOffset += placeable.height
            }
        }
    }
}

// ==================== Preference Divider ====================

@Composable
fun PreferenceDivider() {
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )
    Spacer(modifier = Modifier.height(16.dp))
}

// ==================== Base Preference ====================

@Composable
fun BasePreference(
    title: String,
    subtitle: String? = null,
    enabled: Dependency = Dependency.Enabled,
    visible: Dependency = Dependency.Enabled,
    onClick: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val isEnabled by enabled.asState()
    val isVisible by visible.asState()

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = 400f
            )
        ) + fadeIn(
            animationSpec = tween(250)
        ),
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = 400f
            )
        ) + fadeOut(
            animationSpec = tween(250)
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 1.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = 400f
                    )
                ),
            shape = getPreferenceItemShape(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        ) {
            val alphaValue by animateFloatAsState(
                targetValue = if (isEnabled) 1f else 0.38f,
                animationSpec = tween(200),
                label = "alpha_animation"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isEnabled && onClick != null) { onClick?.invoke() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .alpha(alphaValue),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    leadingContent?.let {
                        it()
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    trailingContent?.let {
                        Spacer(modifier = Modifier.width(16.dp))
                        it()
                    }
                }
            }
        }
    }
}

// ==================== Support Classes ====================

interface PreferenceScope {
    // Marker interface for preference scope
}

class PreferenceScopeImpl : PreferenceScope

sealed class Dependency {
    object Enabled : Dependency()
    object Disabled : Dependency()
    class State<T>(
        private val state: androidx.compose.runtime.State<T>,
        private val condition: (T) -> Boolean
    ) : Dependency() {
        @Composable
        override fun asState(): androidx.compose.runtime.State<Boolean> {
            val stateValue by state
            return derivedStateOf { condition(stateValue) }
        }
    }

    @Composable
    open fun asState(): androidx.compose.runtime.State<Boolean> = when (this) {
        is Enabled -> remember { mutableStateOf(true) }
        is Disabled -> remember { mutableStateOf(false) }
        is State<*> -> this.asState()
    }
}

// ==================== Dialog Input Validator ====================

interface DialogInputValidator {
    sealed class Result {
        object Valid : Result()
        data class Error(val errorText: String) : Result()
    }

    fun validate(input: String): Result
}

@Composable
fun rememberDialogInputValidator(
    validate: (String) -> DialogInputValidator.Result
): DialogInputValidator {
    return remember {
        object : DialogInputValidator {
            override fun validate(input: String): DialogInputValidator.Result = validate(input)
        }
    }
} 