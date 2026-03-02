package com.ai.assistance.operit.ui.features.ultron.roubao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.operit.ui.features.ultron.roubao.data.ApiProvider
import com.ai.assistance.operit.ui.features.ultron.roubao.data.AppSettings
import com.ai.assistance.operit.ui.features.ultron.roubao.ui.theme.BaoziTheme
import com.ai.assistance.operit.ui.features.ultron.roubao.vlm.VLMClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

/**
 * 能力页面：
 * - 底部导航「能力」对应
 * - 顶部导航栏保持不变，仅替换内容区域为两个子页：
 *   - 「AI问答」：通用问答
 *   - 「更多功能」：显示更多功能页面
 */
@Composable
fun CapabilitiesScreen(settings: AppSettings) {
    val colors = BaoziTheme.colors
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("AI问答", "更多功能")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // 顶部标题（保留原有导航样式）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column {
                Text(
                    text = "能力",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Text(
                    text = "语音 / 文本 AI 问答与更多功能",
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            }
        }

        // 顶部 Tab：AI问答 / 更多功能
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = colors.background,
            contentColor = colors.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) colors.primary else colors.textSecondary
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> AiQaScreen(settings = settings)
            1 -> InfoQueryScreen(settings = settings)
        }
    }
}

private data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String
)

/**
 * 公用：检查是否已配置阿里云 API Key（Aliyun + apiKey 非空）
 */
@Composable
private fun AliyunGuard(
    settings: AppSettings,
    content: @Composable () -> Unit
) {
    val colors = BaoziTheme.colors
    val isAliyun = settings.currentProviderId == ApiProvider.ALIYUN.id
    val hasKey = settings.apiKey.isNotBlank()

    if (!isAliyun || !hasKey) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "当前功能暂不可用",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "请在「设置 > API 配置」中选择「阿里云 (Qwen-VL)」并填写 API Key。",
                    fontSize = 14.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        content()
    }
}

/**
 * 「AI问答」页面：通用问答功能
 */
@Composable
private fun AiQaScreen(settings: AppSettings) {
    AliyunGuard(settings = settings) {
        val colors = BaoziTheme.colors
        val scope = rememberCoroutineScope()
        var input by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(false) }
        val messages = remember { mutableStateListOf<ChatMessage>() }

        val vlmClient = remember(settings.apiKey, settings.baseUrl, settings.model) {
            VLMClient(
                apiKey = settings.apiKey,
                baseUrl = settings.baseUrl,
                model = settings.model
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            // 可滚动的内容区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(16.dp)
                    .padding(bottom = 100.dp) // 为底部输入区留出空间
            ) {
                // 聊天列表
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    messages.forEach { msg ->
                        val isUser = msg.role == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isUser) colors.primary else colors.backgroundCard,
                                tonalElevation = if (isUser) 2.dp else 0.dp
                            ) {
                                Text(
                                    text = msg.content,
                                    modifier = Modifier.padding(10.dp),
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else colors.textPrimary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // 固定在底部的输入区（紧贴NavigationBar上方）
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 0.dp) // NavigationBar的padding已经在UltronMainApp中处理
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp),
                        placeholder = { Text("请输入要咨询的问题...") },
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledIconButton(
                        onClick = {
                            val question = input.trim()
                            if (question.isEmpty() || loading) return@FilledIconButton
                            input = ""
                            messages.add(ChatMessage("user", question))
                            loading = true
                            scope.launch {
                                val result = vlmClient.predict(prompt = question)
                                loading = false
                                result.onSuccess { reply ->
                                    messages.add(ChatMessage("assistant", reply))
                                }.onFailure { e ->
                                    messages.add(
                                        ChatMessage(
                                            "assistant",
                                            "查询失败：${e.message ?: "未知错误"}"
                                        )
                                    )
                                }
                            }
                        },
                        enabled = !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "发送")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 「更多功能」页面：显示更多功能网站
 */
@Composable
private fun InfoQueryScreen(settings: AppSettings) {
    val colors = BaoziTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // 显示网站内容
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    val webSettings: android.webkit.WebSettings = this@apply.settings
                    webSettings.javaScriptEnabled = true
                    webSettings.domStorageEnabled = true
                    webSettings.setSupportZoom(true)
                    webSettings.builtInZoomControls = true
                    webSettings.displayZoomControls = false
                    // 启用滚动
                    isVerticalScrollBarEnabled = true
                    isHorizontalScrollBarEnabled = true
                    // 确保可以滚动
                    overScrollMode = android.view.View.OVER_SCROLL_ALWAYS
                    // 启用嵌套滚动（如果父容器支持）
                    isNestedScrollingEnabled = true
                    // 确保 WebView 可以滚动
                    scrollBarStyle = android.view.View.SCROLLBARS_INSIDE_OVERLAY
                    loadUrl("https://space.coze.cn/")
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
