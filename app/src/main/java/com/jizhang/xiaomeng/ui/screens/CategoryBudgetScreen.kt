package com.jizhang.xiaomeng.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jizhang.xiaomeng.data.model.Category
import com.jizhang.xiaomeng.data.model.TransactionType
import com.jizhang.xiaomeng.ui.utils.DateUtils
import com.jizhang.xiaomeng.ui.utils.IconMapper
import com.jizhang.xiaomeng.ui.utils.LocalBudgetManager
import com.jizhang.xiaomeng.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBudgetScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    val budgetManager = LocalBudgetManager.current
    val categories = viewModel.allCategories.collectAsStateWithLifecycle(initialValue = emptyList())
    val expenseCategories = categories.value.filter { it.type == TransactionType.EXPENSE }

    val summary = remember(viewModel.currentYear, viewModel.currentMonth) {
        viewModel.getExpenseCategorySummary(viewModel.currentYear, viewModel.currentMonth)
    }.observeAsState(initial = emptyList())
    val spentMap = remember(summary.value) {
        summary.value.associate { it.categoryId to it.total }
    }

    var budgets by remember { mutableStateOf<Map<Long, Double>>(emptyMap()) }
    LaunchedEffect(expenseCategories) {
        budgets = expenseCategories.associate { it.id to budgetManager.getCategoryBudget(it.id) }
    }

    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var budgetInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分类预算") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "${DateUtils.formatMonthYear(viewModel.currentYear, viewModel.currentMonth)} · 为每个分类设置月度预算",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (expenseCategories.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "暂无支出分类",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn {
                    items(expenseCategories, key = { it.id }) { category ->
                        CategoryBudgetItem(
                            category = category,
                            spent = spentMap[category.id] ?: 0.0,
                            budget = budgets[category.id] ?: 0.0,
                            onClick = {
                                editingCategory = category
                                val current = budgets[category.id] ?: 0.0
                                budgetInput = if (current > 0) current.toString() else ""
                            }
                        )
                    }
                }
            }
        }
    }

    val category = editingCategory
    if (category != null) {
        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("设置「${category.name}」预算") },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { value -> budgetInput = value.filter { it.isDigit() || it == '.' } },
                    label = { Text("月度预算金额（元）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = (budgetInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                    budgetManager.setCategoryBudget(category.id, amount)
                    budgets = budgets + (category.id to amount)
                    editingCategory = null
                }) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun CategoryBudgetItem(category: Category, spent: Double, budget: Double, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = IconMapper.getIcon(category.icon),
                        contentDescription = category.name,
                        tint = Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (budget > 0) {
                        Text(
                            text = "¥${String.format("%.0f", spent)}/¥${String.format("%.0f", budget)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (spent > budget) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "未设置预算",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "设置预算",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (budget > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (spent / budget).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = if (spent > budget) Color(0xFFF44336) else Color(0xFF4CAF50),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}
