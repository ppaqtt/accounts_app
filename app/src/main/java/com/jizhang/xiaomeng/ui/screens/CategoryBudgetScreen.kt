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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jizhang.xiaomeng.data.model.Category
import com.jizhang.xiaomeng.data.model.TransactionType
import com.jizhang.xiaomeng.ui.utils.DateUtils
import com.jizhang.xiaomeng.ui.utils.IconMapper
import com.jizhang.xiaomeng.ui.utils.LocalCategoryBudgetManager
import com.jizhang.xiaomeng.ui.utils.LocalBookManager
import com.jizhang.xiaomeng.viewmodel.TransactionViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBudgetScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    val book = LocalBookManager.current.currentBook
    val budgetManager = LocalCategoryBudgetManager.current
    val calendar = remember { Calendar.getInstance() }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)

    val categories = viewModel.allCategories.collectAsStateWithLifecycle(initialValue = emptyList())
    val expenseCategories = categories.value.filter { it.type == TransactionType.EXPENSE }
    val summary = remember(book, year, month) {
        viewModel.getExpenseCategorySummary(book, year, month)
    }.observeAsState(initial = emptyList())

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
                text = "${DateUtils.formatMonthYear(year, month)} · 当前账本「$book」",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "点击分类设置月度预算，留空或填 0 可取消",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn {
                    items(expenseCategories) { category ->
                        val spent = summary.value.firstOrNull { it.categoryId == category.id }?.total ?: 0.0
                        val budget = budgetManager.budgetFor(category.id)
                        CategoryBudgetItem(
                            category = category,
                            spent = spent,
                            budget = budget,
                            onClick = {
                                editingCategory = category
                                budgetInput = if (budget > 0) budget.toInt().toString() else ""
                            }
                        )
                    }
                }
            }
        }
    }

    editingCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("${category.name} 月度预算") },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { value -> budgetInput = value.filter { it.isDigit() || it == '.' } },
                    label = { Text("预算金额（元）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    budgetManager.setBudget(category.id, budgetInput.toDoubleOrNull() ?: 0.0)
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
fun CategoryBudgetItem(
    category: Category,
    spent: Double,
    budget: Double,
    onClick: () -> Unit = {}
) {
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
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
