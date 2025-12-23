/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import timeflow.app.shared.generated.resources.Res
import timeflow.app.shared.generated.resources.cancel
import timeflow.app.shared.generated.resources.confirm
import timeflow.app.shared.generated.resources.dialog_title_time_period_picker
import xyz.hyli.timeflow.data.Time
import xyz.hyli.timeflow.ui.theme.NotoSans

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
                fontFamily = NotoSans
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = ":",
            fontFamily = NotoSans
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
                fontFamily = NotoSans
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "—",
            fontWeight = FontWeight.Bold,
            fontFamily = NotoSans
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
                fontFamily = NotoSans
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = ":",
            fontFamily = NotoSans
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
                fontFamily = NotoSans
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
            fontFamily = NotoSans
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
            fontFamily = NotoSans
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
            fontFamily = NotoSans
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
