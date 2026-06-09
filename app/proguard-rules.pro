# Add project specific ProGuard rules here.
-keep class com.paperstack.** { *; }

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }

# Keep @Serializable data classes (names used by Json.encodeToString/decodeFromString)
-keep class com.paperstack.domain.model.** { *; }
