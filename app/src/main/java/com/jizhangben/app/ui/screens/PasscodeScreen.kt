package com.jizhangben.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class PasscodeMode {
    VERIFY,
    SET_NEW,
    CONFIRM_NEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasscodeScreen(
    mode: PasscodeMode,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    onVerifyPasscode: (String) -> Boolean,
    onPasscodeSet: (String) -> Unit = {}
) {
    var input by remember { mutableStateOf("") }
    var tempPasscode by remember { mutableStateOf("") }
    var currentMode by remember { mutableStateOf(mode) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun onNumberClick(num: String) {
        if (input.length >= 6) return
        input += num
        errorMessage = null
        if (input.length == 6) {
            when (currentMode) {
                PasscodeMode.VERIFY -> {
                    if (onVerifyPasscode(input)) {
                        onSuccess()
                    } else {
                        errorMessage = "密码错误"
                        input = ""
                    }
                }
                PasscodeMode.SET_NEW -> {
                    tempPasscode = input
                    input = ""
                    currentMode = PasscodeMode.CONFIRM_NEW
                }
                PasscodeMode.CONFIRM_NEW -> {
                    if (input == tempPasscode) {
                        onPasscodeSet(input)
                        onSuccess()
                    } else {
                        errorMessage = "两次密码不一致"
                        input = ""
                    }
                }
            }
        }
    }

    fun onBackspace() {
        if (input.isNotEmpty()) {
            input = input.dropLast(1)
            errorMessage = null
        }
    }

    val title = when (currentMode) {
        PasscodeMode.VERIFY -> "请输入密码"
        PasscodeMode.SET_NEW -> "设置新密码"
        PasscodeMode.CONFIRM_NEW -> "再次确认密码"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mode == PasscodeMode.VERIFY) "解锁" else "密码设置") },
                navigationIcon = {
                    if (mode != PasscodeMode.VERIFY) {
                        androidx.compose.material3.IconButton(onClick = onCancel) {
                            Icon(Icons.Filled.Backspace, contentDescription = "取消")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "🐱",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = androidx.compose.ui.graphics.Color(0xFFF44336),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (currentMode == PasscodeMode.CONFIRM_NEW) {
                Text(
                    text = "请再次输入密码",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "请输入6位数字密码",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 密码显示圆点
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(6) { index ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = if (index < input.length)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 数字键盘
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "back")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                keypad.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            when {
                                key.isEmpty() -> {
                                    Spacer(modifier = Modifier.size(64.dp))
                                }
                                key == "back" -> {
                                    Card(
                                        modifier = Modifier.size(64.dp),
                                        shape = CircleShape,
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { onBackspace() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.Backspace,
                                                contentDescription = "删除",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Card(
                                        modifier = Modifier.size(64.dp),
                                        shape = CircleShape,
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { onNumberClick(key) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = key,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (mode == PasscodeMode.VERIFY) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "忘记密码？清除应用数据可重置",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
