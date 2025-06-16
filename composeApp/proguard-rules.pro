-mergeinterfacesaggressively
-overloadaggressively
-repackageclasses

-keep class okio.** { *; }
-dontwarn io.ktor.network.sockets.SocketBase$attachFor$1
-dontwarn kotlin.Deprecated$Container