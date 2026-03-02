package com.ai.assistance.operit.ui.features.ultron.roubao.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.ai.assistance.operit.ui.features.ultron.roubao.agent.MobileAgent
import com.ai.assistance.operit.ui.features.ultron.roubao.controller.AppScanner
import com.ai.assistance.operit.ui.features.ultron.roubao.controller.DeviceController
import com.ai.assistance.operit.ui.features.ultron.roubao.data.*
import com.ai.assistance.operit.ui.features.ultron.roubao.ui.screens.*
import com.ai.assistance.operit.ui.features.ultron.roubao.ui.theme.*
import com.ai.assistance.operit.ui.features.ultron.roubao.vlm.VLMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import android.util.Log
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

private const val TAG = "UltronMainApp"

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Home : Screen("home", "奥创", Icons.Outlined.Home, Icons.Filled.Home)
    object SpeechChat : Screen("speech_chat", "AI问答", Icons.Outlined.Settings, Icons.Filled.Settings)
    object Capabilities : Screen("capabilities", "能力", Icons.Outlined.Star, Icons.Filled.Star)
    object History : Screen("history", "记录", Icons.Outlined.List, Icons.Filled.List)
    object Settings : Screen("settings", "设置", Icons.Outlined.Settings, Icons.Filled.Settings)
}

/**
 * 奥创主应用 Composable
 * 从 roubao MainActivity 提取出来的可嵌入组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UltronMainApp(
    onBackPressed: () -> Unit,
    context: android.content.Context
) {
    // 初始化组件
    val deviceController = remember { DeviceController(context) }
    val settingsManager = remember { SettingsManager(context) }
    val executionRepository = remember { ExecutionRepository(context) }
    val appScanner = remember { AppScanner(context) }

    deviceController.setCacheDir(context.cacheDir)

    val mobileAgent = remember { mutableStateOf<MobileAgent?>(null) }
    var shizukuAvailable by remember { mutableStateOf(false) }
    var currentExecutionJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val executionRecords = remember { mutableStateListOf<ExecutionRecord>() }
    var isExecuting by remember { mutableStateOf(false) }
    var currentRecordId by remember { mutableStateOf<String?>(null) }
    var shouldNavigateToRecord by remember { mutableStateOf(false) }

    // 加载执行记录
    LaunchedEffect(Unit) {
        executionRecords.clear()
        executionRecords.addAll(executionRepository.getAllRecords())
    }

    // Shizuku 监听器
    val binderReceivedListener = remember {
        Shizuku.OnBinderReceivedListener {
            Log.d(TAG, "Shizuku binder received")
            shizukuAvailable = true
            if (checkShizukuPermission(context)) {
                deviceController.bindService()
            }
        }
    }

    val binderDeadListener = remember {
        Shizuku.OnBinderDeadListener {
            Log.d(TAG, "Shizuku binder dead")
            shizukuAvailable = false
        }
    }

    val permissionResultListener = remember {
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                deviceController.bindService()
                Toast.makeText(context, "Shizuku 权限已获取", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 添加 Shizuku 监听器
    LaunchedEffect(Unit) {
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
        checkAndUpdateShizukuStatus(context, deviceController) { available ->
            shizukuAvailable = available
        }
        
        // 预加载已安装应用
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            AppScanner(context).getApps()
        }
    }

    // 清理监听器
    DisposableEffect(Unit) {
        onDispose {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
            deviceController.unbindService()
        }
    }

    val settings by settingsManager.settings.collectAsState()

    BaoziTheme(themeMode = settings.themeMode) {
        val colors = BaoziTheme.colors
        
        // 处理返回键
        BackHandler(enabled = true) {
            onBackPressed()
        }

        MainAppContent(
            context = context,
            deviceController = deviceController,
            settingsManager = settingsManager,
            executionRepository = executionRepository,
            appScanner = appScanner,
            mobileAgent = mobileAgent,
            shizukuAvailable = shizukuAvailable,
            executionRecords = executionRecords,
            isExecuting = isExecuting,
            currentRecordId = currentRecordId,
            shouldNavigateToRecord = shouldNavigateToRecord,
            onIsExecutingChange = { isExecuting = it },
            onCurrentRecordIdChange = { currentRecordId = it },
            onShouldNavigateToRecordChange = { shouldNavigateToRecord = it },
            onExecutionRecordsUpdate = { executionRecords.clear(); executionRecords.addAll(it) },
            onRefreshShizuku = {
                checkAndUpdateShizukuStatus(context, deviceController) { available ->
                    shizukuAvailable = available
                }
            },
            onShizukuRequired = {
                Toast.makeText(context, "请先连接 Shizuku", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun MainAppContent(
    context: android.content.Context,
    deviceController: DeviceController,
    settingsManager: SettingsManager,
    executionRepository: ExecutionRepository,
    appScanner: AppScanner,
    mobileAgent: MutableState<MobileAgent?>,
    shizukuAvailable: Boolean,
    executionRecords: List<ExecutionRecord>,
    isExecuting: Boolean,
    currentRecordId: String?,
    shouldNavigateToRecord: Boolean,
    onIsExecutingChange: (Boolean) -> Unit,
    onCurrentRecordIdChange: (String?) -> Unit,
    onShouldNavigateToRecordChange: (Boolean) -> Unit,
    onExecutionRecordsUpdate: (List<ExecutionRecord>) -> Unit,
    onRefreshShizuku: () -> Unit,
    onShizukuRequired: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedRecord by remember { mutableStateOf<ExecutionRecord?>(null) }
    var showShizukuHelpDialog by remember { mutableStateOf(false) }
    var hasShownShizukuHelp by remember { mutableStateOf(false) }

    val settings by settingsManager.settings.collectAsState()
    val colors = BaoziTheme.colors
    val agent = mobileAgent.value
    val agentState by agent?.state?.collectAsState() ?: remember { mutableStateOf(null) }
    val logs by agent?.logs?.collectAsState() ?: remember { mutableStateOf(emptyList<String>()) }
    val records = executionRecords
    val isShizukuAvailable = shizukuAvailable && checkShizukuPermission(context)
    val executing = isExecuting
    val navigateToRecord = shouldNavigateToRecord
    val recordId = currentRecordId

    // 监听跳转事件
    LaunchedEffect(navigateToRecord, recordId) {
        if (navigateToRecord && recordId != null) {
            val record = records.find { it.id == recordId }
            if (record != null) {
                selectedRecord = record
                currentScreen = Screen.History
            }
            onShouldNavigateToRecordChange(false)
        }
    }

    // 首次进入且 Shizuku 未连接时，显示帮助引导
    LaunchedEffect(Unit) {
        if (!isShizukuAvailable && settings.hasSeenOnboarding && !hasShownShizukuHelp) {
            hasShownShizukuHelp = true
            showShizukuHelpDialog = true
        }
    }

    Scaffold(
        modifier = Modifier.background(colors.background),
        containerColor = colors.background,
        bottomBar = {
            if (selectedRecord == null) {
                NavigationBar(
                    containerColor = colors.background,
                    contentColor = colors.textPrimary,
                    tonalElevation = 0.dp
                ) {
                    listOf(Screen.Home, Screen.SpeechChat, Screen.Capabilities, Screen.History, Screen.Settings).forEach { screen ->
                        val selected = currentScreen == screen
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = { currentScreen = screen },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (colors.isDark) colors.textPrimary else Color.White,
                                selectedTextColor = colors.primary,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary,
                                indicatorColor = colors.primary
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        val layoutDirection = LocalLayoutDirection.current
        // NavigationBar 的标准高度约为 64dp，但我们不在这里添加 padding
        // 让各页面自己处理底部输入框的位置
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 0.dp, // 顶部不添加 padding，让内容贴在 Operit-main 的顶部栏上
                    start = padding.calculateStartPadding(layoutDirection),
                    end = padding.calculateEndPadding(layoutDirection),
                    bottom = padding.calculateBottomPadding() // 为 NavigationBar 留出空间
                )
        ) {
            // 处理系统返回手势
            BackHandler(enabled = selectedRecord != null) {
                selectedRecord = null
            }

            // 详情页优先显示
            if (selectedRecord != null) {
                HistoryDetailScreen(
                    record = selectedRecord!!,
                    onBack = { selectedRecord = null }
                )
            } else {
                // 主页面切换
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "screen"
                ) { screen ->
                    when (screen) {
                        Screen.Home -> {
                            HomeScreen(
                                agentState = agentState,
                                logs = logs,
                                onExecute = { instruction ->
                                    runAgent(
                                        context = context,
                                        instruction = instruction,
                                        settings = settings,
                                        mobileAgent = mobileAgent,
                                        deviceController = deviceController,
                                        appScanner = appScanner,
                                        executionRepository = executionRepository,
                                        onIsExecutingChange = onIsExecutingChange,
                                        onCurrentRecordIdChange = onCurrentRecordIdChange,
                                        onShouldNavigateToRecordChange = onShouldNavigateToRecordChange,
                                        onExecutionRecordsUpdate = onExecutionRecordsUpdate
                                    )
                                },
                                onStop = {
                                    mobileAgent.value?.stop()
                                },
                                shizukuAvailable = isShizukuAvailable,
                                currentModel = settings.model,
                                onRefreshShizuku = onRefreshShizuku,
                                onShizukuRequired = onShizukuRequired,
                                isExecuting = executing
                            )
                        }
                        Screen.SpeechChat -> {
                            SpeechChatScreen(settings = settings)
                        }
                        Screen.Capabilities -> {
                            CapabilitiesScreen(settings = settings)
                        }
                        Screen.History -> {
                            HistoryScreen(
                                records = records,
                                onRecordClick = { record -> selectedRecord = record },
                                onDeleteRecord = { id ->
                                    kotlinx.coroutines.GlobalScope.launch {
                                        executionRepository.deleteRecord(id)
                                        onExecutionRecordsUpdate(executionRepository.getAllRecords())
                                    }
                                }
                            )
                        }
                        Screen.Settings -> {
                            SettingsScreen(
                                settings = settings,
                                onUpdateApiKey = { settingsManager.updateApiKey(it) },
                                onUpdateBaseUrl = { settingsManager.updateBaseUrl(it) },
                                onUpdateModel = { settingsManager.updateModel(it) },
                                onUpdateCachedModels = { settingsManager.updateCachedModels(it) },
                                onUpdateThemeMode = { settingsManager.updateThemeMode(it) },
                                onUpdateMaxSteps = { settingsManager.updateMaxSteps(it) },
                                onUpdateCloudCrashReport = { enabled ->
                                    settingsManager.updateCloudCrashReportEnabled(enabled)
                                },
                                onUpdateRootModeEnabled = { settingsManager.updateRootModeEnabled(it) },
                                onUpdateSuCommandEnabled = { settingsManager.updateSuCommandEnabled(it) },
                                onSelectProvider = { settingsManager.selectProvider(it) },
                                shizukuAvailable = isShizukuAvailable,
                                shizukuPrivilegeLevel = if (isShizukuAvailable) {
                                    when (deviceController.getShizukuPrivilegeLevel()) {
                                        DeviceController.ShizukuPrivilegeLevel.ROOT -> "ROOT"
                                        DeviceController.ShizukuPrivilegeLevel.ADB -> "ADB"
                                        else -> "NONE"
                                    }
                                } else "NONE",
                                onFetchModels = { onSuccess, onError ->
                                    kotlinx.coroutines.GlobalScope.launch {
                                        val result = VLMClient.fetchModels(settings.baseUrl, settings.apiKey)
                                        result.onSuccess { models ->
                                            onSuccess(models)
                                        }.onFailure { error ->
                                            onError(error.message ?: "未知错误")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Shizuku 帮助对话框（需要从 roubao 复制）
    if (showShizukuHelpDialog) {
        AlertDialog(
            onDismissRequest = { showShizukuHelpDialog = false },
            title = { Text("需要 Shizuku 权限") },
            text = { Text("奥创需要 Shizuku 权限才能正常工作") },
            confirmButton = {
                TextButton(onClick = { showShizukuHelpDialog = false }) {
                    Text("知道了")
                }
            }
        )
    }
}

// 辅助函数
fun checkShizukuPermission(context: android.content.Context): Boolean {
    return try {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (e: Exception) {
        false
    }
}

fun checkAndUpdateShizukuStatus(
    context: android.content.Context,
    deviceController: DeviceController,
    onStatusChanged: (Boolean) -> Unit
) {
    try {
        val binderAlive = Shizuku.pingBinder()
        if (binderAlive) {
            val hasPermission = checkShizukuPermission(context)
            if (hasPermission) {
                deviceController.bindService()
            }
            onStatusChanged(hasPermission)
        } else {
            onStatusChanged(false)
        }
    } catch (e: Exception) {
        onStatusChanged(false)
    }
}

fun runAgent(
    context: android.content.Context,
    instruction: String,
    settings: AppSettings,
    mobileAgent: MutableState<MobileAgent?>,
    deviceController: DeviceController,
    appScanner: AppScanner,
    executionRepository: ExecutionRepository,
    onIsExecutingChange: (Boolean) -> Unit,
    onCurrentRecordIdChange: (String?) -> Unit,
    onShouldNavigateToRecordChange: (Boolean) -> Unit,
    onExecutionRecordsUpdate: (List<ExecutionRecord>) -> Unit
) {
    if (instruction.isBlank()) {
        Toast.makeText(context, "请输入指令", Toast.LENGTH_SHORT).show()
        return
    }
    if (settings.apiKey.isBlank()) {
        Toast.makeText(context, "请输入 API Key", Toast.LENGTH_SHORT).show()
        return
    }

    // 检查悬浮窗权限
    if (!Settings.canDrawOverlays(context)) {
        Toast.makeText(context, "请授予悬浮窗权限", Toast.LENGTH_LONG).show()
        val intent = android.content.Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
        return
    }

    // 立即设置执行状态为 true，显示停止按钮
    onIsExecutingChange(true)

    val vlmClient = VLMClient(
        apiKey = settings.apiKey,
        baseUrl = settings.baseUrl.ifBlank { "https://dashscope.aliyuncs.com/compatible-mode/v1" },
        model = settings.model.ifBlank { "qwen3-vl-plus" }
    )

    mobileAgent.value = MobileAgent(vlmClient, deviceController, context, appScanner)
    
    val currentJob = kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
        // 创建执行记录
        val record = ExecutionRecord(
            title = generateTitle(instruction),
            instruction = instruction,
            startTime = System.currentTimeMillis(),
            status = ExecutionStatus.RUNNING
        )

        // 保存当前记录 ID，用于停止后跳转
        onCurrentRecordIdChange(record.id)

        try {
            // 保存初始记录
            executionRepository.saveRecord(record)
            onExecutionRecordsUpdate(executionRepository.getAllRecords())

            val result = mobileAgent.value!!.runInstruction(instruction, settings.maxSteps)

            // 更新记录状态
            val agentState = mobileAgent.value?.state?.value
            val steps = agentState?.executionSteps ?: emptyList()
            val currentLogs = mobileAgent.value?.logs?.value ?: emptyList()

            val updatedRecord = record.copy(
                endTime = System.currentTimeMillis(),
                status = if (result.success) ExecutionStatus.COMPLETED else ExecutionStatus.FAILED,
                steps = steps,
                logs = currentLogs,
                resultMessage = result.message
            )
            executionRepository.saveRecord(updatedRecord)
            onExecutionRecordsUpdate(executionRepository.getAllRecords())

            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()

            // 重置执行状态
            onIsExecutingChange(false)

            // 延迟3秒后清空日志，恢复默认状态
            kotlinx.coroutines.delay(3000)
            mobileAgent.value?.clearLogs()
        } catch (e: kotlinx.coroutines.CancellationException) {
            // 用户取消任务
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                val agentState = mobileAgent.value?.state?.value
                val steps = agentState?.executionSteps ?: emptyList()
                val currentLogs = mobileAgent.value?.logs?.value ?: emptyList()

                val updatedRecord = record.copy(
                    endTime = System.currentTimeMillis(),
                    status = ExecutionStatus.STOPPED,
                    steps = steps,
                    logs = currentLogs,
                    resultMessage = "已取消"
                )
                executionRepository.saveRecord(updatedRecord)
                onExecutionRecordsUpdate(executionRepository.getAllRecords())

                // 重置执行状态
                onIsExecutingChange(false)

                Toast.makeText(context, "任务已停止", Toast.LENGTH_SHORT).show()
                mobileAgent.value?.clearLogs()

                // 触发跳转到记录详情页
                onShouldNavigateToRecordChange(true)
            }
        } catch (e: Exception) {
            // 更新失败记录
            val currentLogs = mobileAgent.value?.logs?.value ?: emptyList()
            val updatedRecord = record.copy(
                endTime = System.currentTimeMillis(),
                status = ExecutionStatus.FAILED,
                logs = currentLogs,
                resultMessage = "错误: ${e.message}"
            )
            executionRepository.saveRecord(updatedRecord)
            onExecutionRecordsUpdate(executionRepository.getAllRecords())

            // 重置执行状态
            onIsExecutingChange(false)

            Toast.makeText(context, "错误: ${e.message}", Toast.LENGTH_LONG).show()

            // 延迟3秒后清空日志，恢复默认状态
            kotlinx.coroutines.delay(3000)
            mobileAgent.value?.clearLogs()
        }
    }
    
    // 设置停止回调
    mobileAgent.value?.onStopRequested = {
        currentJob.cancel()
    }
}

fun generateTitle(instruction: String): String {
    // 生成简短标题
    val keywords = listOf(
        "打开" to "打开应用",
        "点" to "点餐",
        "发" to "发送消息",
        "看" to "浏览内容",
        "搜" to "搜索",
        "设置" to "调整设置",
        "播放" to "播放媒体"
    )
    for ((key, title) in keywords) {
        if (instruction.contains(key)) {
            return title
        }
    }
    return if (instruction.length > 10) instruction.take(10) + "..." else instruction
}

