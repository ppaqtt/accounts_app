package com.jizhangben.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jizhangben.app.data.model.RecurringTransaction
import com.jizhangben.app.data.model.TransactionType
import com.jizhangben.app.viewmodel.TransactionViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    val recurringList = viewModel.allRecurringTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("周期性账单") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加周期性账单")
            }
        }
    ) { innerPadding ->
        if (recurringList.value.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "暂无周期性账单",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击右下角按钮添加",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(recurringList.value) { item ->
                    RecurringTransactionItem(
                        item = item,
                        onToggleEnabled = { enabled ->
                            viewModel.updateRecurringTransaction(item.copy(isEnabled = enabled))
                        },
                        onDelete = {
                            viewModel.deleteRecurringTransaction(item)
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddRecurringTransactionDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onConfirm = { newRecurring ->
                viewModel.insertRecurringTransaction(newRecurring)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RecurringTransactionItem(
    item: RecurringTransaction,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isEnabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (item.type == TransactionType.EXPENSE) "支出" else "收入",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.type == TransactionType.EXPENSE)
                        Color(0xFFF44336)
                    else
                        Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "每月${item.dayOfMonth}号",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (item.note.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "¥${String.format("%.2f", item.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.type == TransactionType.EXPENSE)
                    Color(0xFFF44336)
                else
                    Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = item.isEnabled,
                onCheckedChange = onToggleEnabled
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringTransactionDialog(
    viewModel: TransactionViewModel,
    onDismiss: () -> Unit,
    onConfirm: (RecurringTransaction) -> Unit
) {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var dayOfMonth by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(-1L) }

    val allCategories = viewModel.allCategories.collectAsStateWithLifecycle(initialValue = emptyList())
    val filteredCategories = allCategories.value.filter { cat -> cat.type == selectedType }

    LaunchedEffect(filteredCategories) {
        if (filteredCategories.isNotEmpty()) {
            val found = filteredCategories.find { cat -> cat.id == selectedCategoryId }
            if (found == null) {
                selectedCategoryId = filteredCategories[0].id
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加周期性账单") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = {
                            selectedType = TransactionType.EXPENSE
                            selectedCategoryId = -1L
                        },
                        label = { Text("支出") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = {
                            selectedType = TransactionType.INCOME
                            selectedCategoryId = -1L
                        },
                        label = { Text("收入") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "选择分类",
                    style = MaterialTheme.typography.labelMedium
                )
                if (filteredCategories.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (category in filteredCategories) {
                            FilterChip(
                                selected = selectedCategoryId == category.id,
                                onClick = { selectedCategoryId = category.id },
                                label = { Text(category.name, maxLines = 1) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Text(
                        text = "暂无分类",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("金额") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = dayOfMonth,
                    onValueChange = {
                        val num = it.toIntOrNull()
                        if (it.isEmpty() || (num != null && num in 1..31)) {
                            dayOfMonth = it
                        }
                    },
                    label = { Text("每月几号（1-31）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val day = dayOfMonth.toIntOrNull() ?: 1
                    if (amount > 0 && selectedCategoryId >= 0 && day in 1..31) {
                        onConfirm(
                            RecurringTransaction(
                                amount = amount,
                                type = selectedType,
                                categoryId = selectedCategoryId,
                                dayOfMonth = day,
                                note = note,
                                isEnabled = true
                            )
                        )
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0 &&
                        selectedCategoryId >= 0 &&
                        (dayOfMonth.toIntOrNull() ?: 0) in 1..31
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}