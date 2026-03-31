-mergeinterfacesaggressively
-overloadaggressively
-repackageclasses

-keep class com.jetbrains.** { *; }
-keep class okio.** { *; }

# Apache HttpClient 5 optional dependencies (Brotli compression, Conscrypt TLS)
-dontwarn org.brotli.dec.**
-dontwarn org.conscrypt.**

# Ktor ContentNegotiation serialization providers (discovered via ServiceLoader)
-keep class * implements io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider { *; }

# Required on JVM for JNA-based integrations.
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }

# Required when using FileKit Dialogs on Linux (XDG Desktop Portal / DBus).
-keep class org.freedesktop.dbus.** { *; }
-keep class io.github.vinceglb.filekit.dialogs.platform.xdg.** { *; }
-keepattributes Signature,InnerClasses,RuntimeVisibleAnnotations