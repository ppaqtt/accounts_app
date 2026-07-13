package com.jizhangben.app.ui.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class BudgetManager(context: Context) {
    private val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)

    val monthlyBudgetState = mutableStateOf(prefs.getFloat("monthly_budget", 0.0f).toDouble())

    var monthlyBudget: Double
        get() = monthlyBudgetState.value
        set(value) {
            monthlyBudgetState.value = value
            prefs.edit().putFloat("monthly_budget", value.toFloat()).apply()
        }

    fun getBudgetProgress(expense: Double): Pair<Double, Boolean> {
        if (monthlyBudget <= 0) return Pair(0.0, false)
        val progress = (expense / monthlyBudget).coerceIn(0.0, 1.0)
        val isOverBudget = expense > monthlyBudget
        return Pair(progress, isOverBudget)
    }
}

val LocalBudgetManager = compositionLocalOf<BudgetManager> {
    error("No BudgetManager provided")
}

@Composable
fun ProvideBudgetManager(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val budgetManager = remember { BudgetManager(context) }
    CompositionLocalProvider(LocalBudgetManager provides budgetManager) {
        content()
    }
}
