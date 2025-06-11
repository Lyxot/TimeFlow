package xyz.hyli.timeflow.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.cancel
import timeflow.composeapp.generated.resources.confirm
import timeflow.composeapp.generated.resources.preference_date_dialog_title

// ==================== Preference Date ====================

@Composable
fun PreferenceDate(
    value: LocalDate,
    onValueChange: (LocalDate) -> Unit,
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
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDays() * 24 * 60 * 60 * 1000L
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
                                val epochDays = (millis / (24 * 60 * 60 * 1000L)).toInt()
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
                // 360 is minimum because DatePicker uses 12.dp horizontal padding and 48.dp for each week day
                val scaleX =
                    remember(this.maxWidth) { if (this.maxWidth > 360.dp) 1f else (this.maxWidth / 360.dp) }
                val scaleY =
                    remember(this.maxHeight) { if (this.maxHeight > 568.dp) 1f else (this.maxHeight / 568.dp) }
                // Make sure there is always enough room, so use requiredWidthIn
                Box(
                    modifier = Modifier
                        .requiredSizeIn(minWidth = 360.dp, minHeight = 568.dp)
                ) {
                    DatePicker(
                        modifier = Modifier
                            .scale(minOf(scaleX, scaleY)),
                        title = null,
                        state = datePickerState
                    )
                }
            }
        }
    }
} 