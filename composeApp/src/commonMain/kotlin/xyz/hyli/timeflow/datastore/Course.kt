package xyz.hyli.timeflow.datastore

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoOneOf

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
data class WeekDescription(
    @ProtoNumber(1) val description: WeekDescriptionEnum = WeekDescriptionEnum.ALL
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
    @ProtoNumber(4) val time: Range,
    @ProtoOneOf val week: IWeekType?
)

@Serializable
sealed interface IWeekType

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class WeekDescriptionType(
    @ProtoNumber(5) val weekDescription: WeekDescription
) : IWeekType

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