/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.data

fun WeekdayV1.toV2(): WeekdayV2 = when (this) {
    WeekdayV1.MONDAY -> WeekdayV2.MONDAY
    WeekdayV1.TUESDAY -> WeekdayV2.TUESDAY
    WeekdayV1.WEDNESDAY -> WeekdayV2.WEDNESDAY
    WeekdayV1.THURSDAY -> WeekdayV2.THURSDAY
    WeekdayV1.FRIDAY -> WeekdayV2.FRIDAY
    WeekdayV1.SATURDAY -> WeekdayV2.SATURDAY
    WeekdayV1.SUNDAY -> WeekdayV2.SUNDAY
}

fun CourseV1.toV2(): CourseV2 =
    CourseV2(
        name = this.name,
        teacher = this.teacher,
        classroom = this.classroom,
        time = RangeV2(
            start = this.time.start,
            end = this.time.end
        ),
        weekday = this.weekday.toV2(),
        week = WeekListV2(
            weeks = this.week.week
        ),
        color = this.color,
        note = ""
    )

fun DateV1.toV2(): DateV2 =
    DateV2(
        year = this.year,
        month = this.month,
        day = this.day
    )

fun LessonV1.toV2(): LessonV2 =
    LessonV2(
        start = TimeV2(
            hour = this.start.hour,
            minute = this.start.minute
        ),
        end = TimeV2(
            hour = this.end.hour,
            minute = this.end.minute
        )
    )

fun LessonTimePeriodInfoV1.toV2(): LessonTimePeriodInfoV2 =
    LessonTimePeriodInfoV2(
        morning = this.morning.map { it.toV2() },
        afternoon = this.afternoon.map { it.toV2() },
        evening = this.evening.map { it.toV2() }
    )

fun ScheduleV1.toV2(): ScheduleV2 {
    val coursesV2 = mutableMapOf<Short, CourseV2>()
    this.courses.forEach { courseV1 ->
        val id = newShortId(coursesV2.keys)
        coursesV2[id] = courseV1.toV2()
    }
    return ScheduleV2(
        name = this.name,
        deleted = this.deleted,
        courses = coursesV2,
        termStartDate = this.termStartDate.toV2(),
        termEndDate = this.termEndDate.toV2(),
        lessonTimePeriodInfo = this.lessonTimePeriodInfo.toV2(),
        displayWeekends = this.displayWeekends
    )
}

fun SettingsV1.toV2(): SettingsV2 {
    val schedulesV2 = mutableMapOf<Short, ScheduleV2>()
    var selectedSchedule = ZERO_ID
    this.schedule.forEach { (uuid, scheduleV1) ->
        val id = newShortId(schedulesV2.keys)
        schedulesV2[id] = scheduleV1.toV2()
        if (uuid == this.selectedSchedule) {
            // 更新选中的课程表 ID
            selectedSchedule = id
        }
    }
    return SettingsV2(
        firstLaunch = this.firstLaunch,
        themeMode = when (this.theme) {
            1 -> ThemeModeV2.LIGHT
            2 -> ThemeModeV2.DARK
            else -> ThemeModeV2.SYSTEM
        },
        themeDynamicColor = this.themeDynamicColor,
        themeColor = this.themeColor,
        schedules = schedulesV2,
        selectedScheduleID = selectedSchedule
    )
}

