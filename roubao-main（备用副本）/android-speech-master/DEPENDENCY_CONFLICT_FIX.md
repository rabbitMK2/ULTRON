# 依赖冲突问题修复说明

## 问题描述

错误信息：
```
Cannot select module with conflict on capability 'com.google.guava:listenablefuture:1.0'
```

**原因**：
- `com.alibaba:dashscope-sdk-java:2.12.0` 依赖 `com.google.guava:guava:32.1.1-jre`
- AndroidX库（如 `androidx.appcompat:appcompat:1.6.1`）依赖 `com.google.guava:listenablefuture:1.0`
- 这两个依赖在 capability `com.google.guava:listenablefuture:1.0` 上发生冲突
- `guava:32.1.1-jre` 已经包含了 `listenablefuture` 的功能，但 AndroidX 仍然需要独立的模块

## 已实施的修复

### 1. 添加依赖解析策略

在 `app/build.gradle` 中添加了 `configurations.all` 块：
```gradle
configurations.all {
    resolutionStrategy {
        // 强制使用 Guava 32.1.1-jre（已包含 listenablefuture）
        force 'com.google.guava:guava:32.1.1-jre'
        // 排除独立的 listenablefuture 模块
        exclude group: 'com.google.guava', module: 'listenablefuture'
    }
}
```

### 2. 显式排除冲突依赖

在 DashScope SDK 依赖中排除 `listenablefuture`：
```gradle
implementation('com.alibaba:dashscope-sdk-java:2.12.0') {
    exclude group: 'com.google.guava', module: 'listenablefuture'
}
```

### 3. 显式添加 Guava

确保使用兼容的 Guava 版本：
```gradle
implementation 'com.google.guava:guava:32.1.1-jre'
```

### 4. 抑制 compileSdk 警告

在 `gradle.properties` 中添加：
```properties
android.suppressUnsupportedCompileSdk=34
```

## 为什么这样修复？

### Guava 版本说明

- **Guava 32.1.1-jre**：已经包含了 `listenablefuture` 的功能
- **listenablefuture:1.0**：独立的旧模块，已被 Guava 新版本整合

### 冲突原因

1. DashScope SDK 需要新版本的 Guava（32.1.1-jre）
2. AndroidX 库仍然依赖旧的独立 `listenablefuture` 模块
3. Gradle 无法自动解决这个冲突，因为两个模块提供了相同的 capability

### 解决方案

通过强制使用 Guava 32.1.1-jre 并排除独立的 `listenablefuture` 模块，我们：
- 满足了 DashScope SDK 的需求
- 满足了 AndroidX 库的需求（Guava 已包含所需功能）
- 避免了 capability 冲突

## 验证修复

完成修改后：

1. 在Android Studio中：
   - `File` -> `Sync Project with Gradle Files`
   - 应该不再报依赖冲突错误

2. 尝试构建APK：
   - `Build` -> `Build Bundle(s) / APK(s)` -> `Build APK(s)`
   - 应该可以成功构建

## 如果仍有问题

如果修复后仍有依赖冲突，可以尝试：

### 方案1：使用更旧的 Guava 版本（不推荐）

```gradle
implementation 'com.google.guava:guava:31.1-android'
```

### 方案2：升级 AndroidX 库

某些新版本的 AndroidX 库可能已经解决了这个问题：
```gradle
implementation 'androidx.appcompat:appcompat:1.7.0'
```

### 方案3：降级 DashScope SDK

如果必须使用旧版本：
```gradle
implementation 'com.alibaba:dashscope-sdk-java:2.11.0'
```

## 已修改的文件

- ✅ `app/build.gradle` - 添加了依赖解析策略和冲突排除
- ✅ `gradle.properties` - 添加了 compileSdk 警告抑制

现在项目应该可以正常构建APK了！

