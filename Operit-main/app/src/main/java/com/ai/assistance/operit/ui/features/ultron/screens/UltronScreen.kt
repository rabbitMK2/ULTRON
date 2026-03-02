package com.ai.assistance.operit.ui.features.ultron.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ai.assistance.operit.ui.features.ultron.roubao.ui.UltronMainApp

/**
 * 奥创主界面
 * 集成 roubao-main 的完整功能
 */
@Composable
fun UltronScreen(
    onGoBack: () -> Unit
) {
    val context = LocalContext.current
    
    UltronMainApp(
        onBackPressed = onGoBack,
        context = context
    )
}

