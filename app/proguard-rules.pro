# ScrollLoom ProGuard & R8 Optimization Rules

# 1. Keep Accessibility Service
-keep public class org.scrollloom.service.LoomAccessibilityService {
    public <init>();
    protected void onServiceConnected();
    public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent);
    public void onInterrupt();
}

# 2. Keep Domain and Storage Models
-keep class org.scrollloom.domain.model.** { *; }
-keep class org.scrollloom.engine.model.** { *; }

# 3. Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# 4. Jetpack Compose
-keepclassmembers class * extends androidx.compose.ui.Modifier { *; }
-dontwarn androidx.compose.**

# 5. General AndroidX & SavedState
-dontwarn androidx.lifecycle.**
-dontwarn androidx.savedstate.**
