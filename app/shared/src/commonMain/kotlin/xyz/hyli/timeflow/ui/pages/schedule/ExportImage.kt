/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import xyz.hyli.timeflow.data.Schedule

expect fun ImageBitmap.encodeToPng(): ByteArray

@Composable
fun ScheduleImageCapture(
    schedule: Schedule,
    onCaptured: (ByteArray) -> Unit,
    onError: (Throwable) -> Unit,
) {
    val columns = if (schedule.displayWeekends) 7 else 5
    val rows = schedule.lessonTimePeriodInfo.totalLessonsCount
    if (rows == 0) {
        LaunchedEffect(Unit) { onError(IllegalStateException("Empty schedule")) }
        return
    }

    val captureLayer = rememberGraphicsLayer()
    val naturalWidth = 48.dp + 5.dp + 96.dp * columns

    Box(
        modifier = Modifier
            .wrapContentWidth(unbounded = true)
            .wrapContentHeight(unbounded = true)
            .requiredWidth(naturalWidth + 16.dp)
            .graphicsLayer { alpha = 0f }
            .drawWithContent {
                captureLayer.record(
                    size = IntSize(size.width.toInt(), size.height.toInt())
                ) { this@drawWithContent.drawContent() }
                drawLayer(captureLayer)
            }
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp)
        ) {
            Text(
                text = schedule.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )
            ReadOnlyScheduleTable(
                schedule = schedule,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    LaunchedEffect(Unit) {
        try {
            // Wait so the content is fully drawn
            kotlinx.coroutines.delay(100)
            val bitmap = captureLayer.toImageBitmap()
            val png = bitmap.encodeToPng()
            onCaptured(png)
        } catch (e: Throwable) {
            onError(e)
        }
    }
}
