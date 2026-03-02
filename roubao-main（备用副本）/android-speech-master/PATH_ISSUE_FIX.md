# 项目路径非ASCII字符问题修复

## 问题描述

错误信息：
```
Your project path contains non-ASCII characters. 
This will most likely cause the build to fail on Windows.
```

**原因**：
- 项目路径包含中文字符：`奥创\核心模块4\Duix-Mobile-main - 副本`
- Android Gradle Plugin 默认不允许项目路径包含非ASCII字符
- 这在Windows上可能导致构建失败

## 已实施的修复

在 `gradle.properties` 中添加了：
```properties
android.overridePathCheck=true
```

这会禁用路径检查，允许项目路径包含非ASCII字符。

## 重要说明

### ⚠️ 虽然可以禁用检查，但建议：

1. **最佳实践**：将项目移到纯英文路径
   - 例如：`D:\Projects\android-speech-master`
   - 避免潜在的文件编码和路径问题

2. **如果必须使用中文路径**：
   - 已添加 `android.overridePathCheck=true` 禁用检查
   - 但可能仍会遇到其他问题（如NDK构建、某些工具链等）

### 为什么Android建议使用英文路径？

1. **文件编码问题**：
   - 某些工具可能无法正确处理非ASCII字符
   - 可能导致文件路径解析错误

2. **NDK构建问题**：
   - NDK工具链对路径中的非ASCII字符支持不佳
   - 可能导致C/C++代码编译失败

3. **跨平台兼容性**：
   - 不同操作系统对非ASCII字符的处理方式不同
   - 可能导致跨平台构建问题

## 当前配置

- ✅ 已添加 `android.overridePathCheck=true`
- ✅ 项目现在可以构建（但建议移到英文路径）

## 如果遇到其他问题

如果禁用检查后仍遇到路径相关的问题，建议：

1. **移动项目到英文路径**：
   ```
   从：D:\BaiduNetdiskDownload\奥创\核心模块4\Duix-Mobile-main - 副本\APP2\android-speech-master
   到：D:\Projects\android-speech-master
   ```

2. **在Android Studio中**：
   - `File` -> `Open`
   - 选择新的项目路径
   - 重新打开项目

## 已修改的文件

- ✅ `gradle.properties` - 添加了 `android.overridePathCheck=true`

现在项目应该可以正常构建了！

