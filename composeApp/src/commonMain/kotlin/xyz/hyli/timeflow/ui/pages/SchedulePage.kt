package xyz.hyli.timeflow.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.friday
import timeflow.composeapp.generated.resources.monday
import timeflow.composeapp.generated.resources.saturday
import timeflow.composeapp.generated.resources.settings_subtitle_schedule_empty
import timeflow.composeapp.generated.resources.sunday
import timeflow.composeapp.generated.resources.thursday
import timeflow.composeapp.generated.resources.tuesday
import timeflow.composeapp.generated.resources.wednesday
import xyz.hyli.timeflow.datastore.Lesson
import xyz.hyli.timeflow.datastore.Schedule
import xyz.hyli.timeflow.datastore.Weekday
import xyz.hyli.timeflow.ui.viewmodel.TimeFlowViewModel

@Composable
fun ScheduleScreen(
    viewModel: TimeFlowViewModel,
    navHostController: NavHostController
) {
    val settings by viewModel.settings.collectAsState()
    val schedule = settings.schedule[settings.selectedSchedule]
    val row = if (schedule?.displayWeekends == true) 7 else 5
    val column = schedule?.lessonTimePeriodInfo?.getTotalLessons()

    if (schedule == null || column == null || column == 0) {
        // 显示空状态或错误信息
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.settings_subtitle_schedule_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 课程表表格
        ScheduleTable(
            rows = row,
            columns = column,
            schedule = schedule,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ScheduleTable(
    rows: Int,
    columns: Int,
    schedule: Schedule,
    modifier: Modifier = Modifier
) {
    val lessonTimePeriodInfo = schedule.lessonTimePeriodInfo.morning +
            schedule.lessonTimePeriodInfo.afternoon +
            schedule.lessonTimePeriodInfo.evening

    Box(modifier = modifier.fillMaxWidth()) {
        // 底层：表格框架
        TableGrid(
            rows = rows,
            columns = columns,
            lessonTimePeriodInfo = lessonTimePeriodInfo
        )

        // 覆盖层：课程内容
        CourseOverlay(
            rows = rows,
            columns = columns,
            schedule = schedule,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun TableGrid(
    rows: Int,
    columns: Int,
    lessonTimePeriodInfo: List<Lesson>
) {
    val weekdays = listOf(
        Res.string.monday,
        Res.string.tuesday,
        Res.string.wednesday,
        Res.string.thursday,
        Res.string.friday,
        Res.string.saturday,
        Res.string.sunday
    ).map { stringResource(it) }

    Row(modifier = Modifier.fillMaxWidth()) {
        // 时间列
        TimeColumn(
            modifier = Modifier.width(48.dp),
            columns = columns,
            allLessons = lessonTimePeriodInfo
        )

        // 每一天的空白列（只显示表头和边框）
        for (dayIndex in 0 until rows) {
            EmptyDayColumn(
                modifier = Modifier.weight(1f),
                dayName = weekdays[dayIndex],
                columns = columns,
                isLastDay = dayIndex == rows - 1
            )
        }
    }
}

@Composable
fun TimeColumn(
    modifier: Modifier = Modifier,
    columns: Int,
    allLessons: List<Lesson>
) {
    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        // 表头 - 空格
        TableCell(
            height = 40.dp,
            isRightBorder = true,
            isBottomBorder = true
        ) { }

        // 时间行
        for (lessonIndex in 0 until columns) {
            TableCell(
                isRightBorder = true,
                isBottomBorder = lessonIndex < columns - 1
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${lessonIndex + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (lessonIndex < allLessons.size) {
                        Text(
                            text = "${allLessons[lessonIndex].start}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${allLessons[lessonIndex].end}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyDayColumn(
    modifier: Modifier = Modifier,
    dayName: String,
    columns: Int,
    isLastDay: Boolean
) {
    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        // 表头 - 星期
        TableCell(
            height = 40.dp,
            isRightBorder = !isLastDay,
            isBottomBorder = true
        ) {
            Text(
                text = dayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // 空白课程单元格
        for (lessonIndex in 0 until columns) {
            TableCell(
                isRightBorder = !isLastDay,
                isBottomBorder = lessonIndex < columns - 1
            ) {
                // 空白单元格，只显示边框
            }
        }
    }
}

@Composable
fun CourseOverlay(
    rows: Int,
    columns: Int,
    schedule: Schedule,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        // 时间列占位（与底层对齐）
        Box(modifier = Modifier.width(48.dp))

        // 课程覆盖层
        for (dayIndex in 0 until rows) {
            CourseColumn(
                modifier = Modifier.weight(1f),
                dayIndex = dayIndex,
                columns = columns,
                schedule = schedule
            )
        }
    }
}

@Composable
fun CourseColumn(
    modifier: Modifier = Modifier,
    dayIndex: Int,
    columns: Int,
    schedule: Schedule
) {
    val weekdays = listOf(
        Weekday.MONDAY,
        Weekday.TUESDAY,
        Weekday.WEDNESDAY,
        Weekday.THURSDAY,
        Weekday.FRIDAY,
        Weekday.SATURDAY,
        Weekday.SUNDAY
    )
    val daySchedule = schedule.courses.filter {
        it.weekday == weekdays[dayIndex]
    }

    Column(modifier = modifier) {
        // 表头占位（与底层对齐）
        Box(modifier = Modifier.height(40.dp))

        // 课程单元格
        for (lessonIndex in 0 until columns) {
            // 查找该时间段的课程
            val coursesForThisTime = daySchedule.filter { course ->
                course.time.start <= lessonIndex + 1 && course.time.end >= lessonIndex + 1
            }

            Box(
                modifier = Modifier.height(64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (coursesForThisTime.isNotEmpty()) {
                    val course = coursesForThisTime.first()
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableCell(
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    isRightBorder: Boolean = false,
    isBottomBorder: Boolean = false,
    content: @Composable () -> Unit
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val borderWidth = 1.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                color = MaterialTheme.colorScheme.surface
            )
            // 绘制右边框
            .then(
                if (isRightBorder) {
                    Modifier.drawBehind {
                        drawLine(
                            color = borderColor,
                            start = Offset(size.width, 0f),
                            end = Offset(size.width, size.height),
                            strokeWidth = borderWidth.toPx()
                        )
                    }
                } else Modifier
            )
            // 绘制底边框
            .then(
                if (isBottomBorder) {
                    Modifier.drawBehind {
                        drawLine(
                            color = borderColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = borderWidth.toPx()
                        )
                    }
                } else Modifier
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
