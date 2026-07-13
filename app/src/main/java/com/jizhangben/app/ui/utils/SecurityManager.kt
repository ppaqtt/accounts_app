package com.jizhangben.app.ui.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class SecurityManager(context: Context) {
    private val prefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    val passcodeState = mutableStateOf(prefs.getString("passcode", null))

    var passcode: String?
        get() = passcodeState.value
        set(value) {
            passcodeState.value = value
            prefs.edit().putString("passcode", value).apply()
        }

    val isPasscodeEnabled: Boolean
        get() = !passcode.isNullOrEmpty()

    fun verifyPasscode(input: String): Boolean {
        return input == passcode
    }

    fun savePasscode(newPasscode: String) {
        passcode = newPasscode
    }

    fun removePasscode() {
        passcode = null
    }
}

val LocalSecurityManager = compositionLocalOf<SecurityManager> {
    error("No SecurityManager provided")
}

@Composable
fun ProvideSecurityManager(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val securityManager = remember { SecurityManager(context) }
    CompositionLocalProvider(LocalSecurityManager provides securityManager) {
        content()
    }
}
