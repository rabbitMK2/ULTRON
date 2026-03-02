# 仓库配置问题修复说明

## 问题描述

错误信息：
```
Build was configured to prefer settings repositories over project repositories 
but repository 'maven' was added by build file 'build.gradle'
```

## 原因

在 **Gradle 8.5+** 中，引入了新的仓库管理机制：

- 当 `settings.gradle` 中配置了 `dependencyResolutionManagement` 并设置 `repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)` 时
- **不允许**在 `build.gradle` 的 `allprojects` 块中定义仓库
- 所有仓库必须在 `settings.gradle` 的 `dependencyResolutionManagement` 中统一配置

## 已实施的修复

### 1. 更新 `settings.gradle`

将所有仓库配置移到 `settings.gradle`：

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 国内镜像源（优先）
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/central' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        // 备用源
        google()
        mavenCentral()
    }
}
```

### 2. 更新 `build.gradle`

- **保留** `buildscript` 中的仓库配置（用于下载Gradle插件，这是允许的）
- **移除** `allprojects` 中的仓库配置（已移到settings.gradle）

## 为什么这样配置？

### `buildscript` 仓库
- 用于下载 **Gradle插件**（如Android Gradle Plugin）
- 仍然可以在 `build.gradle` 中配置
- 已配置国内镜像源以加速下载

### `dependencyResolutionManagement` 仓库
- 用于下载 **项目依赖**（如app模块的依赖）
- 必须在 `settings.gradle` 中配置
- 已配置国内镜像源以加速下载

## 验证修复

完成修改后：

1. 在Android Studio中：
   - `File` -> `Sync Project with Gradle Files`
   - 应该不再报错

2. 检查依赖下载：
   - 应该从阿里云镜像源下载，速度更快
   - 如果镜像源失败，会自动回退到官方源

## 仓库配置说明

### 阿里云镜像源
- `repository/google` - Google仓库镜像
- `repository/central` - Maven Central镜像
- `repository/public` - 公共仓库镜像
- `repository/gradle-plugin` - Gradle插件仓库镜像

### 备用源
- `google()` - Google官方仓库
- `mavenCentral()` - Maven Central官方仓库

## 常见问题

### Q: 为什么需要两个地方配置仓库？
A: 
- `buildscript` 仓库：用于下载构建工具（Gradle插件）
- `dependencyResolutionManagement` 仓库：用于下载项目依赖

### Q: 可以移除 `allprojects` 块吗？
A: 
**可以！** 如果 `allprojects` 块中只有 `repositories`，可以完全移除。如果还有其他配置，保留块但移除 `repositories`。

### Q: 如果镜像源也慢怎么办？
A: 
- 镜像源配置了备用源（google()和mavenCentral()）
- 如果镜像源失败，会自动尝试官方源
- 也可以尝试其他镜像源（如华为云、腾讯云）

## 已修改的文件

- ✅ `settings.gradle` - 添加了国内镜像源配置
- ✅ `build.gradle` - 移除了 `allprojects` 中的仓库配置

现在项目应该可以正常同步了！

