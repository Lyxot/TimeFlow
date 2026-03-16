-mergeinterfacesaggressively
-overloadaggressively
-repackageclasses

-keep class com.jetbrains.** { *; }
-keep class com.sun.jna.** { *; }
-keep class okio.** { *; }

# Apache HttpClient 5 optional dependencies (Brotli compression, Conscrypt TLS)
-dontwarn org.brotli.dec.**
-dontwarn org.conscrypt.**

# Ktor ContentNegotiation serialization providers (discovered via ServiceLoader)
-keep class * implements io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider { *; }