# 奥创（Roubao）集成指南

## 当前状态

已完成的集成工作：
1. ✅ 侧边栏菜单项已删除（创作激励活动、使用手册、关于、更新历史）
2. ✅ 添加了"奥创"菜单项到工具分组
3. ✅ 创建了主题文件 (`roubao/ui/theme/Theme.kt`)
4. ✅ 创建了数据模型（`SettingsManager.kt`, `ExecutionHistory.kt`）

## 待完成的文件复制

由于 roubao-main 包含大量文件（约30+），需要复制以下目录的所有文件：

### 1. UI Screens (必需)
- `ui/screens/HomeScreen.kt`
- `ui/screens/SettingsScreen.kt`
- `ui/screens/HistoryScreen.kt`
- `ui/screens/CapabilitiesScreen.kt`
- `ui/screens/SpeechChatScreen.kt`
- `ui/screens/OnboardingScreen.kt`

### 2. 核心业务逻辑 (必需)
- `agent/MobileAgent.kt`
- `agent/Executor.kt`
- `agent/Manager.kt`
- `agent/ActionReflector.kt`
- `agent/ConversationMemory.kt`
- `agent/InfoPool.kt`
- `agent/Notetaker.kt`
- `controller/DeviceController.kt`
- `controller/AppScanner.kt`
- `vlm/VLMClient.kt`

### 3. 工具类 (必需)
- `tools/Tool.kt`
- `tools/ToolManager.kt`
- `tools/ShellTool.kt`
- `tools/OpenAppTool.kt`
- `tools/ClipboardTool.kt`
- `tools/DeepLinkTool.kt`
- `tools/HttpTool.kt`
- `tools/SearchAppsTool.kt`
- `skills/Skill.kt`
- `skills/SkillManager.kt`
- `skills/SkillRegistry.kt`

### 4. 其他文件
- `service/ShellService.kt`
- `service/IShellService.java`
- `ui/OverlayService.kt`
- `utils/CrashHandler.kt`
- `App.kt` (需要适配)

## 复制步骤

1. 从 `D:\BaiduNetdiskDownload\ULTRON\roubao-main（备用副本）\app\src\main\java\com\roubao\autopilot\`
2. 复制到 `app\src\main\java\com\ai\assistance\operit\ui\features\ultron\roubao\`
3. 修改所有文件的包名从 `com.roubao.autopilot` 到 `com.ai.assistance.operit.ui.features.ultron.roubao`

## 修改说明

所有文件复制后需要：
1. 修改包名声明
2. 更新导入语句
3. 检查依赖关系（特别是 AndroidManifest 中的权限和组件声明）

## 快速集成脚本（可选）

可以创建一个 Python 脚本来自动复制和修改包名，但由于路径包含中文，可能需要使用绝对路径或复制到临时目录后再处理。

## 当前简化版本

目前 `UltronScreen.kt` 是一个占位实现，完整集成后需要替换为 roubao 的 MainApp Composable。





