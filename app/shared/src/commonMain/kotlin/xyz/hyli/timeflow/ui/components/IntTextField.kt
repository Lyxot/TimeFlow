/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
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