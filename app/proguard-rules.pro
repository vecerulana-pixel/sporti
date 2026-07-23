# Retrofit DTO fields are read through Gson reflection.
-keep class com.sporti.core.data.remote.dto.** { *; }
-keepattributes Signature

# Preserve methods exposed to web pages through the clipboard JavaScript bridge.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
