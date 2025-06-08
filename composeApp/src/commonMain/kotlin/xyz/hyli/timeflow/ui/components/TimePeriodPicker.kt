package xyz.hyli.timeflow.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.michaelflisar.composedialogs.core.Dialog
import com.michaelflisar.composedialogs.core.DialogButton
import com.michaelflisar.composedialogs.core.DialogButtonType
import com.michaelflisar.composedialogs.core.DialogDefaults
import com.michaelflisar.composedialogs.core.DialogState
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.cancel
import timeflow.composeapp.generated.resources.confirm
import timeflow.composeapp.generated.resources.dialog_title_time_period_picker
import xyz.hyli.timeflow.datastore.Time

@Composable
fun TimePeriodPickerDialog(
    state: DialogState,
    initStartTime: Time,
    initEndTime: Time,
    onTimePeriodChange: (startTime: Time, endTime: Time) -> Unit
) {
    val startTime = remember { mutableStateOf(initStartTime) }
    val endTime = remember { mutableStateOf(initEndTime) }
    Dialog(
        state = state,
        title = { Text(stringResource(Res.string.dialog_title_time_period_picker)) },
        buttons = DialogDefaults.buttons(
            positive = DialogButton(stringResource(Res.string.confirm)),
            negative = DialogButton(stringResource(Res.string.cancel)),
        ),
        onEvent = { event ->
            if (event.isPositiveButton) {
                onTimePeriodChange(startTime.value, endTime.value)
            }
        }
    ) {
        LaunchedEffect(TimePeriodValidator(startTime.value, endTime.value)) {
            state.enableButton(DialogButtonType.Positive, TimePeriodValidator(startTime.value, endTime.value))
        }
        TimePeriodPickerContent(
            initStartTime = initStartTime,
            initEndTime = initEndTime,
            onStartTimeChange = { startTime.value = it },
            onEndTimeChange = { endTime.value = it }
        )
    }
}

@Composable
private fun TimePeriodPickerContent(
    initStartTime: Time,
    initEndTime: Time,
    onStartTimeChange: (startTime: Time) -> Unit,
    onEndTimeChange: (endTime: Time) -> Unit,
) {
    val startTime = remember { mutableStateOf(initStartTime) }
    val endTime = remember { mutableStateOf(initEndTime) }
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(128.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            WheelPicker(
                data = (0..23).map { it.toString().padStart(2, '0') },
                selectIndex = initStartTime.hour,
                visibleCount = 3,
                onSelect = { index, _ ->
                    startTime.value = startTime.value.copy(hour = index)
                    onStartTimeChange(startTime.value)
                }
            ) { hour ->
                Text(
                    text = hour,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = ":",
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.weight(1f))
            WheelPicker(
                data = (0..59).map { it.toString().padStart(2, '0') },
                selectIndex = initStartTime.minute,
                visibleCount = 3,
                onSelect = { index, _ ->
                    startTime.value = startTime.value.copy(minute = index)
                    onStartTimeChange(startTime.value)
                }
            ) { minute ->
                Text(
                    text = minute,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "—",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.weight(1f))
            WheelPicker(
                data = (0..23).map { it.toString().padStart(2, '0') },
                selectIndex = initEndTime.hour,
                visibleCount = 3,
                onSelect = { index, _ ->
                    endTime.value = endTime.value.copy(hour = index)
                    onEndTimeChange(endTime.value)
                }
            ) { hour ->
                Text(
                    text = hour,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = ":",
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.weight(1f))
            WheelPicker(
                data = (0..59).map { it.toString().padStart(2, '0') },
                selectIndex = initEndTime.minute,
                visibleCount = 3,
                onSelect = { index, _ ->
                    endTime.value = endTime.value.copy(minute = index)
                    onEndTimeChange(endTime.value)
                }
            ) { minute ->
                Text(
                    text = minute,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

private fun TimePeriodValidator(
    startTime: Time,
    endTime: Time
): Boolean {
    return startTime < endTime
}