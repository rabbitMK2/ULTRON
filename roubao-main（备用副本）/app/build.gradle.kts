plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // 注释掉 Firebase 相关插件，避免要求 google-services.json
    // id("com.google.gms.google-services")
    // id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.roubao.autopilot"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.roubao.autopilot"
        minSdk = 26
        targetSdk = 34
        versionCode = 7
        versionName = "1.4.2"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        aidl = false // 已用手写 Java Stub 替代 AIDL
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

configurations.all {
    resolutionStrategy {
        // 解决 Guava 和 listenablefuture 的冲突
        // guava 32.1.1-android 已经包含了 listenablefuture 的功能
        force("com.google.guava:guava:32.1.1-android")
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Security (Encrypted SharedPreferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // OkHttp for API calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Shizuku
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")

    // JSON
    implementation("org.json:json:20231013")

    // Android Speech Recognition
    implementation("net.gotev:speech:1.6.2")

    // Alibaba DashScope SDK
    implementation("com.alibaba:dashscope-sdk-java:2.12.0") {
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
    // 使用 Android 版本的 Guava
    implementation("com.google.guava:guava:32.1.1-android")

    // Firebase Crashlytics（调试阶段先去掉，避免 google-services.json 依赖）
    // implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    // implementation("com.google.firebase:firebase-crashlytics")
    // implementation("com.google.firebase:firebase-analytics")

    // Debug
    implementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
