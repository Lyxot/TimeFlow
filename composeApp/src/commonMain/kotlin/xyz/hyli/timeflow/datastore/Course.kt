package xyz.hyli.timeflow.datastore

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

// Docs: https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/formats.md
/* Example usage
val course = Course(
    name = "大学语文",
    teacher = "王婷",
    classroom = "松江SD306",
    time = Range(6, 7),
    week = WeekRangeType(
        weekRange = WeekRange(
            range = listOf(
                Range(1, 16)
            )
        )
    )
)

val hexString = ProtoBuf.encodeToHexString(course)
val course = ProtoBuf.decodeFromHexString<Course>(hexString)
*/

@OptIn(ExperimentalSerializationApi::class)
@Serializable
enum class Weekday {
    @ProtoNumber(1)
    MONDAY,
    @ProtoNumber(2)
    TUESDAY,
    @ProtoNumber(3)
    WEDNESDAY,
    @ProtoNumber(4)
    THURSDAY,
    @ProtoNumber(5)
    FRIDAY,
    @ProtoNumber(6)
    SATURDAY,
    @ProtoNumber(7)
    SUNDAY,
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
enum class WeekDescriptionEnum {
    @ProtoNumber(1)
    ALL,
    @ProtoNumber(2)
    ODD,
    @ProtoNumber(3)
    EVEN
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Range(
    @ProtoNumber(1) val start: Int,
    @ProtoNumber(2) val end: Int
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
) {
    constructor(weekDescription: WeekDescriptionEnum, totalWeeks: Int) : this(
        week = when (weekDescription) {
            WeekDescriptionEnum.ALL -> (1..totalWeeks).toList()
            WeekDescriptionEnum.ODD -> (1..totalWeeks step 2).toList()
            WeekDescriptionEnum.EVEN -> (2..totalWeeks step 2).toList()
        }
    )

    constructor(weekRange: WeekRange) : this(
        week = weekRange.range.flatMap { it.start..it.end }.distinct()
    )
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Course(
    @ProtoNumber(1) val name: String,
    @ProtoNumber(2) val teacher: String = "",
    @ProtoNumber(3) val classroom: String = "",
    @ProtoNumber(4) val time: Range,
    @ProtoNumber(5) val weekday: Weekday,
    @ProtoNumber(6) val week: WeekList
)