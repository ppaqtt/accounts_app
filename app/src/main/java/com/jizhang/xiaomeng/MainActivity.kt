package com.jizhang.xiaomeng

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.jizhang.xiaomeng.ui.JiZhangBenApp
import com.jizhang.xiaomeng.ui.screens.PasscodeMode
import com.jizhang.xiaomeng.ui.screens.PasscodeScreen
import com.jizhang.xiaomeng.ui.theme.JiZhangBenTheme
import com.jizhang.xiaomeng.ui.theme.ProvideThemeManager
import com.jizhang.xiaomeng.ui.theme.ThemeManager
import com.jizhang.xiaomeng.ui.utils.BiometricHelper
import com.jizhang.xiaomeng.ui.utils.ProvideAppSettingsManager
import com.jizhang.xiaomeng.ui.utils.ProvideAutoBackupManager
import com.jizhang.xiaomeng.ui.utils.ProvideBiometricHelper
import com.jizhang.xiaomeng.ui.utils.ProvideBookManager
import com.jizhang.xiaomeng.ui.utils.ProvideBudgetManager
import com.jizhang.xiaomeng.ui.utils.ProvideCategoryBudgetManager
import com.jizhang.xiaomeng.ui.utils.ProvideReminderManager
import com.jizhang.xiaomeng.ui.utils.ProvideSecurityManager
import com.jizhang.xiaomeng.ui.utils.SecurityManager
import com.jizhang.xiaomeng.viewmodel.TransactionViewModel

class MainActivity : FragmentActivity() {
    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var themeManager: ThemeManager
    private lateinit var securityManager: SecurityManager
    private lateinit var biometricHelper: BiometricHelper

    private val isUnlocked = mutableStateOf(false)
    private val openAddRequest = mutableIntStateOf(0)
    private var backgroundedAt = 0L

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeManager = ThemeManager(this)
        securityManager = SecurityManager(this)
        biometricHelper = BiometricHelper(this)
        isUnlocked.value = !securityManager.isPasscodeEnabled
        consumeIntent(intent)

        setContent {
            ProvideAppSettingsManager {
                ProvideCategoryBudgetManager {
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
                                                    val locked = !isUnlocked.value && securityManager.isPasscodeEnabled
                                                    if (locked) {
                                                        PasscodeScreen(
                                                            mode = PasscodeMode.VERIFY,
                                                            onSuccess = { isUnlocked.value = true },
                                                            onCancel = {},
                                                            onVerifyPasscode = { securityManager.verifyPasscode(it) }
                                                        )
                                                        BiometricUnlockEffect(
                                                            enabled = biometricHelper.isEnabled,
                                                            canAuthenticate = biometricHelper.canAuthenticate(),
                                                            onAuthenticate = {
                                                                biometricHelper.authenticate(
                                                                    activity = this@MainActivity,
                                                                    onSuccess = { isUnlocked.value = true },
                                                                    onError = { }
                                                                )
                                                            }
                                                        )
                                                    } else {
                                                        Surface(
                                                            modifier = Modifier.fillMaxSize(),
                                                            color = MaterialTheme.colorScheme.background
                                                        ) {
                                                            JiZhangBenApp(
                                                                viewModel = viewModel,
                                                                openAddRequest = openAddRequest.intValue,
                                                                onOpenAddHandled = { openAddRequest.intValue = 0 }
                                                            )
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

        requestNotificationPermissionIfNeeded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        // 短暂离开（例如系统文件选择器）不立刻上锁，超过 30 秒再要求重新解锁。
        if (backgroundedAt > 0 && securityManager.isPasscodeEnabled) {
            val elapsed = System.currentTimeMillis() - backgroundedAt
            if (elapsed > 30_000L) {
                isUnlocked.value = false
            }
            backgroundedAt = 0L
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            backgroundedAt = System.currentTimeMillis()
        }
    }

    private fun consumeIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("open_add", false) == true) {
            openAddRequest.intValue++
            intent.removeExtra("open_add")
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun BiometricUnlockEffect(
    enabled: Boolean,
    canAuthenticate: Boolean,
    onAuthenticate: () -> Unit
) {
    LaunchedEffect(enabled, canAuthenticate) {
        if (enabled && canAuthenticate) {
            onAuthenticate()
        }
    }
}
