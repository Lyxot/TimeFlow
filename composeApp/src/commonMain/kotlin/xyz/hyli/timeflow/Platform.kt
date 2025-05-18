package xyz.hyli.timeflow

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform