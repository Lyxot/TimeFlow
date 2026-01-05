/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
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
    /** 课程开始时间 */
    @ProtoNumber(1) val start: Time,
    /** 课程结束时间 */
    @ProtoNumber(2) val end: Time,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class LessonTimePeriodInfo(
    /** 上午的课程列表 */
    @ProtoNumber(1) val morning: List<Lesson>,
    /** 下午的课程列表 */
    @ProtoNumber(2) val afternoon: List<Lesson>,
    /** 晚上的课程列表 */
    @ProtoNumber(3) val evening: List<Lesson>
) {
    companion object {
        /**
         * 根据早、中、晚的课程节数以及时间参数，工厂方法创建 [LessonTimePeriodInfo] 实例。
         *
         * @param morningCount 上午的课程节数。
         * @param afternoonCount 下午的课程节数。
         * @param eveningCount 晚上的课程节数。
         * @param lessonDuration 每节课的持续时间（分钟）。
         * @param breakDuration 课间休息时间（分钟）。
         * @param morningStart 上午第一节课的开始时间。
         * @param afternoonStart 下午第一节课的开始时间。
         * @param eveningStart 晚上第一节课的开始时间。
         * @return 一个新的 [LessonTimePeriodInfo] 实例。
         */
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

        /**
         * 根据指定的参数生成一个课程时间列表。
         * @param count 要生成的课程数量。
         * @param startTime 第一节课的开始时间。
         * @param lessonDuration 每节课的持续时间（分钟）。
         * @param breakDuration 课间休息时间（分钟）。
         * @return 生成的 [Lesson] 列表。
         */
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

    /** 一天中的总课程节数 */
    val totalLessonsCount: Int = morning.size + afternoon.size + evening.size

    /** 按早、中、晚顺序合并的总课程列表 */
    val lessons: List<Lesson> = morning + afternoon + evening

    /**
     * 根据 1-based 的索引获取对应的课程时间。
     * 例如，索引 1 对应上午第一节课。
     *
     * @param index 课程的 1-based 索引。
     * @return 对应的 [Lesson] 对象。
     * @throws IndexOutOfBoundsException 如果索引超出了总课程数的范围。
     */
    fun getLessonByIndex(index: Int): Lesson {
        return when (index) {
            in 1..morning.size -> morning[index - 1]
            in (morning.size + 1)..(morning.size + afternoon.size) -> afternoon[index - morning.size - 1]
            in (morning.size + afternoon.size + 1)..totalLessonsCount -> evening[index - morning.size - afternoon.size - 1]
            else -> throw IndexOutOfBoundsException("Lesson index out of range")
        }
    }

    /**
     * 存在时间冲突的课程索引集合。
     * 如果第 n 节课的开始时间早于第 n-1 节课的结束时间，则认为存在冲突。
     */
    val conflictSet: Set<Int> = (1 until lessons.size)
        .filter { i -> lessons[i].start < lessons[i - 1].end }
        .flatMap { i -> listOf(i - 1, i) }
        .toSet()
}