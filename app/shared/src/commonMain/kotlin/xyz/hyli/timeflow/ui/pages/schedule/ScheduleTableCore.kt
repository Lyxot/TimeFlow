/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.data.Lesson
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.ui.theme.NotoSans
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun ScheduleTableLayout(
    config: ScheduleDisplayConfig,
    tableDataFactory: @Composable (cellWidth: Dp) -> ScheduleTableData,
    modifier: Modifier = Modifier,
    fixedCellWidth: Dp? = null,
    columnContent: @Composable BoxScope.(dayIndex: Int, tableData: ScheduleTableData) -> Unit
) {
    val headerWidth = remember { mutableStateOf(48.dp) }
    val headerHeight = remember { mutableStateOf(40.dp) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cellWidth = fixedCellWidth ?: ((maxWidth - headerWidth.value - 5.dp) / config.columns)
        val tableData = tableDataFactory(cellWidth)
        val totalHeight = tableData.rowYOffsets.last()
        val naturalHeight = headerHeight.value + totalHeight

        val tableContent: @Composable () -> Unit = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(naturalHeight)
            ) {
                // Weekday column headers
                WeekdayHeaders(
                    config = config,
                    cellWidth = cellWidth,
                    headerWidth = headerWidth,
                    headerHeight = headerHeight
                )

                // Lesson row headers + left-side horizontal dividers
                LessonRowHeaders(
                    config = config,
                    tableData = tableData,
                    headerWidth = headerWidth,
                    headerHeight = headerHeight
                )

                // Grid dividers (vertical + per-column horizontal)
                GridDividers(
                    config = config,
                    tableData = tableData,
                    cellWidth = cellWidth,
                    headerWidth = headerWidth.value,
                    headerHeight = headerHeight.value
                )

                // Column content slots
                for (dayIndex in 0 until config.columns) {
                    Box(
                        modifier = Modifier
                            .padding(end = 1.dp)
                            .width(cellWidth - 1.dp)
                            .offset(
                                x = headerWidth.value + 5.dp + cellWidth * dayIndex,
                                y = headerHeight.value + 1.dp
                            )
                    ) {
                        columnContent(dayIndex, tableData)
                    }
                }
            }
        }

        if (fixedCellWidth != null) {
            val naturalWidth = headerWidth.value + 5.dp + fixedCellWidth * config.columns
            val scaleFactor = (maxWidth.value / naturalWidth.value).coerceAtMost(1f)

            Layout(
                content = tableContent,
                modifier = Modifier.fillMaxWidth()
            ) { measurables, constraints ->
                val placeable = measurables.first().measure(
                    Constraints.fixed(
                        naturalWidth.roundToPx(),
                        naturalHeight.roundToPx()
                    )
                )
                val scaledWidth = (placeable.width * scaleFactor).toInt().coerceAtMost(constraints.maxWidth)
                val scaledHeight = (placeable.height * scaleFactor).toInt()
                layout(scaledWidth, scaledHeight) {
                    placeable.placeWithLayer(0, 0) {
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                }
            }
        } else {
            tableContent()
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun WeekdayHeaders(
    config: ScheduleDisplayConfig,
    cellWidth: Dp,
    headerWidth: MutableState<Dp>,
    headerHeight: MutableState<Dp>
) {
    val weekdayNames = listOf(
        Res.string.monday,
        Res.string.tuesday,
        Res.string.wednesday,
        Res.string.thursday,
        Res.string.friday,
        Res.string.saturday,
        Res.string.sunday
    ).map { stringResource(it) }
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    Box(modifier = Modifier.fillMaxWidth()) {
        for (dayIndex in 0 until config.columns) {
            SubcomposeLayout(
                modifier = Modifier.padding(end = 1.dp)
            ) { constraints ->
                val column = subcompose("day$dayIndex") {
                    Column(
                        modifier = Modifier
                            .width(cellWidth - 1.dp)
                            .height(IntrinsicSize.Max)
                            .offset(
                                x = headerWidth.value + 5.dp + cellWidth * dayIndex,
                                y = 0.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val color =
                            if (config.showDates && config.dateList != null && config.dateList[dayIndex] == today) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onBackground
                            }
                        Text(
                            text = weekdayNames[dayIndex],
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = color
                        )
                        if (config.showDates && config.dateList != null) {
                            Text(
                                text = config.dateList[dayIndex].let {
                                    it.month.number.toString() + "/" + it.day.toString().padStart(2, '0')
                                },
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = color.copy(alpha = 0.6f)
                            )
                        }
                    }
                }[0].measure(constraints)
                val newHeight = column.height.toDp()
                if (headerHeight.value != newHeight) {
                    headerHeight.value = newHeight
                }
                layout(cellWidth.roundToPx(), column.height) {
                    column.placeRelative(0, 0)
                }
            }
        }
    }
}

@Composable
private fun LessonRowHeaders(
    config: ScheduleDisplayConfig,
    tableData: ScheduleTableData,
    headerWidth: MutableState<Dp>,
    headerHeight: MutableState<Dp>
) {
    // Use offset-based positioning (matching grid content) to avoid
    // cumulative pixel rounding drift from Column layout.
    Box {
        for (lessonIndex in 0 until config.rows) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier
                    .width(headerWidth.value + 4.dp)
                    .offset(
                        x = 1.dp,
                        y = headerHeight.value + tableData.rowYOffsets[lessonIndex] - 1.dp
                    )
            )
            LessonRowHeader(
                lessonIndex = lessonIndex,
                rowHeight = tableData.rowHeights[lessonIndex],
                yOffset = headerHeight.value + tableData.rowYOffsets[lessonIndex],
                lessons = config.lessons,
                headerWidth = headerWidth
            )
        }
    }
}

@Composable
private fun LessonRowHeader(
    lessonIndex: Int,
    rowHeight: Dp,
    yOffset: Dp,
    lessons: List<Lesson>,
    headerWidth: MutableState<Dp>
) {
    SubcomposeLayout(
        modifier = Modifier.offset(x = 1.dp, y = yOffset)
    ) { constraints ->
        val heightPx = rowHeight.roundToPx()

        val column = subcompose("lesson$lessonIndex") {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${lessonIndex + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                if (lessonIndex < lessons.size) {
                    Text(
                        text = "${lessons[lessonIndex].start}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = NotoSans,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = "${lessons[lessonIndex].end}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = NotoSans,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }[0].measure(constraints.copy(minHeight = heightPx, maxHeight = heightPx))
        val newWidth = column.width.toDp()
        if (headerWidth.value != newWidth) {
            headerWidth.value = newWidth
        }
        layout(column.width, heightPx) {
            column.placeRelative(0, 0)
        }
    }
}

@Composable
private fun GridDividers(
    config: ScheduleDisplayConfig,
    tableData: ScheduleTableData,
    cellWidth: Dp,
    headerWidth: Dp,
    headerHeight: Dp
) {
    val totalHeight = tableData.rowYOffsets.last()

    // Vertical dividers
    for (dayIndex in 0 until config.columns) {
        VerticalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier
                .offset(
                    x = headerWidth + 4.dp + cellWidth * dayIndex,
                    y = 0.dp
                )
                .height(headerHeight + totalHeight)
        )
    }

    // Horizontal dividers per column (respecting noGridCells)
    for (dayIndex in 0 until config.columns) {
        Box(
            modifier = Modifier
                .width(cellWidth)
                .offset(x = headerWidth + 4.dp + cellWidth * dayIndex)
        ) {
            for (lessonIndex in 0 until config.rows) {
                if (lessonIndex + 1 in tableData.noGridCells[dayIndex]) continue
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.offset(
                        y = headerHeight + tableData.rowYOffsets[lessonIndex] - 1.dp
                    )
                )
            }
        }
    }
}
