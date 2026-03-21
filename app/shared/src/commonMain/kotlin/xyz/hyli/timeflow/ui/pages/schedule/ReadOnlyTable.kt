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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.hyli.timeflow.data.Schedule

@Composable
fun ReadOnlyScheduleTable(
    schedule: Schedule,
    modifier: Modifier = Modifier,
    showDates: Boolean = false,
    currentWeek: Int? = null,
) {
    val columns = if (schedule.displayWeekends) 7 else 5
    val rows = schedule.lessonTimePeriodInfo.totalLessonsCount
    if (rows == 0) return

    val config = ScheduleDisplayConfig(
        rows = rows,
        columns = columns,
        showDates = showDates && currentWeek != null,
        dateList = currentWeek?.let { schedule.dateList(it) },
        lessons = schedule.lessonTimePeriodInfo.lessons
    )

    val overviewData = remember(schedule) {
        List(columns) { dayIndex ->
            schedule.getOverviewTimeSlotsFor(weekdays[dayIndex])
        }
    }

    ScheduleTableLayout(
        config = config,
        tableDataFactory = { cellWidth ->
            rememberOverviewTableData(schedule, config, cellWidth)
        },
        modifier = modifier,
        fixedCellWidth = 96.dp
    ) { dayIndex, tableData ->
        overviewData[dayIndex].forEach { (range, courses) ->
            if (courses.isEmpty()) return@forEach
            val cellHeight = tableData.rowYOffsets[range.end] - tableData.rowYOffsets[range.start - 1]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellHeight)
                    .padding(bottom = 1.dp)
                    .offset(y = tableData.rowYOffsets[range.start - 1] - 1.dp)
            ) {
                OverviewCourseCell(
                    courses = courses,
                    onClick = null
                )
            }
        }
    }
}
