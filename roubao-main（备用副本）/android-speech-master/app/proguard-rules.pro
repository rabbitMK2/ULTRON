# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep DashScope SDK classes
-keep class com.alibaba.dashscope.** { *; }
-dontwarn com.alibaba.dashscope.**

# Keep Speech library classes
-keep class net.gotev.speech.** { *; }
-dontwarn net.gotev.speech.**

