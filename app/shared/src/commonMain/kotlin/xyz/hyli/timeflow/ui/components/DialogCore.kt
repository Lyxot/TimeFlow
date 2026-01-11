/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


/**
 * Properties that can be used to configure the behavior of a [Dialog].
 *
 * @param dismissOnBackPress Whether the dialog should be dismissed when the back button is pressed.
 * @param dismissOnClickOutside Whether the dialog should be dismissed when clicking outside the dialog panel.
 */
data class DialogProperties(val dismissOnBackPress: Boolean = true, val dismissOnClickOutside: Boolean = true)

@Stable
class DialogState(initiallyVisible: Boolean = false) {

    @Deprecated(
        "This will go away in 2.0. Use rememberDialogState(visible)",
        ReplaceWith("rememberDialogState(visible)")
    )
    constructor(visible: Boolean = false, ____deprecated_constructor: Unit) : this(initiallyVisible = visible)

    internal val panelVisibilityState = MutableTransitionState(initiallyVisible)
    internal val scrimVisibilityState = MutableTransitionState(initiallyVisible)

    var visible: Boolean
        set(value) {
            panelVisibilityState.targetState = value
            scrimVisibilityState.targetState = value
        }
        get() {
            return panelVisibilityState.currentState || scrimVisibilityState.currentState
        }
}

@Stable
class DialogScope internal constructor(state: DialogState) {
    internal var state by mutableStateOf(state)
}

private val DialogStateSaver = run {
    mapSaver(
        save = {
            mapOf("visible" to it.visible)
        },
        restore = {
            DialogState(initiallyVisible = it["visible"] as Boolean)
        }
    )
}

/**
 * Creates a [DialogState] that can be used to control the visibility of a [Dialog].
 *
 * @param initiallyVisible Whether the dialog should be initially visible.
 */
@Composable
fun rememberDialogState(initiallyVisible: Boolean = false): DialogState {
    return rememberSaveable(saver = DialogStateSaver) { DialogState(initiallyVisible) }
}

/**
 * A stackable, renderless, highly performant foundational component to build dialogs with.
 *
 * For interactive preview & code examples, visit [Dialog Documentation](https://composeunstyled.com/dialog).
 *
 * ## Basic Example
 *
 * ```kotlin
 * val dialogState = rememberDialogState()
 *
 * Box {
 *     Button(onClick = { dialogState.visible = true }) {
 *         Text("Show Dialog")
 *     }
 *     Dialog(state = dialogState) {
 *         DialogPanel(
 *             modifier = Modifier
 *                 .displayCutoutPadding()
 *                 .systemBarsPadding()
 *                 .widthIn(min = 280.dp, max = 560.dp)
 *                 .padding(20.dp)
 *                 .clip(RoundedCornerShape(12.dp))
 *                 .border(1.dp, Color(0xFFE4E4E4), RoundedCornerShape(12.dp))
 *                 .background(Color.White)
 *         ) {
 *             Column {
 *                 Text("Something important happened")
 *                 Button(onClick = { dialogState.visible = false }) {
 *                     Text("Got it")
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param state The [DialogState] that controls the visibility of the dialog.
 * @param properties The [DialogProperties] that configure the behavior of the dialog.
 * @param onDismiss Callback that is called when the dialog is dismissed.
 * @param content The content of the dialog, which should contain a [DialogPanel] and optionally a [Scrim].
 */
@Composable
fun Dialog(
    state: DialogState,
    properties: xyz.hyli.timeflow.ui.components.DialogProperties = xyz.hyli.timeflow.ui.components.DialogProperties(),
    onDismiss: () -> Unit = { },
    content: @Composable (DialogScope.() -> Unit)
) {
    val scope = remember { DialogScope(state) }

    val currentDismiss by rememberUpdatedState(onDismiss)

    val isPanelVisible = state.panelVisibilityState.isIdle.not() || state.panelVisibilityState.currentState
    val isScrimVisible = state.scrimVisibilityState.isIdle.not() || state.scrimVisibilityState.currentState
    if (isScrimVisible || isPanelVisible) {
        val onKeyEvent = if (properties.dismissOnBackPress) {
            { event: KeyEvent ->
                if (event.type == KeyEventType.KeyDown && (event.key == Key.Back || event.key == Key.Escape)) {
                    currentDismiss()
                    state.visible = false
                    true
                } else false
            }
        } else {
            { false }
        }
        Modal(onKeyEvent = onKeyEvent) {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .then(
                        if (properties.dismissOnClickOutside) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures {
                                    currentDismiss()
                                    state.visible = false
                                }
                            }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                scope.content()
            }
        }
    }
}

/**
 * A container component that renders the dialog's panel and its contents.
 *
 * @param modifier Modifier to be applied to the dialog panel.
 * @param enter The enter transition for the dialog panel.
 * @param exit The exit transition for the dialog panel.
 * @param content The content of the dialog panel.
 */
@Composable
fun DialogScope.DialogPanel(
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn(animationSpec = tween(durationMillis = 0)),
    exit: ExitTransition = fadeOut(animationSpec = tween(durationMillis = 0)),
    shape: Shape = RectangleShape,
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = LocalContentColor.current,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visibleState = state.panelVisibilityState,
        enter = enter,
        exit = exit,
    ) {
        Box(
            modifier
                .clip(shape)
                .background(backgroundColor)
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(contentPadding)
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}

/**
 * A component that renders a scrim behind the dialog panel.
 *
 * @param modifier Modifier to be applied to the scrim.
 * @param scrimColor The color of the scrim.
 * @param enter The enter transition for the scrim.
 * @param exit The exit transition for the scrim.
 */
@Composable
fun DialogScope.Scrim(
    modifier: Modifier = Modifier,
    scrimColor: Color = Color.Black.copy(0.6f),
    enter: EnterTransition = fadeIn(animationSpec = tween(durationMillis = 0)),
    exit: ExitTransition = fadeOut(animationSpec = tween(durationMillis = 0)),
) {
    AnimatedVisibility(
        visibleState = state.scrimVisibilityState,
        enter = enter,
        exit = exit
    ) {
        Box(Modifier.fillMaxSize().focusable(false).background(scrimColor).then(modifier))
    }
}
