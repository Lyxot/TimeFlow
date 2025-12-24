/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.shared.generated.resources.Res
import xyz.hyli.timeflow.shared.generated.resources.cancel
import xyz.hyli.timeflow.shared.generated.resources.confirm
import xyz.hyli.timeflow.shared.generated.resources.preference_date_dialog_title

// ==================== Preference Date ====================

const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L

@Composable
fun PreferenceDate(
    value: LocalDate,
    onValueChange: (LocalDate) -> Unit,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
    title: String,
    subtitle: String? = null,
    enabled: Dependency = Dependency.Enabled,
    visible: Dependency = Dependency.Enabled
) {
    val isEnabled by enabled.asState()
    var showDialog by remember { mutableStateOf(false) }

    BasePreference(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        visible = visible,
        onClick = if (isEnabled) {
            { showDialog = true }
        } else null,
        leadingContent = null,
        trailingContent = {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    )

    if (showDialog) {
        DatePickerDialog(
            initialDate = value,
            selectableDates = selectableDates,
            onDateSelected = { selectedDate ->
                onValueChange(selectedDate)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: LocalDate,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDays() * MILLIS_PER_DAY,
        selectableDates = selectableDates
    )
    val dialogState = rememberDialogState()

    LaunchedEffect(Unit) {
        dialogState.show()
    }

    if (dialogState.visible) {
        MyDialog(
            state = dialogState,
            title = { Text(stringResource(Res.string.preference_date_dialog_title)) },
            buttons = DialogDefaults.buttons(
                positive = DialogButton(stringResource(Res.string.confirm)),
                negative = DialogButton(stringResource(Res.string.cancel))
            ),
            onEvent = { event ->
                when (event) {
                    is DialogEvent.Button -> {
                        if (event.isPositiveButton) {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val epochDays = (millis / (MILLIS_PER_DAY)).toInt()
                                onDateSelected(LocalDate.fromEpochDays(epochDays))
                            }
                        } else {
                            onDismiss()
                        }
                    }

                    DialogEvent.Dismissed -> onDismiss()
                }
            }
        ) {
            BoxWithConstraints {
                // 456dp is minimum height of picker mode, 180dp is minimum height of input mode
                val requiredHeight =
                    if (datePickerState.displayMode == DisplayMode.Picker) 456.dp else 180.dp
                val scaleY =
                    if (this.maxHeight > requiredHeight) 1f else (this.maxHeight / requiredHeight)
                // Make sure there is always enough room
                val width = this.maxWidth / scaleY
                Box(
                    modifier = Modifier
                        .requiredSizeIn(minWidth = width, minHeight = requiredHeight)
                ) {
                    DatePicker(
                        modifier = Modifier
                            .scale(scaleY)
                            .width(width),
                        title = null,
                        state = datePickerState
                    )
                }
            }
        }
    }
} 