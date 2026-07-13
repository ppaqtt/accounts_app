package com.jizhangben.app.ui.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity

class BiometricHelper(private val context: Context) {
    private val prefs = context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)

    val isEnabledState = mutableStateOf(prefs.getBoolean("biometric_enabled", false))
    var isEnabled: Boolean
        get() = isEnabledState.value
        set(value) {
            isEnabledState.value = value
            prefs.edit().putBoolean("biometric_enabled", value).apply()
        }

    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("小萌记账")
            .setSubtitle("请验证身份以解锁应用")
            .setNegativeButtonText("使用密码")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
                override fun onAuthenticationFailed() {
                    onError("验证失败")
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString())
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}

val LocalBiometricHelper = compositionLocalOf<BiometricHelper> {
    error("No BiometricHelper provided")
}

@Composable
fun ProvideBiometricHelper(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val helper = remember { BiometricHelper(context) }
    CompositionLocalProvider(LocalBiometricHelper provides helper) {
        content()
    }
}
