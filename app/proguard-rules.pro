# StudyBuddy ProGuard rules

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Room entities
-keep class com.studybuddy.core.data.db.entity.** { *; }

# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}

# SLF4J (transitive dependency from Ktor) — no runtime binding needed
-dontwarn org.slf4j.**

# Whisper JNI native methods
-keepclasseswithmembernames class * { native <methods>; }
-keep class com.studybuddy.shared.whisper.** { *; }

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
