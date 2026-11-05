package com.jizhang.xiaomeng.ui.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject

class CategoryBudgetManager(context: Context) {
    private val prefs = context.getSharedPreferences("category_budget_prefs", Context.MODE_PRIVATE)

    val budgetsState = mutableStateOf<Map<Long, Double>>(loadBudgets())

    fun budgetFor(categoryId: Long): Double = budgetsState.value[categoryId] ?: 0.0

    fun setBudget(categoryId: Long, amount: Double) {
        val newBudgets = budgetsState.value.toMutableMap()
        if (amount > 0) {
            newBudgets[categoryId] = amount
        } else {
            newBudgets.remove(categoryId)
        }
        budgetsState.value = newBudgets
        saveBudgets(newBudgets)
    }

    private fun loadBudgets(): Map<Long, Double> {
        val raw = prefs.getString("budgets", null) ?: return emptyMap()
        return try {
            val json = JSONObject(raw)
            val result = mutableMapOf<Long, Double>()
            json.keys().forEach { key ->
                key.toLongOrNull()?.let { result[it] = json.optDouble(key, 0.0) }
            }
            result
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveBudgets(budgets: Map<Long, Double>) {
        val json = JSONObject()
        budgets.forEach { (id, amount) -> json.put(id.toString(), amount) }
        prefs.edit().putString("budgets", json.toString()).apply()
    }
}

val LocalCategoryBudgetManager = compositionLocalOf<CategoryBudgetManager> {
    error("No CategoryBudgetManager provided")
}

@Composable
fun ProvideCategoryBudgetManager(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val manager = remember { CategoryBudgetManager(context) }
    CompositionLocalProvider(LocalCategoryBudgetManager provides manager) {
        content()
    }
}
