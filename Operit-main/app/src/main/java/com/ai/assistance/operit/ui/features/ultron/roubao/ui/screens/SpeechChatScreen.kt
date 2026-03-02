package com.ai.assistance.operit.ui.features.ultron.roubao.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.view.MotionEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import com.ai.assistance.operit.R
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ai.assistance.operit.ui.features.ultron.roubao.data.AppSettings
import com.ai.assistance.operit.ui.features.ultron.roubao.ui.theme.BaoziTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// import net.gotev.speech.Speech
// import net.gotev.speech.SpeechDelegate
// import net.gotev.speech.SpeechRecognitionNotAvailable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 语音聊天消息数据类
 */
private data class SpeechChatMessage(
    val message: String,
    val sentBy: String
) {
    companion object {
        const val SENT_BY_ME = "me"
        const val SENT_BY_BOT = "bot"
    }
}

/**
 * AI 语音问答页面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SpeechChatScreen(
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = BaoziTheme.colors
    val scope = rememberCoroutineScope()

    // 消息列表
    var messages by remember { mutableStateOf<List<SpeechChatMessage>>(emptyList()) }
    
    // 状态文本
    var statusText by remember { mutableStateOf("状态：就绪") }
    
    // 识别结果
    var recognizedText by remember { mutableStateOf("识别结果：") }
    
    // AI回复
    var responseText by remember { mutableStateOf("AI回复：") }
    
    // 是否正在按住按钮
    var isHoldingButton by remember { mutableStateOf(false) }
    
    // 是否正在监听
    var isListening by remember { mutableStateOf(false) }
    
    // 识别缓冲区
    val recognizedBuffer = remember { StringBuilder() }
    
    // 语音RMS阈值
    val VOICE_RMS_THRESHOLD = 2.5f
    val SILENCE_COMMA_DELAY_MS = 650L
    var lastVoiceTime by remember { mutableStateOf(0L) }
    var silenceCommaAdded by remember { mutableStateOf(false) }
    
    // TTS
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    
    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            statusText = "状态：需要录音权限"
        }
    }
    
    // 初始化 Speech
    LaunchedEffect(Unit) {
        // Speech.init(context, context.packageName)
        // Speech.getInstance().setStopListeningAfterInactivity(24 * 60 * 60 * 1000L)
        // Speech.getInstance().setTransitionMinimumDelay(0)
        // TODO: Speech library not available, using placeholder
        
        // 初始化 TTS
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.CHINESE)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    statusText = "状态：TTS语言不支持"
                }
            } else {
                statusText = "状态：TTS初始化失败"
            }
        }
        
        // 检查权限
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            if (isListening) {
                // Speech.getInstance().stopListening()
            }
            // Speech.getInstance().shutdown()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }
    
    // 更新识别文本显示
    val updateRecognizedText: () -> Unit = {
        recognizedText = "识别结果：${recognizedBuffer.toString()}"
    }
    
    // 添加逗号（如果需要）
    val appendCommaIfNeeded: () -> Unit = appendCommaIfNeeded@{
        if (recognizedBuffer.isEmpty()) return@appendCommaIfNeeded
        val lastChar = recognizedBuffer.last()
        if (lastChar != '，' && lastChar != ',') {
            recognizedBuffer.append('，')
            updateRecognizedText()
        }
    }
    
    // 应用部分识别结果
    val applyPartialResult: (String) -> Unit = applyPartialResult@{ text ->
        if (text.isEmpty()) return@applyPartialResult
        var merged = text
        if (recognizedBuffer.isNotEmpty() && recognizedBuffer.last() == '，') {
            if (!text.endsWith("，") && !text.endsWith(",")) {
                merged = text + "，"
            }
        }
        recognizedBuffer.setLength(0)
        recognizedBuffer.append(merged)
        updateRecognizedText()
    }
    
    // 添加结果（带逗号）
    val appendResultWithComma: (String) -> Unit = appendResultWithComma@{ text ->
        if (text.isEmpty()) return@appendResultWithComma
        if (recognizedBuffer.isNotEmpty()) {
            appendCommaIfNeeded()
        }
        recognizedBuffer.append(text)
        appendCommaIfNeeded()
        updateRecognizedText()
    }
    
    // 检查TTS状态
    lateinit var checkTtsStatus: (TextToSpeech) -> Unit
    checkTtsStatus = { tts ->
        scope.launch {
            if (!tts.isSpeaking) {
                statusText = "状态：播放完成"
            } else {
                kotlinx.coroutines.delay(500)
                checkTtsStatus(tts)
            }
        }
    }
    
    // 播放文本
    lateinit var speakText: (String) -> Unit
    speakText = { text ->
        textToSpeech?.let { tts ->
            val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            if (result == TextToSpeech.SUCCESS) {
                statusText = "状态：正在播放语音..."
                scope.launch {
                    kotlinx.coroutines.delay(500)
                    if (!tts.isSpeaking) {
                        statusText = "状态：播放完成"
                    } else {
                        checkTtsStatus(tts)
                    }
                }
            } else {
                statusText = "状态：语音播放失败"
            }
        }
    }
    
    // 调用阿里云智能体API（使用兼容模式的聊天API）
    lateinit var callAliyunAgent: (String) -> Unit
    callAliyunAgent = callAliyunAgent@{ userInput ->
        if (userInput.trim().isEmpty()) {
            val fallback = recognizedBuffer.toString()
            if (fallback.trim().isNotEmpty()) {
                callAliyunAgent(fallback)
            } else {
                statusText = "状态：识别结果为空"
            }
            return@callAliyunAgent
        }
        
        val prompt = userInput.trim()
        
        // 添加用户消息到聊天列表
        messages = messages + SpeechChatMessage(prompt, SpeechChatMessage.SENT_BY_ME)
        
        scope.launch(Dispatchers.IO) {
            try {
                val apiKey = settings.apiKey.ifBlank { 
                    withContext(Dispatchers.Main) {
                        statusText = "状态：请先配置 API Key"
                    }
                    return@launch
                }
                
                val baseUrl = settings.baseUrl.ifBlank { 
                    "https://dashscope.aliyuncs.com/compatible-mode/v1" 
                }
                val model = settings.model.ifBlank { 
                    "qwen-turbo" 
                }
                
                // 使用兼容模式的聊天API
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()
                
                val messagesArray = JSONArray()
                val userMessage = JSONObject()
                userMessage.put("role", "user")
                userMessage.put("content", prompt)
                messagesArray.put(userMessage)
                
                val requestBody = JSONObject()
                requestBody.put("model", model)
                requestBody.put("messages", messagesArray)
                requestBody.put("stream", false)
                
                val request = Request.Builder()
                    .url("$baseUrl/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()
                
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                
                if (response.isSuccessful) {
                    val json = JSONObject(responseBody)
                    val choices = json.getJSONArray("choices")
                    if (choices.length() > 0) {
                        val choice = choices.getJSONObject(0)
                        val message = choice.getJSONObject("message")
                        val aiResponse = message.getString("content")
                        
                        withContext(Dispatchers.Main) {
                            responseText = "AI回复：$aiResponse"
                            statusText = "状态：回复准备播放..."
                            
                            // 添加AI回复到聊天列表
                            messages = messages + SpeechChatMessage(aiResponse, SpeechChatMessage.SENT_BY_BOT)
                            
                            // 播放语音
                            speakText(aiResponse)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            statusText = "状态：API返回空结果"
                        }
                    }
                } else {
                    val errorMsg = "API调用失败：${response.code} - $responseBody"
                    withContext(Dispatchers.Main) {
                        responseText = errorMsg
                        statusText = "状态：错误"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMsg = "错误：${e.message}"
                    responseText = errorMsg
                    statusText = "状态：错误"
                }
            }
        }
    }
    
    // 开始语音识别
    lateinit var startSpeechRecognition: (Boolean) -> Unit
    startSpeechRecognition = startSpeechRecognition@{ resetBuffer ->
        if (isListening) return@startSpeechRecognition
        isListening = true
        
        if (resetBuffer) {
            recognizedBuffer.setLength(0)
        }
        silenceCommaAdded = false
        lastVoiceTime = System.currentTimeMillis()
        updateRecognizedText()
        statusText = "状态：按住说话中..."
        
        try {
            // Speech library not available - placeholder code
            statusText = "状态：语音识别功能暂不可用（需要 gotev Speech 库）"
            isListening = false
            /*
            Speech.getInstance().startListening(object : SpeechDelegate {
                override fun onStartOfSpeech() {
                    statusText = "状态：开始说话..."
                }
                
                override fun onSpeechRmsChanged(value: Float) {
                    scope.launch {
                        val now = System.currentTimeMillis()
                        if (value > VOICE_RMS_THRESHOLD) {
                            lastVoiceTime = now
                            silenceCommaAdded = false
                        } else if (!silenceCommaAdded && (now - lastVoiceTime) >= SILENCE_COMMA_DELAY_MS) {
                            appendCommaIfNeeded()
                            silenceCommaAdded = true
                            lastVoiceTime = now
                        }
                    }
                }
                
                override fun onSpeechPartialResults(results: List<String>?) {
                    if (results != null && results.isNotEmpty()) {
                        val text = results[0]
                        scope.launch {
                            applyPartialResult(text)
                        }
                    }
                }
                
                override fun onSpeechResult(result: String?) {
                    scope.launch {
                        if (isHoldingButton) {
                            appendResultWithComma(result ?: "")
                            isListening = false
                            startSpeechRecognition(false)
                        } else {
                            val finalResult = if (result != null && result.trim().isNotEmpty()) {
                                result
                            } else {
                                recognizedBuffer.toString()
                            }
                            recognizedBuffer.setLength(0)
                            recognizedBuffer.append(finalResult)
                            updateRecognizedText()
                            isListening = false
                            statusText = "状态：识别完成，正在调用AI..."
                            callAliyunAgent(finalResult)
                        }
                    }
                }
            })
            */
        } catch (e: Exception) {
            statusText = "状态：启动语音识别失败：${e.message}"
            isListening = false
        }
    }
    
    // 停止语音识别
    val stopSpeechRecognition: () -> Unit = {
        if (isListening) {
            // Speech.getInstance().stopListening()
            isListening = false
            statusText = "状态：已松开，等待结果..."
        }
    }
    
    val listState = rememberLazyListState()
    
    // 自动滚动到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // 可滚动的内容区域
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(bottom = 100.dp) // 为底部按钮留出空间
        ) {
            // 顶部标题栏（与其他页面风格一致）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI问答",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Text(
                            text = "语音交互助手，支持语音识别和AI对话",
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            messages = emptyList()
                            recognizedBuffer.setLength(0)
                            recognizedText = "识别结果："
                            responseText = "AI回复："
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清空",
                            tint = colors.textSecondary
                        )
                    }
                }
            }
            
            // 状态栏
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.backgroundCard
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                }
            }
            
            // 聊天消息列表（改为普通Column以支持滚动）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                messages.forEach { message ->
                    ChatMessageItem(
                        message = message,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 显示识别结果和AI回复（如果没有消息）
                if (messages.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE0E0E0)
                        )
                    ) {
                        Text(
                            text = recognizedText,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
                        )
                    ) {
                        Text(
                            text = responseText,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
        
        // 固定在底部的按钮（紧贴NavigationBar上方）
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 0.dp) // NavigationBar的padding已经在UltronMainApp中处理
                .imePadding()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.backgroundCard,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(if (isHoldingButton || isListening) colors.error else colors.primary)
                            .pointerInteropFilter { event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN -> {
                                        isHoldingButton = true
                                        startSpeechRecognition(true)
                                        true
                                    }
                                    MotionEvent.ACTION_UP,
                                    MotionEvent.ACTION_CANCEL -> {
                                        isHoldingButton = false
                                        stopSpeechRecognition()
                                        true
                                    }
                                    else -> false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Image placeholder - resource not available
                            Text("🎤", fontSize = 20.sp, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHoldingButton || isListening) "正在录音，松开结束" else "按住说话，松开结束",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 聊天消息项
 */
@Composable
private fun ChatMessageItem(
    message: SpeechChatMessage,
    modifier: Modifier = Modifier
) {
    val colors = BaoziTheme.colors
    val isMe = message.sentBy == SpeechChatMessage.SENT_BY_ME
    
    Row(
        modifier = modifier,
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = if (isMe) 40.dp else 0.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) Color(0xFF7CB342) else Color(0xFF1E88E5)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = message.message,
                modifier = Modifier.padding(12.dp),
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

