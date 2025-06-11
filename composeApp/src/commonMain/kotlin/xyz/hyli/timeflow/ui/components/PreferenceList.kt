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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.preference_dialog_close

// ==================== Preference List ====================

object PreferenceListStyle {
    enum class Style { Dialog, SegmentedButtons }
}

@Composable
fun <T> PreferenceList(
    value: T,
    onValueChange: (T) -> Unit,
    items: List<T>,
    itemTextProvider: @Composable (T) -> String,
    title: String,
    subtitle: String? = null,
    style: PreferenceListStyle.Style = PreferenceListStyle.Style.Dialog,
    enabled: Dependency = Dependency.Enabled,
    visible: Dependency = Dependency.Enabled
) {
    val isEnabled by enabled.asState()
    val isVisible by visible.asState()

    when (style) {
        PreferenceListStyle.Style.SegmentedButtons -> {
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
                        .padding(horizontal = 8.dp, vertical = 2.dp)
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
                    val listAlphaValue by animateFloatAsState(
                        targetValue = if (isEnabled) 1f else 0.38f,
                        animationSpec = tween(200),
                        label = "list_alpha_animation"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .alpha(listAlphaValue)
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
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items.forEachIndexed { index, item ->
                            SegmentedButton(
                                selected = value == item,
                                onClick = { if (isEnabled) onValueChange(item) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = items.size
                                ),
                                enabled = isEnabled
                            ) {
                                Text(itemTextProvider(item))
                            }
                        }
                    }
                }
            }
        }

        PreferenceListStyle.Style.Dialog -> {
            val dialogState = rememberDialogState()

            BasePreference(
                title = title,
                subtitle = subtitle,
                enabled = enabled,
                visible = visible,
                onClick = if (isEnabled) {
                    { dialogState.show() }
                } else null,
                trailingContent = {
                    Text(
                        text = itemTextProvider(value),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            )

            if (dialogState.visible) {
                MyDialog(
                    state = dialogState,
                    title = { Text(title) },
                    buttons = DialogDefaults.buttons(
                        positive = DialogButton(stringResource(Res.string.preference_dialog_close)),
                        negative = DialogButton("")
                    ),
                    onEvent = { event ->
                        when (event) {
                            is DialogEvent.Button, DialogEvent.Dismissed -> {
                                // 对话框关闭，不需要特殊处理
                            }
                        }
                    }
                ) {
                    LazyColumn {
                        items(items) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onValueChange(item)
                                        dialogState.dismiss()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = value == item,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = itemTextProvider(item),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 