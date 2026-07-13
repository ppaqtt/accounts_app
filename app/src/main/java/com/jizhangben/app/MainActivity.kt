package com.jizhangben.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jizhangben.app.ui.JiZhangBenApp
import com.jizhangben.app.ui.screens.PasscodeMode
import com.jizhangben.app.ui.screens.PasscodeScreen
import com.jizhangben.app.ui.theme.JiZhangBenTheme
import com.jizhangben.app.ui.theme.ProvideThemeManager
import com.jizhangben.app.ui.theme.ThemeManager
import com.jizhangben.app.ui.utils.AutoBackupManager
import com.jizhangben.app.ui.utils.BiometricHelper
import com.jizhangben.app.ui.utils.ProvideAutoBackupManager
import com.jizhangben.app.ui.utils.ProvideBiometricHelper
import com.jizhangben.app.ui.utils.ProvideBookManager
import com.jizhangben.app.ui.utils.ProvideBudgetManager
import com.jizhangben.app.ui.utils.ProvideReminderManager
import com.jizhangben.app.ui.utils.ProvideSecurityManager
import com.jizhangben.app.ui.utils.ReminderManager
import com.jizhangben.app.ui.utils.SecurityManager
import com.jizhangben.app.viewmodel.TransactionViewModel

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
                                            com.jizhangben.app.ui.theme.ThemeMode.DARK -> true
                                            com.jizhangben.app.ui.theme.ThemeMode.LIGHT -> false
                                            com.jizhangben.app.ui.theme.ThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
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
