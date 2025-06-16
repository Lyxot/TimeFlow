package xyz.hyli.timeflow.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.cancel
import timeflow.composeapp.generated.resources.confirm
import timeflow.composeapp.generated.resources.dialog_course_time_picker_section
import timeflow.composeapp.generated.resources.dialog_title_course_time_picker

enum class CourseTimePickerStyle {
    Wheel,
    TextField
}

@Composable
fun CourseTimePickerDialog(
    style: CourseTimePickerStyle,
    state: DialogState,
    initStartTime: Int,
    initEndTime: Int,
    totalLessonsCount: Int,
    onCourseTimeChange: (startTime: Int, endTime: Int) -> Unit
) {
    var startTime by remember { mutableStateOf(initStartTime) }
    var endTime by remember { mutableStateOf(initEndTime) }
    MyDialog(
        state = state,
        title = { Text(stringResource(Res.string.dialog_title_course_time_picker)) },
        buttons = DialogDefaults.buttons(
            positive = DialogButton(stringResource(Res.string.confirm)),
            negative = DialogButton(stringResource(Res.string.cancel)),
        ),
        onEvent = { event ->
            if (event.isPositiveButton) {
                onCourseTimeChange(startTime, endTime)
            }
        }
    ) {
        LaunchedEffect(startTime, endTime) {
            state.enableButton(DialogButtonType.Positive, startTime <= endTime)
        }
        when (style) {
            CourseTimePickerStyle.Wheel -> {
                CourseTimePickerWheel(
                    initStartTime = initStartTime,
                    initEndTime = initEndTime,
                    onStartTimeChange = { startTime = it },
                    onEndTimeChange = { endTime = it },
                    totalLessonsCount = totalLessonsCount
                )
            }

            CourseTimePickerStyle.TextField -> {
                CourseTimePickerTextField(
                    initStartTime = initStartTime,
                    initEndTime = initEndTime,
                    onStartTimeChange = { startTime = it },
                    onEndTimeChange = { endTime = it },
                    totalLessonsCount = totalLessonsCount
                )
            }
        }
    }
}

@Composable
private fun CourseTimePickerWheel(
    initStartTime: Int,
    initEndTime: Int,
    onStartTimeChange: (startTime: Int) -> Unit,
    onEndTimeChange: (endTime: Int) -> Unit,
    totalLessonsCount: Int
) {
    var startTime by remember { mutableStateOf(initStartTime) }
    var endTime by remember { mutableStateOf(initEndTime) }

    Row(
        modifier = Modifier.height(128.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        WheelPicker(
            data = (1..totalLessonsCount).toList(),
            selectIndex = initStartTime,
            visibleCount = 3,
            onSelect = { _, time ->
                startTime = time
                onStartTimeChange(startTime)
            }
        ) {
            Text(
                modifier = Modifier.width(32.dp),
                text = it.toString(),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = stringResource(Res.string.dialog_course_time_picker_section),
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "—",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.weight(1f))
        WheelPicker(
            data = (1..totalLessonsCount).toList(),
            selectIndex = initEndTime,
            visibleCount = 3,
            onSelect = { _, time ->
                endTime = time
                onEndTimeChange(endTime)
            }
        ) {
            Text(
                modifier = Modifier.width(32.dp),
                text = it.toString(),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = stringResource(Res.string.dialog_course_time_picker_section),
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CourseTimePickerTextField(
    initStartTime: Int,
    initEndTime: Int,
    onStartTimeChange: (startTime: Int) -> Unit,
    onEndTimeChange: (endTime: Int) -> Unit,
    totalLessonsCount: Int
) {
    var startTime by remember { mutableStateOf(initStartTime) }
    var endTime by remember { mutableStateOf(initEndTime) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        IntTextField(
            value = startTime,
            range = 1..totalLessonsCount,
            onValueChange = { newTime ->
                startTime = newTime.coerceIn(1, totalLessonsCount)
                onStartTimeChange(startTime)
            }
        )
        Text(
            text = stringResource(Res.string.dialog_course_time_picker_section),
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "—",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.weight(1f))
        IntTextField(
            value = endTime,
            range = 1..totalLessonsCount,
            onValueChange = { newTime ->
                endTime = newTime.coerceIn(startTime, totalLessonsCount)
                onEndTimeChange(endTime)
            }
        )
        Text(
            text = stringResource(Res.string.dialog_course_time_picker_section),
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}