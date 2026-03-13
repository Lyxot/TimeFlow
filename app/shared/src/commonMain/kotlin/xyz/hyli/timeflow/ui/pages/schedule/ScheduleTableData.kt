/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import xyz.hyli.timeflow.data.*

val BASE_CELL_HEIGHT = 56.dp
private val NAME_LINE_HEIGHT = 16.dp
private val INFO_LINE_HEIGHT = 14.dp
private const val NAME_CHAR_WIDTH = 12f
private const val INFO_CHAR_WIDTH = 11f
const val CELL_CONTENT_PADDING = 12f

val weekdays = listOf(
    Weekday.MONDAY,
    Weekday.TUESDAY,
    Weekday.WEDNESDAY,
    Weekday.THURSDAY,
    Weekday.FRIDAY,
    Weekday.SATURDAY,
    Weekday.SUNDAY
)

data class ScheduleDisplayConfig(
    val rows: Int,
    val columns: Int,
    val showDates: Boolean,
    val dateList: List<LocalDate>?,
    val lessons: List<Lesson>,
)

data class ScheduleTableData(
    val config: ScheduleDisplayConfig,
    val rowHeights: List<Dp>,
    val rowYOffsets: List<Dp>,
    val noGridCells: List<Set<Int>>,
)

fun interface CellHeightEstimator {
    fun estimateHeight(courses: Map<Short, Course>, availableTextWidth: Float): Dp
}

fun estimateTextLines(text: String, availableWidthDp: Float, charWidthDp: Float): Int {
    if (text.isEmpty()) return 0
    var totalWidth = 0f
    for (c in text) {
        totalWidth += if (c.code > 0x7F) charWidthDp else charWidthDp * 0.6f
    }
    return maxOf(1, kotlin.math.ceil((totalWidth / availableWidthDp).toDouble()).toInt())
}

fun computeRowYOffsets(rowHeights: List<Dp>): List<Dp> {
    val offsets = mutableListOf(0.dp)
    rowHeights.forEach { h -> offsets.add(offsets.last() + h) }
    return offsets
}

fun computeRowHeights(
    rows: Int,
    columns: Int,
    availableTextWidth: Float,
    estimator: CellHeightEstimator,
    timeSlotsProvider: (dayIndex: Int) -> Map<Range, Map<Short, Course>>
): List<Dp> {
    val heights = MutableList(rows) { BASE_CELL_HEIGHT }
    for (dayIndex in 0 until columns) {
        val daySlots = timeSlotsProvider(dayIndex)
        for ((range, courses) in daySlots) {
            if (courses.isEmpty()) continue
            val span = range.end - range.start + 1
            val neededHeight = estimator.estimateHeight(courses, availableTextWidth)
            val baseSpanHeight = BASE_CELL_HEIGHT * span
            if (neededHeight > baseSpanHeight) {
                val perRowExtra = (neededHeight - baseSpanHeight) / span
                for (i in (range.start - 1) until range.end) {
                    if (i in heights.indices) {
                        heights[i] = maxOf(heights[i], BASE_CELL_HEIGHT + perRowExtra)
                    }
                }
            }
        }
    }
    return heights
}

fun computeNoGridCells(
    columns: Int,
    timeSlotsProvider: (dayIndex: Int) -> Collection<Range>
): List<Set<Int>> {
    return List(columns) { dayIndex ->
        timeSlotsProvider(dayIndex).flatMapTo(mutableSetOf()) { range ->
            if (range.end - range.start > 0) {
                (range.start until range.end).map { it + 1 }
            } else {
                emptyList()
            }
        }
    }
}

val WeeklyCellHeightEstimator = CellHeightEstimator { courses, availableTextWidth ->
    courses.values.maxOfOrNull { course ->
        val nameLines = estimateTextLines(course.name, availableTextWidth, NAME_CHAR_WIDTH)
        var h = NAME_LINE_HEIGHT * nameLines
        if (course.classroom.isNotBlank()) {
            h += INFO_LINE_HEIGHT * minOf(
                2,
                estimateTextLines("@${course.classroom}", availableTextWidth, INFO_CHAR_WIDTH)
            )
        }
        if (course.teacher.isNotBlank()) {
            h += INFO_LINE_HEIGHT
        }
        h + 8.dp
    } ?: 0.dp
}

private val INTER_ENTRY_SPACING = 9.dp // Spacer(4dp) + HorizontalDivider(0.5dp) + Spacer(4dp)
private val COLUMN_PADDING = 8.dp      // Column padding 4dp top + 4dp bottom

val OverviewCellHeightEstimator = CellHeightEstimator { courses, availableTextWidth ->
    var neededHeight = COLUMN_PADDING
    courses.values.forEachIndexed { index, course ->
        if (index > 0) neededHeight += INTER_ENTRY_SPACING
        neededHeight += NAME_LINE_HEIGHT * minOf(
            3,
            estimateTextLines(course.name, availableTextWidth, NAME_CHAR_WIDTH)
        )
        val weekTextLen = course.week.toString().length + 4
        neededHeight += INFO_LINE_HEIGHT * estimateTextLines(
            "x".repeat(weekTextLen),
            availableTextWidth,
            INFO_CHAR_WIDTH
        )
        neededHeight += INFO_LINE_HEIGHT // time period
        if (course.classroom.isNotBlank()) {
            neededHeight += INFO_LINE_HEIGHT * minOf(
                2,
                estimateTextLines(course.classroom, availableTextWidth, INFO_CHAR_WIDTH)
            )
        }
        if (course.teacher.isNotBlank()) {
            neededHeight += INFO_LINE_HEIGHT
        }
    }
    neededHeight
}

@Composable
fun rememberWeeklyTableData(
    schedule: Schedule,
    config: ScheduleDisplayConfig,
    cellWidth: Dp
): ScheduleTableData {
    val availableTextWidth = (cellWidth - CELL_CONTENT_PADDING.dp).value
    val rowHeights = remember(schedule, config.rows, cellWidth) {
        computeRowHeights(
            rows = config.rows,
            columns = config.columns,
            availableTextWidth = availableTextWidth,
            estimator = WeeklyCellHeightEstimator,
            timeSlotsProvider = { dayIndex ->
                val courses = schedule.getCoursesOfWeekday(weekdays[dayIndex])
                courses.entries
                    .groupBy({ it.value.time }, { it.key to it.value })
                    .mapValues { it.value.toMap() }
            }
        )
    }
    val rowYOffsets = remember(rowHeights) { computeRowYOffsets(rowHeights) }
    val noGridCells = remember(schedule, config.columns) {
        computeNoGridCells(config.columns) { dayIndex ->
            schedule.getCoursesOfWeekday(weekdays[dayIndex]).values.map { it.time }.toSet()
        }
    }
    return ScheduleTableData(config, rowHeights, rowYOffsets, noGridCells)
}

@Composable
fun rememberOverviewTableData(
    schedule: Schedule,
    config: ScheduleDisplayConfig,
    cellWidth: Dp
): ScheduleTableData {
    val availableTextWidth = (cellWidth - CELL_CONTENT_PADDING.dp).value
    val overviewData = remember(schedule) {
        List(config.columns) { dayIndex ->
            schedule.getOverviewTimeSlotsFor(weekdays[dayIndex])
        }
    }
    val rowHeights = remember(overviewData, config.rows, cellWidth) {
        computeRowHeights(
            rows = config.rows,
            columns = config.columns,
            availableTextWidth = availableTextWidth,
            estimator = OverviewCellHeightEstimator,
            timeSlotsProvider = { dayIndex -> overviewData[dayIndex] }
        )
    }
    val rowYOffsets = remember(rowHeights) { computeRowYOffsets(rowHeights) }
    val noGridCells = remember(overviewData, config.columns) {
        computeNoGridCells(config.columns) { dayIndex ->
            overviewData[dayIndex].keys
        }
    }
    return ScheduleTableData(config, rowHeights, rowYOffsets, noGridCells)
}
