package com.jizhang.xiaomeng

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.jizhang.xiaomeng.ui.JiZhangBenApp
import com.jizhang.xiaomeng.ui.screens.PasscodeMode
import com.jizhang.xiaomeng.ui.screens.PasscodeScreen
import com.jizhang.xiaomeng.ui.theme.JiZhangBenTheme
import com.jizhang.xiaomeng.ui.theme.ProvideThemeManager
import com.jizhang.xiaomeng.ui.theme.ThemeManager
import com.jizhang.xiaomeng.ui.utils.AutoBackupManager
import com.jizhang.xiaomeng.ui.utils.BiometricHelper
import com.jizhang.xiaomeng.ui.utils.ProvideAutoBackupManager
import com.jizhang.xiaomeng.ui.utils.ProvideBiometricHelper
import com.jizhang.xiaomeng.ui.utils.ProvideBookManager
import com.jizhang.xiaomeng.ui.utils.ProvideBudgetManager
import com.jizhang.xiaomeng.ui.utils.ProvideReminderManager
import com.jizhang.xiaomeng.ui.utils.ProvideSecurityManager
import com.jizhang.xiaomeng.ui.utils.ReminderManager
import com.jizhang.xiaomeng.ui.utils.SecurityManager
import com.jizhang.xiaomeng.viewmodel.TransactionViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var themeManager: ThemeManager
    private lateinit var securityManager: SecurityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeManager = ThemeManager(this)
        securityManager = SecurityManager(this)
        setContent {
            ProvideThemeManager {
                ProvideBudgetManager {
                    ProvideSecurityManager {
                        ProvideReminderManager {
                            ProvideBookManager {
                                ProvideAutoBackupManager {
                                    ProvideBiometricHelper {
                                        val useDarkTheme = when (themeManager.themeMode) {
                                            com.jizhang.xiaomeng.ui.theme.ThemeMode.DARK -> true
                                            com.jizhang.xiaomeng.ui.theme.ThemeMode.LIGHT -> false
                                            com.jizhang.xiaomeng.ui.theme.ThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                                        }
                                        JiZhangBenTheme(
                                        darkTheme = useDarkTheme,
                                        dynamicColor = themeManager.dynamicColor
                                    ) {
                                        var showSplash by remember { mutableStateOf(true) }
                                        LaunchedEffect(Unit) {
                                            delay(1200)
                                            showSplash = false
                                        }

                                        if (showSplash) {
                                            SplashScreen()
                                        } else {
                                            val isUnlockedState = androidx.compose.runtime.remember {
                                                androidx.compose.runtime.mutableStateOf(!securityManager.isPasscodeEnabled)
                                            }

                                            if (!isUnlockedState.value && securityManager.isPasscodeEnabled) {
                                                PasscodeScreen(
                                                    mode = PasscodeMode.VERIFY,
                                                    onSuccess = { isUnlockedState.value = true },
                                                    onCancel = {},
                                                    onVerifyPasscode = { securityManager.verifyPasscode(it) }
                                                )
                                            } else {
                                                Surface(
                                                    modifier = Modifier.fillMaxSize(),
                                                    color = MaterialTheme.colorScheme.background
                                                ) {
                                                    JiZhangBenApp(viewModel = viewModel)
                                                }
                                            }
                                        }
                                    }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🐱", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "小萌记账",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "记好每一笔",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "ICP备案：浙ICP备2025194709号-8A",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
