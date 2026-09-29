# ScrollLoom ProGuard & R8 Optimization Rules

# 1. Keep Accessibility Service & Foreground Capture Service
-keep public class org.scrollloom.service.LoomAccessibilityService { *; }
-keep public class org.scrollloom.service.capture.LoomMediaProjectionService { *; }

# 2. Keep Domain and Storage Models
-keep class org.scrollloom.domain.model.** { *; }
-keep class org.scrollloom.engine.model.** { *; }

# 3. Kotlin Coroutines Internal Mechanics
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
