package com.jizhang.xiaomeng.ui.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.security.MessageDigest

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
        val stored = passcode ?: return false
        if (stored.startsWith(HASH_PREFIX)) {
            return stored == HASH_PREFIX + hash(input)
        }
        // 兼容旧版本明文密码，验证成功后立即升级为哈希存储。
        val matched = stored == input
        if (matched) {
            savePasscode(input)
        }
        return matched
    }

    fun savePasscode(newPasscode: String) {
        passcode = HASH_PREFIX + hash(newPasscode)
    }

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun removePasscode() {
        passcode = null
    }

    private companion object {
        const val HASH_PREFIX = "sha256:"
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
