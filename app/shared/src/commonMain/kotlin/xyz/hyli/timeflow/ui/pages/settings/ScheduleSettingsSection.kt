/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.pages.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.data.Date
import xyz.hyli.timeflow.data.LessonTimePeriodInfo
import xyz.hyli.timeflow.data.Schedule
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.ui.components.*
import xyz.hyli.timeflow.utils.InputValidation
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.isDesktop

/**
 * Reusable schedule settings section content.
 * Used by both the Settings page and the AI Preview page.
 *
 * @param schedule The schedule to display/edit.
 * @param onScheduleChanged Called with the updated schedule when any field changes.
 * @param lessonsPerDayContent Optional trailing content for the "lessons per day" row
 *   (e.g. a navigate-next icon in settings). If null, inline number pickers are shown
 *   for morning/afternoon/evening counts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceScope.ScheduleSettingsContent(
    schedule: Schedule,
    onScheduleChanged: (Schedule) -> Unit,
    lessonsPerDayContent: (@Composable () -> Unit)? = null,
) {
    val validationMessages = localizedValidationMessages()

    // Schedule Name
    PreferenceInputText(
        value = schedule.name,
        onValueChange = { newName ->
            onScheduleChanged(schedule.copy(name = newName))
        },
        title = stringResource(Res.string.settings_title_schedule_name),
        validator = rememberDialogInputValidator(
            validate = {
                val error = InputValidation.validateName(it, messages = validationMessages)
                if (error == null)
                    DialogInputValidator.Result.Valid
                else
                    DialogInputValidator.Result.Error(error)
            }
        ),
        maxLength = InputValidation.MAX_NAME_LENGTH
    )
    // Term Start Date
    PreferenceDate(
        value = schedule.termStartDate.toLocalDate(),
        onValueChange = { newDate ->
            val newTermStartDate = Date.fromLocalDate(newDate)
            val currentTermEndDate = schedule.termEndDate
            val newTermEndDate =
                if (newTermStartDate.weeksTill(currentTermEndDate) in 1..60) {
                    currentTermEndDate
                } else newTermStartDate.addWeeks(schedule.totalWeeks)
            onScheduleChanged(
                schedule.copy(
                    termStartDate = newTermStartDate,
                    termEndDate = newTermEndDate
                )
            )
        },
        title = stringResource(Res.string.settings_title_schedule_term_start_date)
    )
    // Term End Date
    PreferenceDate(
        value = schedule.termEndDate.toLocalDate(),
        onValueChange = { newDate ->
            onScheduleChanged(schedule.copy(termEndDate = Date.fromLocalDate(newDate)))
        },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val epochDays = (utcTimeMillis / MILLIS_PER_DAY).toInt()
                val date = LocalDate.fromEpochDays(epochDays)
                return schedule.termStartDate.weeksTill(date) in 1..60
            }
        },
        title = stringResource(Res.string.settings_title_schedule_term_end_date)
    )
    // Total Weeks
    PreferenceNumber(
        style = if (currentPlatform().isDesktop())
            PreferenceNumberStyle.TextField()
        else
            PreferenceNumberStyle.Wheel,
        value = schedule.totalWeeks,
        min = 1,
        max = 60,
        onValueChange = {
            val newEndDate = schedule.termStartDate.addWeeks(it)
            onScheduleChanged(schedule.copy(termEndDate = newEndDate))
        },
        title = stringResource(Res.string.settings_title_schedule_total_weeks)
    )
    // Lessons Per Day
    if (lessonsPerDayContent != null) {
        lessonsPerDayContent()
    } else {
        val numberStyle = if (currentPlatform().isDesktop())
            PreferenceNumberStyle.TextField()
        else
            PreferenceNumberStyle.Wheel
        PreferenceNumber(
            style = numberStyle,
            value = schedule.lessonTimePeriodInfo.morning.size,
            min = 0,
            max = 10,
            onValueChange = { count ->
                onScheduleChanged(
                    schedule.copy(
                        lessonTimePeriodInfo = LessonTimePeriodInfo.fromPeriodCounts(
                            morningCount = count,
                            afternoonCount = schedule.lessonTimePeriodInfo.afternoon.size,
                            eveningCount = schedule.lessonTimePeriodInfo.evening.size
                        )
                    )
                )
            },
            title = stringResource(Res.string.settings_title_schedule_lessons_per_day_morning)
        )
        PreferenceNumber(
            style = numberStyle,
            value = schedule.lessonTimePeriodInfo.afternoon.size,
            min = 0,
            max = 10,
            onValueChange = { count ->
                onScheduleChanged(
                    schedule.copy(
                        lessonTimePeriodInfo = LessonTimePeriodInfo.fromPeriodCounts(
                            morningCount = schedule.lessonTimePeriodInfo.morning.size,
                            afternoonCount = count,
                            eveningCount = schedule.lessonTimePeriodInfo.evening.size
                        )
                    )
                )
            },
            title = stringResource(Res.string.settings_title_schedule_lessons_per_day_afternoon)
        )
        PreferenceNumber(
            style = numberStyle,
            value = schedule.lessonTimePeriodInfo.evening.size,
            min = 0,
            max = 10,
            onValueChange = { count ->
                onScheduleChanged(
                    schedule.copy(
                        lessonTimePeriodInfo = LessonTimePeriodInfo.fromPeriodCounts(
                            morningCount = schedule.lessonTimePeriodInfo.morning.size,
                            afternoonCount = schedule.lessonTimePeriodInfo.afternoon.size,
                            eveningCount = count
                        )
                    )
                )
            },
            title = stringResource(Res.string.settings_title_schedule_lessons_per_day_evening)
        )
    }
    // Display Weekends
    PreferenceBool(
        style = PreferenceBoolStyle.Style.Switch,
        value = schedule.displayWeekends,
        onValueChange = {
            onScheduleChanged(schedule.copy(displayWeekends = it))
        },
        title = stringResource(Res.string.settings_title_display_weekends)
    )
}
