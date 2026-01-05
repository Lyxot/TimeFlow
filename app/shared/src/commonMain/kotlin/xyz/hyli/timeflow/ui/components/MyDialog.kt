/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.hyli.timeflow.ui.components.DialogDefaults.buttonsDisabled
import kotlin.math.max

// ------------------
// 主要 Dialog 函数
// ------------------

/**
 * Shows a dialog containing some arbitrary [content]
 *
 * @param state the [MyDialogState] of the dialog
 * @param title the optional title of the dialog
 * @param icon the optional icon of the dialog
 * @param buttons the [DialogButtons] of the dialog - use [DialogDefaults.buttons] here [DialogDefaults.buttonsDisabled]
 * @param options the [DialogOptions] of the dialog - use [DialogDefaults.options] here
 * @param onEvent the callback for all [DialogEvent] - this can be a button click [DialogEvent.Button] or the dismiss information [DialogEvent.Dismissed]
 * @param content the content of this dialog
 */
@Composable
fun MyDialog(
    state: MyDialogState,
    title: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    buttons: DialogButtons = DialogDefaults.buttons(),
    options: DialogOptions = DialogDefaults.options(),
    onEvent: (event: DialogEvent) -> Unit = {},
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val dialogState = rememberDialogState(initiallyVisible = true)

    // 默认样式参数
    val swipeDismissable = false
    val dismissOnBackPress = true
    val dismissOnClickOutside = true
    val scrim = true
    val styleOptions = StyleOptions()
    val shape = DialogStyleDefaults.shape
    val containerColor = DialogStyleDefaults.containerColor
    val iconColor = DialogStyleDefaults.iconColor
    val titleColor = DialogStyleDefaults.titleColor
    val contentColor = DialogStyleDefaults.contentColor

    // 间距配置
    val spacing = DialogSpacing(24.dp, 24.dp)

    val animDurationEnter = 250
    val animDurationExit = 150
    val animEnter =
        scaleIn(initialScale = 0.8f) + fadeIn(tween(durationMillis = animDurationEnter))
    val animExit =
        scaleOut(targetScale = 0.6f) + fadeOut(tween(durationMillis = animDurationExit))

    val dismiss = { dialogState.visible = false }
    var buttonPressed = false
    val waitForDismissAnimationAndUpdateState = {
        coroutineScope.launch {
            delay(animDurationExit.toLong())
            if (buttonPressed)
                state.dismiss()
            else
                state.dismiss(onEvent)
        }
    }

    val shouldDismissOnBackPress by remember {
        derivedStateOf { dismissOnBackPress && state.interactionSource.dismissAllowed.value }
    }
    val shouldDismissOnClickOutside by remember {
        derivedStateOf { dismissOnClickOutside && state.interactionSource.dismissAllowed.value }
    }

    Dialog(
        state = dialogState,
        properties = DialogProperties(
            dismissOnBackPress = shouldDismissOnBackPress,
            dismissOnClickOutside = shouldDismissOnClickOutside
        ),
        onDismiss = {
            waitForDismissAnimationAndUpdateState()
        }
    ) {

        if (scrim) {
            Scrim(
                enter = fadeIn(),
                exit = fadeOut(),
                scrimColor = MaterialTheme.colorScheme.scrim.copy(0.3f),
                modifier = Modifier.fillMaxSize()
            )
        }

        DialogPanel(
            modifier = Modifier
                .systemBarsPadding()
                .imePadding()
                .padding(16.dp)
                .shadow(6.dp, shape)
                .clip(shape)
                .background(containerColor)
                .padding(24.dp),
            enter = animEnter,
            exit = animExit
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 560.dp)
            ) {

                // Icon + Title
                ComposeDialogTitle(
                    modifier = Modifier,
                    title = title,
                    icon = icon,
                    iconColor = iconColor,
                    titleColor = titleColor,
                    options = styleOptions
                )

                // Content
                ComposeDialogContent(
                    content = content,
                    contentColor = contentColor,
                    modifier = Modifier.weight(weight = 1f, fill = false),
                    bottomPadding = spacing.contentPadding(buttons)
                )

                // Buttons
                ComposeDialogButtons(
                    buttons = buttons,
                    state = state,
                    options = options,
                    dismissOnButtonPressed = {
                        buttonPressed = true
                        dismiss()
                        waitForDismissAnimationAndUpdateState()
                    },
                    onEvent = onEvent
                )
            }
        }
    }
}

// ------------------
// DialogDefaults 对象
// ------------------

object DialogDefaults {

    /**
     * the setup of the dialog buttons
     *
     * use [buttonsDisabled] if you do not want to show any buttons
     * use [DialogButton.DISABLED] as button if you do not want to show the respective button
     *
     * Info: Buttons with empty text will be disabled!
     *
     * @param positive positive [DialogButton]
     * @param negative negative [DialogButton]
     */
    @Composable
    fun buttons(
        positive: DialogButton = DialogButton("OK"),
        negative: DialogButton = DialogButton("")
    ) = DialogButtons(
        positive,
        negative
    )

    /**
     * the setup of the dialog buttons if you do not want to show any buttons
     */
    @Composable
    fun buttonsDisabled() = DialogButtons.DISABLED

    /**
     * some additional options for the dialog
     *
     * @param dismissOnButtonClick if true, the dialog will be dismissed on button click
     */
    @Composable
    fun options(
        dismissOnButtonClick: Boolean = true
    ) = DialogOptions(
        dismissOnButtonClick
    )
}

// ------------------
// 样式默认值
// ------------------

object DialogStyleDefaults {

    val shape: Shape
        @Composable get() = RoundedCornerShape(size = 28.dp)

    val containerColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh

    val iconColor
        @Composable get() = MaterialTheme.colorScheme.secondary

    val titleColor
        @Composable get() = MaterialTheme.colorScheme.onSurface

    val contentColor
        @Composable get() = MaterialTheme.colorScheme.onSurface
}

// ------------------
// 事件和状态类
// ------------------

/**
 * the sealed event for all dialog events (button click and dismissal)
 *
 * [DialogEvent.dismissed] the information if this event will dismiss the dialog or not
 * [DialogEvent.isPositiveButton] a convenient value to determine, if this is the event of the positive dialog button
 */
sealed class DialogEvent {

    abstract val dismissed: Boolean
    abstract val isPositiveButton: Boolean // most interesting attribute so we make it easily accessible

    /**
     * the event of a dialog button click
     *
     * @param button the [DialogButtonType] of the button that was clicked
     * @param dismissed the information if this button will dismiss the dialog or not
     */
    class Button(
        val button: DialogButtonType,
        override val dismissed: Boolean
    ) : DialogEvent() {
        override val isPositiveButton = button == DialogButtonType.Positive
    }

    /**
     * the event of a dialog dismissal
     */
    object Dismissed : DialogEvent() {
        override val dismissed = true
        override val isPositiveButton = false
        override fun toString() = "DialogEvent::Dismissed"
    }
}

/**
 * an enum for the different dialog button types
 */
enum class DialogButtonType {
    Positive,
    Negative
}

/**
 * an interaction source for button states and option state
 *
 * @param buttonPositiveEnabled the positive button is only enabled if this state is true
 * @param buttonNegativeEnabled the negative button is only enabled if this state is true
 * @param dismissAllowed the dialog can only be dismissed if this state is true
 * @param swipeAllowed the dialog can only be swiped away if this state is true
 */
class DialogInteractionSource internal constructor(
    val buttonPositiveEnabled: MutableState<Boolean>,
    val buttonNegativeEnabled: MutableState<Boolean>,
    val dismissAllowed: MutableState<Boolean>,
    val swipeAllowed: MutableState<Boolean>
)

/**
 * a dialog state holding the current showing state and the some additional state [DialogInteractionSource] of the dialog
 */
abstract class MyDialogState {

    /**
     * the showing state of the dialog
     */
    abstract val visible: Boolean

    /**
     * the [DialogInteractionSource] holding other states for this dialog
     */
    abstract val interactionSource: DialogInteractionSource

    abstract fun onDismiss()

    /**
     * this will dismiss this dialog (if the [interactionSource] ([DialogInteractionSource.dismissAllowed]) does allow this)
     *
     * @return true, if the dismiss was successful
     */
    fun dismiss(): Boolean {
        if (interactionSource.dismissAllowed.value) {
            onDismiss()
        }
        return !visible
    }

    internal fun dismiss(
        onEvent: (event: DialogEvent) -> Unit
    ): Boolean {
        if (dismiss())
            onEvent(DialogEvent.Dismissed)
        return !visible
    }

    internal fun onButtonPressed(
        onEvent: (event: DialogEvent) -> Unit,
        button: DialogButtonType,
        dismiss: Boolean
    ) {
        onEvent(DialogEvent.Button(button, dismiss))
    }

    /**
     * this will determine if a button currently can be pressed (depends on the [interactionSource] ([DialogInteractionSource.buttonPositiveEnabled] or [DialogInteractionSource.buttonNegativeEnabled]))
     */
    fun isButtonEnabled(button: DialogButtonType): Boolean {
        return when (button) {
            DialogButtonType.Positive -> interactionSource.buttonPositiveEnabled.value
            DialogButtonType.Negative -> interactionSource.buttonNegativeEnabled.value
        }
    }

    /**
     * this will enable or disable a button
     *
     * @param button the [DialogButtonType] that should be enabled/disabled
     * @param enabled if true, the button will be enabled
     */
    fun enableButton(
        button: DialogButtonType,
        enabled: Boolean
    ) {
        when (button) {
            DialogButtonType.Positive -> interactionSource.buttonPositiveEnabled.value = enabled
            DialogButtonType.Negative -> interactionSource.buttonNegativeEnabled.value = enabled
        }
    }

    /**
     * this will make the dialog dismissable or not
     *
     * @param enabled if true, the dialog can be dismissed
     */
    fun dismissable(enabled: Boolean) {
        interactionSource.dismissAllowed.value = enabled
    }
}

/**
 * a dialog state holding the current showing state and the some additional state [DialogInteractionSource] of the dialog
 *
 * @param state the visibility state of the dialog
 * @param interactionSource the [DialogInteractionSource] holding other states for this dialog
 */
class MyDialogStateNoData(
    private val state: MutableState<Boolean>,
    interactionSource: MutableState<DialogInteractionSource>
) : MyDialogState() {

    override val visible by state
    override val interactionSource by interactionSource

    override fun onDismiss() {
        state.value = false
    }

    /**
     * this will show this dialog
     */
    fun show() {
        state.value = true
    }

}

/**
 * a dialog state holding the current state and the some additional state [DialogInteractionSource] of the dialog
 *
 * @param visible the visibility state of the dialog - must be derived from the state!
 * @param state the state of the dialog - if not null, the dialog is visible, otherwise not
 * @param interactionSource the [DialogInteractionSource] holding other states for this dialog
 */
class MyDialogStateWithData<T>(
    visible: State<Boolean>,
    private val state: MutableState<T?>,
    interactionSource: MutableState<DialogInteractionSource>,
) : MyDialogState() {

    override val visible by visible
    override val interactionSource by interactionSource

    override fun onDismiss() {
        state.value = null
    }

    /**
     * this will show this dialog
     */
    fun show(data: T) {
        state.value = data
    }

    /**
     * this will return the currently holded data
     */
    val data: T?
        get() = state.value

    /**
     * this will return the currently holded data
     *
     * should only be called if the dialog is shown because of a forcefully cast to a non null value!
     */
    fun requireData() = state.value!!

}

/**
 * a dialog buttons
 *
 * @param text the text of the button
 */
class DialogButton(
    val text: String = ""
) {
    companion object {
        val DISABLED = DialogButton("")
    }

    val enabled: Boolean
        get() = text.isNotEmpty()
}

/**
 * see [DialogDefaults.buttons] and [DialogDefaults.buttonsDisabled]
 *
 */
class DialogButtons internal constructor(
    val positive: DialogButton,
    val negative: DialogButton
) {
    companion object {
        val DISABLED = DialogButtons(DialogButton.DISABLED, DialogButton.DISABLED)
    }

    val enabled = positive.enabled || negative.enabled
}

class StyleOptions(
    val iconMode: IconMode = IconMode.CenterTop
) {
    enum class IconMode {
        CenterTop,
        Begin
    }

    fun copy(
        iconMode: IconMode = this.iconMode
    ) = StyleOptions(iconMode)
}

class DialogOptions internal constructor(
    val dismissOnButtonClick: Boolean
)

class DialogSpacing internal constructor(
    val spacingContentToButtons: Dp,
    val spacingContentToBottom: Dp
) {
    fun contentPadding(buttons: DialogButtons) =
        if (buttons.enabled) spacingContentToButtons else spacingContentToBottom
}

// ------------------
// 内部组件
// ------------------

@Composable
internal fun ColumnScope.ComposeDialogTitle(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)?,
    icon: @Composable (() -> Unit)?,
    iconColor: Color,
    titleColor: Color,
    options: StyleOptions,
) {
    if (icon != null) {
        if (options.iconMode == StyleOptions.IconMode.CenterTop) {
            Column(
                modifier = modifier.align(Alignment.CenterHorizontally),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TitleIcon(
                    icon = icon,
                    iconColor = iconColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                TitleTitle(
                    title = title,
                    titleColor = titleColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        } else {
            Row(
                modifier = modifier.padding(bottom = 16.dp)
            ) {
                TitleIcon(
                    icon = icon,
                    iconColor = iconColor,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically)
                )
                Column(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    TitleTitle(
                        title = title,
                        titleColor = titleColor,
                        modifier = Modifier
                    )
                }
            }
        }
    } else {
        TitleTitle(
            title = title,
            titleColor = titleColor,
            modifier = modifier
                .padding(bottom = 16.dp)
                .align(Alignment.Start)

        )
    }
}

@Composable
internal fun TitleIcon(
    icon: @Composable (() -> Unit)?,
    iconColor: Color,
    modifier: Modifier,
) {
    if (icon != null) {
        CompositionLocalProvider(LocalContentColor provides iconColor) {
            Box(modifier) {
                icon()
            }
        }
    }
}

@Composable
internal fun TitleTitle(
    title: @Composable (() -> Unit)?,
    titleColor: Color,
    modifier: Modifier,
) {
    if (title != null) {
        CompositionLocalProvider(LocalContentColor provides titleColor) {
            val textStyle = MaterialTheme.typography.headlineSmall
            ProvideTextStyle(textStyle) {
                Box(modifier) {
                    title()
                }
            }
        }
    }
}

@Composable
internal fun ColumnScope.ComposeDialogContent(
    content: @Composable () -> Unit,
    contentColor: Color,
    modifier: Modifier,
    bottomPadding: Dp
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        val textStyle = MaterialTheme.typography.bodyMedium
        ProvideTextStyle(textStyle) {
            Box(
                modifier
                    .padding(bottom = bottomPadding)
                    .align(Alignment.Start),
                contentAlignment = Alignment.TopStart
            ) {
                content()
            }
        }
    }
}

@Composable
internal fun ColumnScope.ComposeDialogButtons(
    modifier: Modifier = Modifier,
    buttons: DialogButtons,
    state: MyDialogState,
    options: DialogOptions,
    dismissOnButtonPressed: () -> Unit,
    onEvent: (event: DialogEvent) -> Unit
) {
    if (!buttons.enabled) {
        return
    }
    Box(
        modifier = modifier.align(Alignment.End),
        contentAlignment = Alignment.BottomEnd
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            val textStyle = MaterialTheme.typography.labelLarge
            ProvideTextStyle(
                value = textStyle,
                content = {
                    AlertDialogFlowRow(
                        mainAxisSpacing = ButtonsMainAxisSpacing,
                        crossAxisSpacing = ButtonsCrossAxisSpacing
                    ) {
                        ComposeDialogButton(
                            button = buttons.negative,
                            buttonType = DialogButtonType.Negative,
                            state = state,
                            options = options,
                            dismissOnButtonPressed = dismissOnButtonPressed,
                            onEvent = onEvent
                        )
                        ComposeDialogButton(
                            button = buttons.positive,
                            buttonType = DialogButtonType.Positive,
                            state = state,
                            options = options,
                            dismissOnButtonPressed = dismissOnButtonPressed,
                            onEvent = onEvent
                        )
                    }
                }
            )
        }
    }
}

@Composable
internal fun ComposeDialogButton(
    button: DialogButton,
    buttonType: DialogButtonType,
    state: MyDialogState,
    options: DialogOptions,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    dismissOnButtonPressed: () -> Unit,
    onEvent: (event: DialogEvent) -> Unit
) {
    if (button.text.isNotEmpty()) {
        val enabled = state.isButtonEnabled(buttonType)
        TextButton(
            enabled = enabled,
            colors = colors,
            onClick = {
                val dismiss =
                    options.dismissOnButtonClick && state.interactionSource.dismissAllowed.value
                state.onButtonPressed(onEvent, buttonType, dismiss)
                if (dismiss) {
                    dismissOnButtonPressed()
                }
            }
        ) {
            Text(button.text)
        }
    }
}

// ------------------
// AlertDialogFlowRow 组件 (复制自原项目)
// ------------------

internal val ButtonsMainAxisSpacing = 8.dp
internal val ButtonsCrossAxisSpacing = 12.dp

@Composable
internal fun AlertDialogFlowRow(
    mainAxisSpacing: Dp,
    crossAxisSpacing: Dp,
    content: @Composable () -> Unit
) {
    Layout(content) { measurables, constraints ->
        val sequences = mutableListOf<List<Placeable>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        var mainAxisSpace = 0
        var crossAxisSpace = 0

        val currentSequence = mutableListOf<Placeable>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        // Return whether the placeable can be added to the current sequence.
        fun canAddToCurrentSequence(placeable: Placeable) =
            currentSequence.isEmpty() || currentMainAxisSize + mainAxisSpacing.roundToPx() +
                    placeable.width <= constraints.maxWidth

        // Store current sequence information and start a new sequence.
        fun startNewSequence() {
            if (sequences.isNotEmpty()) {
                crossAxisSpace += crossAxisSpacing.roundToPx()
            }
            sequences += currentSequence.toList()
            crossAxisSizes += currentCrossAxisSize
            crossAxisPositions += crossAxisSpace

            crossAxisSpace += currentCrossAxisSize
            mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)

            currentSequence.clear()
            currentMainAxisSize = 0
            currentCrossAxisSize = 0
        }

        for (measurable in measurables) {
            // Ask the child for its preferred size.
            val placeable = measurable.measure(constraints)

            // Start a new sequence if there is not enough space.
            if (!canAddToCurrentSequence(placeable)) startNewSequence()

            // Add the child to the current sequence.
            if (currentSequence.isNotEmpty()) {
                currentMainAxisSize += mainAxisSpacing.roundToPx()
            }
            currentSequence.add(placeable)
            currentMainAxisSize += placeable.width
            currentCrossAxisSize = max(currentCrossAxisSize, placeable.height)
        }

        if (currentSequence.isNotEmpty()) startNewSequence()

        val mainAxisLayoutSize = max(mainAxisSpace, constraints.minWidth)

        val crossAxisLayoutSize = max(crossAxisSpace, constraints.minHeight)

        layout(mainAxisLayoutSize, crossAxisLayoutSize) {
            sequences.forEachIndexed { i, placeables ->
                val childrenMainAxisSizes = IntArray(placeables.size) { j ->
                    placeables[j].width +
                            if (j < placeables.lastIndex) mainAxisSpacing.roundToPx() else 0
                }
                val arrangement = Arrangement.Bottom
                // TODO(soboleva): rtl support
                // Handle vertical direction
                val mainAxisPositions = IntArray(childrenMainAxisSizes.size)
                with(arrangement) {
                    arrange(mainAxisLayoutSize, childrenMainAxisSizes, mainAxisPositions)
                }
                placeables.forEachIndexed { j, placeable ->
                    placeable.place(
                        x = mainAxisPositions[j],
                        y = crossAxisPositions[i]
                    )
                }
            }
        }
    }
}

// ------------------
// 辅助函数 - 创建状态
// ------------------

/**
 * 创建一个无数据的对话框状态
 */
@Composable
fun rememberDialogState(): MyDialogStateNoData {
    val visible = remember { mutableStateOf(false) }
    val interactionSource = remember {
        mutableStateOf(
            DialogInteractionSource(
                buttonPositiveEnabled = mutableStateOf(true),
                buttonNegativeEnabled = mutableStateOf(true),
                dismissAllowed = mutableStateOf(true),
                swipeAllowed = mutableStateOf(true)
            )
        )
    }
    return remember { MyDialogStateNoData(visible, interactionSource) }
}

/**
 * 创建一个带数据的对话框状态
 */
@Composable
fun <T> rememberDialogState(): MyDialogStateWithData<T> {
    val state = remember { mutableStateOf<T?>(null) }
    val visible = remember { derivedStateOf { state.value != null } }
    val interactionSource = remember {
        mutableStateOf(
            DialogInteractionSource(
                buttonPositiveEnabled = mutableStateOf(true),
                buttonNegativeEnabled = mutableStateOf(true),
                dismissAllowed = mutableStateOf(true),
                swipeAllowed = mutableStateOf(true)
            )
        )
    }
    return remember { MyDialogStateWithData(visible, state, interactionSource) }
}

// ------------------
// 使用示例
// ------------------

/*
使用方式:

@Composable
fun MyScreen() {
    val dialogState = rememberDialogState()

    // 显示对话框
    Button(onClick = { dialogState.show() }) {
        Text("显示对话框")
    }

    // 对话框实现
    if (dialogState.visible) {
        MyDialog(
            state = dialogState,
            title = { Text("标题") },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            buttons = DialogDefaults.buttons(
                positive = DialogButton("确定"),
                negative = DialogButton("取消")
            ),
            onEvent = { event ->
                when (event) {
                    is DialogEvent.Button -> {
                        if (event.isPositiveButton) {
                            // 处理确定按钮
                        } else {
                            // 处理取消按钮
                        }
                    }
                    is DialogEvent.Dismissed -> {
                        // 处理对话框关闭
                    }
                }
            }
        ) {
            Text("这里是对话框内容")
        }
    }
}
*/
