package xyz.hyli.timeflow.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.cancel
import timeflow.composeapp.generated.resources.confirm
import timeflow.composeapp.generated.resources.dialog_title_time_period_picker
import xyz.hyli.timeflow.datastore.Time

enum class TimePeriodPickerStyle {
    Wheel,
    TextField
}

@Composable
fun TimePeriodPickerDialog(
    style: TimePeriodPickerStyle,
    state: DialogState,
    initStartTime: Time,
    initEndTime: Time,
    onTimePeriodChange: (startTime: Time, endTime: Time) -> Unit
) {
    val startTime = remember { mutableStateOf(initStartTime) }
    val endTime = remember { mutableStateOf(initEndTime) }
    MyDialog(
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
        LaunchedEffect(startTime.value, endTime.value) {
            state.enableButton(DialogButtonType.Positive, startTime.value < endTime.value)
        }
        when (style) {
            TimePeriodPickerStyle.Wheel -> {
                TimePeriodPickerWheel(
                    initStartTime = initStartTime,
                    initEndTime = initEndTime,
                    onStartTimeChange = { startTime.value = it },
                    onEndTimeChange = { endTime.value = it }
                )
            }
            TimePeriodPickerStyle.TextField -> {
                TimePeriodPickerTextField(
                    initStartTime = initStartTime,
                    initEndTime = initEndTime,
                    onStartTimeChange = { startTime.value = it },
                    onEndTimeChange = { endTime.value = it }
                )
            }
        }
    }
}

@Composable
private fun TimePeriodPickerWheel(
    initStartTime: Time,
    initEndTime: Time,
    onStartTimeChange: (startTime: Time) -> Unit,
    onEndTimeChange: (endTime: Time) -> Unit,
) {
    val startTime = remember { mutableStateOf(initStartTime) }
    val endTime = remember { mutableStateOf(initEndTime) }

    Row(
        modifier = Modifier.height(128.dp),
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

@Composable
private fun TimePeriodPickerTextField(
    initStartTime: Time,
    initEndTime: Time,
    onStartTimeChange: (startTime: Time) -> Unit,
    onEndTimeChange: (endTime: Time) -> Unit
) {
    var startTime by remember { mutableStateOf(initStartTime) }
    var endTime by remember { mutableStateOf(initEndTime) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        IntTextField(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(80.dp),
            value = startTime.hour,
            range = 0..23,
            onValueChange = { hour ->
                startTime = startTime.copy(hour = hour)
                onStartTimeChange(startTime)
            }
        )
        Text(
            text = ":",
            fontFamily = FontFamily.Monospace
        )
        IntTextField(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(80.dp),
            value = startTime.minute,
            range = 0..59,
            onValueChange = { minute ->
                startTime = startTime.copy(minute = minute)
                onStartTimeChange(startTime)
            }
        )

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "—",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.weight(1f))

        IntTextField(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(80.dp),
            value = endTime.hour,
            range = 0..23,
            onValueChange = { hour ->
                endTime = endTime.copy(hour = hour)
                onEndTimeChange(endTime)
            }
        )
        Text(
            text = ":",
            fontFamily = FontFamily.Monospace
        )
        IntTextField(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(80.dp),
            value = endTime.minute,
            range = 0..59,
            onValueChange = { minute ->
                endTime = endTime.copy(minute = minute)
                onEndTimeChange(endTime)
            }
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}
