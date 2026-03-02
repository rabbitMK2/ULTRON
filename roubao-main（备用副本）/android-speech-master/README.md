# 语音交互助手 Android应用

这是一个基于gotev android-speech库开发的语音交互应用，集成了阿里云DashScope智能体API，实现了完整的语音识别->AI对话->语音合成的闭环流程。

## 功能特性

- ✅ 语音识别（ASR）：使用gotev android-speech库封装Android原生语音识别API
- ✅ AI对话：集成阿里云DashScope智能体，实现智能对话
- ✅ 语音合成（TTS）：使用Android原生TextToSpeech API
- ✅ 简洁的UI界面：方便测试和调试

## 技术栈

- **语音识别**：net.gotev:speech:1.6.2
- **AI服务**：com.alibaba:dashscope-sdk-java:2.12.0
- **语音合成**：Android原生TextToSpeech API
- **开发框架**：Android SDK + Java

## 项目结构

```
android-speech-master/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/speechapp/
│   │       │   └── MainActivity.java    # 主Activity，包含所有核心逻辑
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml  # 主界面布局
│   │       │   └── values/
│   │       │       └── strings.xml        # 字符串资源
│   │       └── AndroidManifest.xml        # 应用清单文件
│   └── build.gradle                       # 应用级构建配置
├── build.gradle                           # 项目级构建配置
├── settings.gradle                        # 项目设置
└── README.md                              # 本文件
```

## 配置说明

### API配置

在 `MainActivity.java` 中已配置：
- **API Key**: `sk-a185dda2406b42b7832babe480c61316`
- **App ID**: `8b4d6a9575c0428eab5289fa41b5aab1`

如需修改，请编辑 `MainActivity.java` 中的常量：
```java
private static final String DASHSCOPE_API_KEY = "your-api-key";
private static final String APP_ID = "your-app-id";
```

### 权限要求

应用需要以下权限：
- `RECORD_AUDIO`：用于语音识别
- `INTERNET`：用于调用阿里云API

这些权限已在 `AndroidManifest.xml` 中声明，应用启动时会自动请求。

## 使用说明

### 1. 构建项目

使用Android Studio打开项目，等待Gradle同步完成。

### 2. 运行应用

1. 连接Android设备或启动模拟器
2. 点击运行按钮
3. 首次运行时会请求录音权限，请授予权限

### 3. 使用流程

1. 点击"开始语音识别"按钮
2. 对着设备说话
3. 系统会自动识别语音并显示识别结果
4. 识别完成后自动调用阿里云智能体获取回复
5. AI回复会显示在界面上，并自动通过TTS播放

## 工作流程

```
用户说话 
  ↓
ASR识别（gotev android-speech）
  ↓
显示识别结果
  ↓
调用阿里云DashScope智能体API
  ↓
获取AI回复
  ↓
显示AI回复
  ↓
TTS语音合成播放
  ↓
完成
```

## 依赖说明

### 核心依赖

1. **net.gotev:speech:1.6.2**
   - 轻量级Android语音识别库
   - 封装了Android原生SpeechRecognizer API
   - 简化了语音识别的调用流程

2. **com.alibaba:dashscope-sdk-java:2.12.0**
   - 阿里云DashScope Java SDK
   - 用于调用智能体API

3. **Android原生TextToSpeech**
   - 系统自带的语音合成引擎
   - 支持中文语音合成

## 注意事项

1. **网络连接**：需要稳定的网络连接以调用阿里云API
2. **语音识别**：需要清晰的语音输入，建议在安静环境中使用
3. **TTS语言**：默认设置为中文，如需其他语言请修改代码
4. **API限制**：请注意阿里云API的调用频率限制

## 故障排除

### 语音识别不可用
- 检查设备是否支持语音识别
- 确认已授予录音权限
- 检查网络连接（某些设备需要网络进行语音识别）

### API调用失败
- 检查网络连接
- 验证API Key和App ID是否正确
- 查看控制台错误日志

### TTS无法播放
- 检查设备是否支持中文TTS
- 确认TTS引擎已正确初始化

## 开发建议

如需扩展功能，可以考虑：
- 添加对话历史记录
- 实现连续对话（上下文记忆）
- 添加更多UI交互元素
- 优化错误处理和用户提示
- 添加语音唤醒功能

## 许可证

本项目基于开源库开发，请遵循相关开源许可证。
