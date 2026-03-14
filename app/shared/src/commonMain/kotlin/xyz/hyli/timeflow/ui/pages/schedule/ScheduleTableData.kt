/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.schedule

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import xyz.hyli.timeflow.data.*

val BASE_CELL_HEIGHT = 56.dp
private val CELL_CONTENT_PADDING = 12.dp  // Card 2*2dp + Column ~2*4dp horizontal
private val CELL_VERTICAL_PADDING = 12.dp // Card 2*2dp + Column ~2*4dp vertical
private val INTER_ENTRY_SPACING = 9.dp    // Spacer(4dp) + HorizontalDivider(0.5dp) + Spacer(4dp)

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

fun computeRowYOffsets(rowHeights: List<Dp>): List<Dp> {
    val offsets = mutableListOf(0.dp)
    rowHeights.forEach { h -> offsets.add(offsets.last() + h) }
    return offsets
}

fun computeRowHeights(
    rows: Int,
    columns: Int,
    estimateHeight: (courses: Map<Short, Course>, span: Int) -> Dp,
    timeSlotsProvider: (dayIndex: Int) -> Map<Range, Map<Short, Course>>
): List<Dp> {
    val heights = MutableList(rows) { BASE_CELL_HEIGHT }
    for (dayIndex in 0 until columns) {
        val daySlots = timeSlotsProvider(dayIndex)
        for ((range, courses) in daySlots) {
            if (courses.isEmpty()) continue
            val span = range.end - range.start + 1
            val neededHeight = estimateHeight(courses, span)
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

/**
 * Measure the height of a single course's text content using [TextMeasurer].
 */
private fun measureWeeklyCourseHeight(
    course: Course,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    nameStyle: TextStyle,
    infoStyle: TextStyle,
    textConstraints: Constraints,
    density: androidx.compose.ui.unit.Density,
    span: Int
): Dp {
    var heightPx = 0
    val nameHeight3LineMax = textMeasurer.measure(
        text = course.name,
        maxLines = 3,
        style = nameStyle,
        constraints = textConstraints
    ).size.height
    val nameHeight = textMeasurer.measure(
        text = course.name,
        style = nameStyle,
        constraints = textConstraints
    ).size.height
    if (course.classroom.isNotBlank()) {
        heightPx += textMeasurer.measure(
            text = "@${course.classroom}",
            style = infoStyle,
            constraints = textConstraints
        ).size.height
    }
    if (course.teacher.isNotBlank()) {
        heightPx += textMeasurer.measure(
            text = course.teacher,
            style = infoStyle,
            maxLines = 1,
            constraints = textConstraints
        ).size.height
    }
    heightPx += if (with(density) { (heightPx + nameHeight).toDp() } + CELL_VERTICAL_PADDING > BASE_CELL_HEIGHT * span) {
        // If the info lines + name exceed base span height, use the 3-line max height for name to avoid overflow
        nameHeight3LineMax
    } else {
        nameHeight
    }
    return with(density) { heightPx.toDp() } + CELL_VERTICAL_PADDING
}

/**
 * Measure the height of an overview cell containing multiple courses using [TextMeasurer].
 */
private fun measureOverviewCellHeight(
    courses: Map<Short, Course>,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    nameStyle: TextStyle,
    infoStyle: TextStyle,
    textConstraints: Constraints,
    density: androidx.compose.ui.unit.Density,
    nameMinHeightPx: Int,
    span: Int
): Dp {
    var heightPx = 0
    courses.values.forEachIndexed { index, course ->
        if (index > 0) heightPx += with(density) { INTER_ENTRY_SPACING.toPx() }.toInt()
        val nameHeight3LineMax = textMeasurer.measure(
            text = course.name,
            maxLines = 3,
            style = nameStyle,
            constraints = textConstraints
        ).size.height
        val nameHeight = textMeasurer.measure(
            text = course.name,
            style = nameStyle,
            constraints = textConstraints
        ).size.height
        heightPx += textMeasurer.measure(
            text = course.week.toString(),
            style = infoStyle,
            constraints = textConstraints
        ).size.height
        heightPx += textMeasurer.measure(
            text = "${course.time.start}-${course.time.end}",
            style = infoStyle,
            constraints = textConstraints
        ).size.height
        if (course.classroom.isNotBlank()) {
            heightPx += textMeasurer.measure(
                text = "@${course.classroom}",
                style = infoStyle,
                constraints = textConstraints
            ).size.height
        }
        if (course.teacher.isNotBlank()) {
            heightPx += textMeasurer.measure(
                text = course.teacher,
                style = infoStyle,
                maxLines = 1,
                constraints = textConstraints
            ).size.height
        }
        heightPx += if (with(density) { (heightPx + nameHeight).toDp() } + CELL_VERTICAL_PADDING > BASE_CELL_HEIGHT * span) {
            // If the info lines + name exceed base span height, use the 3-line max height for name to avoid overflow
            nameHeight3LineMax
        } else {
            nameHeight
        }
    }
    return with(density) { heightPx.toDp() } + CELL_VERTICAL_PADDING
}

@Composable
fun rememberWeeklyTableData(
    schedule: Schedule,
    config: ScheduleDisplayConfig,
    cellWidth: Dp
): ScheduleTableData {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val nameStyle = MaterialTheme.typography.labelMedium
    val infoStyle = MaterialTheme.typography.labelSmall

    val rowHeights = remember(schedule, config.rows, cellWidth, density, nameStyle, infoStyle) {
        val contentWidthPx = with(density) { (cellWidth - CELL_CONTENT_PADDING).toPx() }
            .toInt().coerceAtLeast(1)
        val textConstraints = Constraints(maxWidth = contentWidthPx)
        val nameMinHeightPx = textMeasurer.measure(
            text = "W\nW\nW",
            style = nameStyle,
            constraints = textConstraints
        ).size.height

        computeRowHeights(
            rows = config.rows,
            columns = config.columns,
            estimateHeight = { courses, span ->
                courses.values.maxOfOrNull { course ->
                    measureWeeklyCourseHeight(
                        course, textMeasurer, nameStyle, infoStyle, textConstraints, density, span
                    )
                } ?: 0.dp
            },
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
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val nameStyle = MaterialTheme.typography.labelMedium
    val infoStyle = MaterialTheme.typography.labelSmall

    val overviewData = remember(schedule) {
        List(config.columns) { dayIndex ->
            schedule.getOverviewTimeSlotsFor(weekdays[dayIndex])
        }
    }
    val rowHeights = remember(overviewData, config.rows, cellWidth, density, nameStyle, infoStyle) {
        val contentWidthPx = with(density) { (cellWidth - CELL_CONTENT_PADDING).toPx() }
            .toInt().coerceAtLeast(1)
        val textConstraints = Constraints(maxWidth = contentWidthPx)
        val nameMinHeightPx = textMeasurer.measure(
            text = "W\nW\nW",
            style = nameStyle,
            constraints = textConstraints
        ).size.height

        computeRowHeights(
            rows = config.rows,
            columns = config.columns,
            estimateHeight = { courses, span ->
                measureOverviewCellHeight(
                    courses, textMeasurer, nameStyle, infoStyle, textConstraints, density,
                    nameMinHeightPx, span
                )
            },
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
