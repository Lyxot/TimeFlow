package xyz.hyli.timeflow.datastore

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.supportDynamicColor

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Settings(
    val initialized: Boolean = true, // Whether the settings have been initialized, always true
    @ProtoNumber(1) val firstLaunch: Int = 0, // 0: Not launched, Version Code: Last launched version code
    @ProtoNumber(2) val theme: Int = 0, // 0: System, 1: Light, 2: Dark
    @ProtoNumber(3) val themeDynamicColor: Boolean = currentPlatform().supportDynamicColor(), // Whether dynamic color is enabled, only applicable for Android 12 and above
    @ProtoNumber(4) val themeColor: Long = 0xFFCBDDEE, // Default seed color in ARGB format
    @ProtoNumber(5) val schedule: Map<String, Schedule> = emptyMap(), // A map of schedules, where the key is the schedule name and the value is the Schedule object
    @ProtoNumber(6) val selectedSchedule: String = "" // The name of the selected schedule, empty if no schedule is selected
)