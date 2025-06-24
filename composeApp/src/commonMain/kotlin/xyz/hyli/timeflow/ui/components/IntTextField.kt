package xyz.hyli.timeflow.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import xyz.hyli.timeflow.ui.theme.NotoSans

@Composable
fun IntTextField(
    modifier: Modifier = Modifier,
    value: Int,
    range: IntRange,
    label: @Composable (() -> Unit)? = null,
    textAlign: TextAlign = TextAlign.Center,
    onValueChange: (Int) -> Unit,
    shape: Shape = OutlinedTextFieldDefaults.shape,
) {
    var textValue by remember { mutableStateOf(value.toString()) }
    var isValid by remember { mutableStateOf(true) }
    LaunchedEffect(textValue) {
        try {
            val numValue = textValue.toIntOrNull()
            isValid = numValue in range
            if (isValid) {
                onValueChange(numValue!!)
            }
        } catch (_: Exception) {
            isValid = false
        }
    }
    OutlinedTextField(
        value = textValue,
        onValueChange = { textValue = it },
        modifier = modifier,
        textStyle = LocalTextStyle.current.copy(
            fontFamily = NotoSans,
            textAlign = textAlign
        ),
        label = label,
        singleLine = true,
        isError = !isValid,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (isValid) {
                    textValue.toIntOrNull()?.let { onValueChange(it) }
                }
            }
        ),
        shape = shape,
    )
}