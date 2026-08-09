package com.jizhang.xiaomeng

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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
