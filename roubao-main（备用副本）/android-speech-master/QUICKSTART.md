# 快速开始指南

## 前置要求

1. **Android Studio**：建议使用最新版本的Android Studio
2. **JDK**：JDK 8或更高版本
3. **Android设备或模拟器**：Android 7.0 (API 24) 或更高版本
4. **网络连接**：用于调用阿里云API

## 安装步骤

### 1. 打开项目

1. 启动Android Studio
2. 选择 `File` -> `Open`
3. 选择项目目录：`android-speech-master`
4. 等待Gradle同步完成（首次可能需要几分钟）

### 2. 配置API密钥（可选）

API密钥已在代码中配置，如需修改：
- 打开 `app/src/main/java/com/example/speechapp/MainActivity.java`
- 修改以下常量：
  ```java
  private static final String DASHSCOPE_API_KEY = "your-api-key";
  private static final String APP_ID = "your-app-id";
  ```

### 3. 运行应用

1. 连接Android设备或启动模拟器
2. 点击工具栏上的运行按钮（绿色三角形）或按 `Shift+F10`
3. 选择目标设备
4. 等待应用安装和启动

### 4. 授予权限

首次运行时，应用会请求以下权限：
- **录音权限**：必须授予，否则无法使用语音识别
- **网络权限**：必须授予，否则无法调用API

## 使用应用

### 基本流程

1. **启动识别**：点击"开始语音识别"按钮
2. **说话**：对着设备清晰地说出你的问题
3. **查看结果**：
   - 识别结果会显示在"识别结果"区域
   - AI回复会显示在"AI回复"区域
4. **听回复**：AI回复会自动通过语音播放

### 界面说明

- **状态栏**：显示当前操作状态（监听中、识别中、调用AI、播放中等）
- **识别结果**：显示语音识别的文本结果
- **AI回复**：显示阿里云智能体的回复内容
- **开始按钮**：开始语音识别
- **停止按钮**：停止当前识别（仅在识别过程中可用）

## 常见问题

### Q: 语音识别不工作？
A: 
- 检查是否授予了录音权限
- 确认设备支持语音识别功能
- 检查网络连接（某些设备需要网络进行语音识别）

### Q: API调用失败？
A:
- 检查网络连接
- 验证API Key和App ID是否正确
- 查看Android Studio的Logcat查看详细错误信息

### Q: TTS无法播放？
A:
- 检查设备音量设置
- 确认设备支持中文TTS引擎
- 尝试重启应用

### Q: Gradle同步失败？
A:
- 检查网络连接（需要下载依赖）
- 尝试 `File` -> `Invalidate Caches / Restart`
- 检查 `build.gradle` 文件中的依赖版本

## 开发调试

### 查看日志

在Android Studio中：
1. 打开 `View` -> `Tool Windows` -> `Logcat`
2. 过滤标签：`MainActivity` 或 `SpeechApp`

### 测试API

可以直接在代码中测试API调用，查看 `callAliyunAgent()` 方法。

## 下一步

- 添加对话历史功能
- 实现连续对话（上下文记忆）
- 优化UI界面
- 添加更多交互功能

