package com.jizhang.xiaomeng.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jizhang.xiaomeng.data.model.Category
import com.jizhang.xiaomeng.data.model.RecurringTransaction
import com.jizhang.xiaomeng.data.model.TransactionType
import com.jizhang.xiaomeng.ui.utils.IconMapper
import com.jizhang.xiaomeng.viewmodel.TransactionViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.livedata.observeAsState
import java.util.Calendar
import java.util.Locale

private val EXPENSE_RED = Color(0xFFF44336)
private val INCOME_GREEN = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    val recurringList = viewModel.allRecurringTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val allCategories = viewModel.allCategories.collectAsStateWithLifecycle(initialValue = emptyList())
    val monthExpense = viewModel.getTotalExpenseByMonth(viewModel.currentYear, viewModel.currentMonth)
        .observeAsState(initial = 0.0)

    var showAddDialog by remember { mutableStateOf(false) }

    val enabledExpenseList = recurringList.value.filter { it.isEnabled && it.type == TransactionType.EXPENSE }
    val monthlyFixedExpense = enabledExpenseList.sumOf { it.amount }
    val monthExpenseValue = monthExpense.value ?: 0.0
    val fixedRatio = if (monthExpenseValue > 0) monthlyFixedExpense / monthExpenseValue else 0.0
    val nextItem = enabledExpenseList.minByOrNull { daysUntil(it.dayOfMonth) }
    val categoryMap = allCategories.value.associateBy { it.id }

    val grouped = enabledExpenseList
        .groupBy { bucketOf(categoryMap[it.categoryId]?.name ?: "") }
        .toList()
        .sortedBy { listOf("会员订阅", "居住租房", "还款账单", "其他固定").indexOf(it.first) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("固定支出") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
                Icon(Icons.Filled.Add, contentDescription = "添加固定支出")
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
                    Text("🧾", fontSize = 44.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "暂无固定支出",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "添加会员、房租、还款，提前知道每月的固定开销",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                item {
                    FixedExpenseOverview(
                        monthlyFixedExpense = monthlyFixedExpense,
                        fixedRatio = fixedRatio,
                        monthExpense = monthExpenseValue,
                        nextItem = nextItem,
                        categoryMap = categoryMap
                    )
                }

                if (grouped.isEmpty()) {
                    item {
                        Text(
                            text = "所有固定支出已停用",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }

                grouped.forEach { (bucket, list) ->
                    item {
                        Text(
                            text = "$bucket · ${list.size} 项",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }
                    items(list, key = { it.id }) { item ->
                        RecurringTransactionItem(
                            item = item,
                            category = categoryMap[item.categoryId],
                            onToggleEnabled = { enabled ->
                                viewModel.updateRecurringTransaction(item.copy(isEnabled = enabled))
                            },
                            onDelete = { viewModel.deleteRecurringTransaction(item) }
                        )
                    }
                }

                val disabled = recurringList.value.filter { !it.isEnabled || it.type == TransactionType.INCOME }
                if (disabled.isNotEmpty()) {
                    item {
                        Text(
                            text = "已停用 / 收入",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }
                    items(disabled, key = { "off_${it.id}" }) { item ->
                        RecurringTransactionItem(
                            item = item,
                            category = categoryMap[item.categoryId],
                            onToggleEnabled = { enabled ->
                                viewModel.updateRecurringTransaction(item.copy(isEnabled = enabled))
                            },
                            onDelete = { viewModel.deleteRecurringTransaction(item) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(72.dp)) }
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
private fun FixedExpenseOverview(
    monthlyFixedExpense: Double,
    fixedRatio: Double,
    monthExpense: Double,
    nextItem: RecurringTransaction?,
    categoryMap: Map<Long, Category>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "每月固定支出",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = money(monthlyFixedExpense),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = fixedRatio.toFloat().coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (monthExpense > 0)
                    "占本月支出 ${percent(fixedRatio * 100)}%（本月已支出 ${money(monthExpense)}）"
                else
                    "本月还没有支出记录，固定支出占 0%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            if (nextItem != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))
                val days = daysUntil(nextItem.dayOfMonth)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "下次扣款",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${categoryMap[nextItem.categoryId]?.name ?: "固定支出"} ${money(nextItem.amount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = when (days) {
                            0 -> "今天"
                            1 -> "明天"
                            else -> "${days} 天后"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun RecurringTransactionItem(
    item: RecurringTransaction,
    category: Category?,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val isExpense = item.type == TransactionType.EXPENSE
    val accent = if (isExpense) EXPENSE_RED else INCOME_GREEN
    val days = daysUntil(item.dayOfMonth)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconMapper.getIcon(category?.icon ?: "more_horiz"),
                    contentDescription = category?.name,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category?.name ?: (if (isExpense) "支出" else "收入"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (item.isEnabled) {
                        "每月${item.dayOfMonth}号 · " + when (days) {
                            0 -> "今天扣款"
                            1 -> "明天扣款"
                            else -> "还有 $days 天"
                        }
                    } else {
                        "已停用 · 每月${item.dayOfMonth}号"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.note.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = (if (isExpense) "-" else "+") + money(item.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Spacer(modifier = Modifier.width(4.dp))
            Switch(
                checked = item.isEnabled,
                onCheckedChange = onToggleEnabled
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
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
        title = { Text("添加固定支出") },
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

/** 距离本月（或下月）第 day 天还有多少天，0 表示就是今天。 */
private fun daysUntil(dayOfMonth: Int): Int {
    val today = Calendar.getInstance()
    val target = (today.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
        set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(maxDay))
    }
    val startOfToday = (today.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (target.before(startOfToday)) {
        target.add(Calendar.MONTH, 1)
        val maxDay = target.getActualMaximum(Calendar.DAY_OF_MONTH)
        target.set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(maxDay))
    }
    val diff = target.timeInMillis - startOfToday.timeInMillis
    return (diff / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(0)
}

private fun bucketOf(categoryName: String): String = when {
    categoryName.contains("会员") || categoryName.contains("订阅") ||
            categoryName.contains("视频") || categoryName.contains("音乐") -> "会员订阅"
    categoryName.contains("房租") || categoryName.contains("物业") ||
            categoryName.contains("水") || categoryName.contains("电") ||
            categoryName.contains("燃气") || categoryName.contains("宽带") -> "居住租房"
    categoryName.contains("还款") || categoryName.contains("贷款") ||
            categoryName.contains("信用卡") || categoryName.contains("借款") -> "还款账单"
    else -> "其他固定"
}

private fun money(value: Double): String =
    "¥" + String.format(Locale.CHINA, "%,.2f", value)

private fun percent(value: Double): String =
    String.format(Locale.CHINA, "%.0f", value)
