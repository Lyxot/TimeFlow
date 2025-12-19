/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.data.v2

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber


@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Lesson(
    @ProtoNumber(1) val start: Time,
    @ProtoNumber(2) val end: Time,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class LessonTimePeriodInfo(
    @ProtoNumber(1) val morning: List<Lesson>,
    @ProtoNumber(2) val afternoon: List<Lesson>,
    @ProtoNumber(3) val evening: List<Lesson>
) {
    companion object {
        fun fromPeriodCounts(
            morningCount: Int = 5,
            afternoonCount: Int = 5,
            eveningCount: Int = 4,
            lessonDuration: Int = 40,
            breakDuration: Int = 10,
            morningStart: Time = Time(8, 0),
            afternoonStart: Time = Time(13, 0),
            eveningStart: Time = Time(18, 0)
        ): LessonTimePeriodInfo {
            val start = listOf(morningCount, afternoonCount, eveningCount).indexOfFirst { it > 0 }
            var time = when (start) {
                0 -> morningStart
                1 -> afternoonStart
                2 -> eveningStart
                else -> Time(0, 0) // Default case, should not happen
            }
            val morning = if (morningCount == 0) {
                emptyList()
            } else {
                generateLessons(
                    morningCount,
                    if (time > morningStart) time else morningStart,
                    lessonDuration,
                    breakDuration
                )
            }
            time = if (morningCount > 0) {
                morning.last().end.addMinutes(breakDuration)
            } else {
                afternoonStart
            }

            val afternoon = if (afternoonCount == 0) {
                emptyList()
            } else {
                generateLessons(
                    afternoonCount,
                    if (time > afternoonStart) time else afternoonStart,
                    lessonDuration,
                    breakDuration
                )
            }
            time = if (afternoonCount > 0) {
                afternoon.last().end.addMinutes(breakDuration)
            } else {
                eveningStart
            }

            val evening = if (eveningCount == 0) {
                emptyList()
            } else {
                generateLessons(
                    eveningCount,
                    if (time > eveningStart) time else eveningStart,
                    lessonDuration,
                    breakDuration
                )
            }

            return LessonTimePeriodInfo(
                morning = morning,
                afternoon = afternoon,
                evening = evening
            )
        }

        val defaultLessonTimePeriodInfo = fromPeriodCounts()

        fun generateLessons(
            count: Int,
            startTime: Time,
            lessonDuration: Int,
            breakDuration: Int
        ): List<Lesson> {
            return List(count) { index ->
                val lessonStart = startTime.addMinutes(index * (lessonDuration + breakDuration))
                Lesson(
                    start = lessonStart,
                    end = lessonStart.addMinutes(lessonDuration)
                )
            }
        }
    }

    val totalLessonsCount: Int = morning.size + afternoon.size + evening.size
    val lessons: List<Lesson> = morning + afternoon + evening

    fun getLessonByIndex(index: Int): Lesson {
        return when (index) {
            in 1..morning.size -> morning[index - 1]
            in (morning.size + 1)..(morning.size + afternoon.size) -> afternoon[index - morning.size - 1]
            in (morning.size + afternoon.size + 1)..totalLessonsCount -> evening[index - morning.size - afternoon.size - 1]
            else -> throw IndexOutOfBoundsException("Lesson index out of range")
        }
    }

    val conflictSet: Set<Int> = (1 until lessons.size)
        .filter { i -> lessons[i].start < lessons[i - 1].end }
        .flatMap { i -> listOf(i - 1, i) }
        .toSet()
}