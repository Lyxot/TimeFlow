package xyz.hyli.timeflow.proto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoOneOf

// Docs: https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/formats.md
/* Example usage
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf

val course = Course(
    name = "大学语文",
    teacher = "王婷",
    classroom = "松江SD306",
    time = TimeRangeType(
        timeRange = TimeRange(
            range = listOf(
                Range(6, 7)
            )
        )
    ),
    week = WeekRangeType(
        weekRange = WeekRange(
            range = listOf(
                Range(1, 16)
            )
        )
    )
)

val hexString = ProtoBuf.encodeToHexString(course)
val course2 = ProtoBuf.decodeFromHexString<Course>(hexString)
*/

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Range(
    @ProtoNumber(1) val start: Int,
    @ProtoNumber(2) val end: Int
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class TimeRange(
    @ProtoNumber(1) val range: List<Range>
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class TimeList(
    @ProtoNumber(1) val time: List<Int>
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class WeekRange(
    @ProtoNumber(1) val range: List<Range>
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class WeekList(
    @ProtoNumber(1) val week: List<Int>
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Course(
    @ProtoNumber(1) val name: String,
    @ProtoNumber(2) val teacher: String = "",
    @ProtoNumber(3) val classroom: String = "",
    @ProtoOneOf val time: ITimeType?,
    @ProtoOneOf val week: IWeekType?
)

@Serializable
sealed interface ITimeType

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class TimeRangeType(
    @ProtoNumber(4) val timeRange: TimeRange
) : ITimeType

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class TimeListType(
    @ProtoNumber(5) val timeList: TimeList
) : ITimeType

@Serializable
sealed interface IWeekType

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class WeekRangeType(
    @ProtoNumber(6) val weekRange: WeekRange
) : IWeekType

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class WeekListType(
    @ProtoNumber(7) val weekList: WeekList
) : IWeekType

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class DayList(
    @ProtoNumber(1) val courses: List<Course>
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Schedule(
    @ProtoNumber(1) val name: String,
    @ProtoNumber(2) val day: List<DayList>
) {
    companion object
}
