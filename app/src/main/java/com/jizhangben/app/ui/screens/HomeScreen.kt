package com.jizhangben.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import com.jizhangben.app.data.model.Category
import com.jizhangben.app.data.model.Transaction
import com.jizhangben.app.data.model.TransactionType
import com.jizhangben.app.ui.utils.DateUtils
import com.jizhangben.app.ui.utils.IconMapper
import com.jizhangben.app.ui.utils.LocalBookManager
import com.jizhangben.app.ui.utils.LocalBudgetManager
import com.jizhangben.app.viewmodel.TransactionViewModel
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(viewModel: TransactionViewModel, navController: NavController) {
    var currentYear by remember { mutableIntStateOf(viewModel.currentYear) }
    var currentMonth by remember { mutableIntStateOf(viewModel.currentMonth) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableLongStateOf(-1L) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var showFilter by remember { mutableStateOf(false) }
    var minAmount by remember { mutableStateOf("") }
    var maxAmount by remember { mutableStateOf("") }

    var multiSelectMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    val monthTransactions = viewModel.getTransactionsByMonth(currentYear, currentMonth).observeAsState(initial = emptyList())
    val monthIncome = viewModel.getTotalIncomeByMonth(currentYear, currentMonth).observeAsState(initial = 0.0)
    val monthExpense = viewModel.getTotalExpenseByMonth(currentYear, currentMonth).observeAsState(initial = 0.0)
    val categoryList = viewModel.allCategories.collectAsStateWithLifecycle(initialValue = emptyList())

    val filteredTransactions = remember(monthTransactions.value, searchQuery, showFilter, filterType, minAmount, maxAmount) {
        var result = monthTransactions.value
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            result = result.filter { transaction ->
                transaction.note.lowercase().contains(query) ||
                        categoryList.value.find { it.id == transaction.categoryId }?.name?.lowercase()?.contains(query) == true
            }
        }
        if (showFilter) {
            result = result.filter { it.type == filterType }
            val min = minAmount.toDoubleOrNull()
            val max = maxAmount.toDoubleOrNull()
            if (min != null) {
                result = result.filter { it.amount >= min }
            }
            if (max != null) {
                result = result.filter { it.amount <= max }
            }
        }
        result.sortedByDescending { it.date }
    }

    val groupedTransactions = groupTransactionsByDate(filteredTransactions)

    fun onDeleteClick(transactionId: Long) {
        transactionToDelete = transactionId
        showDeleteDialog = true
    }

    fun confirmDelete() {
        if (transactionToDelete != -1L) {
            val transaction = monthTransactions.value.find { it.id == transactionToDelete }
            transaction?.let { viewModel.deleteTransaction(it) }
        }
        showDeleteDialog = false
        transactionToDelete = -1L
    }

    fun onEditClick(_transaction: Transaction) {
        navController.navigate("add")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val bookManager = LocalBookManager.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📒 ${bookManager.currentBook}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        MonthSelector(
            year = currentYear,
            month = currentMonth,
            onPrevious = {
                if (currentMonth == 0) {
                    currentMonth = 11
                    currentYear--
                } else {
                    currentMonth--
                }
            },
            onNext = {
                if (currentMonth == 11) {
                    currentMonth = 0
                    currentYear++
                } else {
                    currentMonth++
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SummaryCard(
            income = monthIncome.value ?: 0.0,
            expense = monthExpense.value ?: 0.0
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "近期账单",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row {
                if (multiSelectMode) {
                    IconButton(onClick = {
                        val allIds = filteredTransactions.map { it.id }.toSet()
                        selectedIds = if (selectedIds == allIds) emptySet() else allIds
                    }) {
                        Icon(
                            Icons.Filled.SelectAll,
                            contentDescription = "全选",
                            tint = if (selectedIds == filteredTransactions.map { it.id }.toSet()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        multiSelectMode = false
                        selectedIds = emptySet()
                    }) {
                        Text("取消", color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Filled.Search, contentDescription = "搜索")
                    }
                    IconButton(onClick = { showFilter = !showFilter }) {
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = "筛选",
                            tint = if (showFilter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        multiSelectMode = true
                        selectedIds = emptySet()
                    }) {
                        Icon(Icons.Filled.Checklist, contentDescription = "多选")
                    }
                }
            }
        }

        if (showSearch) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索备注或分类...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        if (showFilter) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("收支类型", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = filterType == TransactionType.EXPENSE,
                            onClick = { filterType = TransactionType.EXPENSE },
                            label = { Text("支出") }
                        )
                        FilterChip(
                            selected = filterType == TransactionType.INCOME,
                            onClick = { filterType = TransactionType.INCOME },
                            label = { Text("收入") }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("金额范围", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minAmount,
                            onValueChange = { minAmount = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("最低") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = maxAmount,
                            onValueChange = { maxAmount = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("最高") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredTransactions.isEmpty()) {
            EmptyState(if (searchQuery.isNotBlank() || showFilter) "没有找到匹配的账单" else "暂无账单记录")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                groupedTransactions.forEach { (date, dayTransactions) ->
                    item {
                        DayHeader(date = date, transactions = dayTransactions, _categories = categoryList.value)
                    }
                    items(dayTransactions) { transaction ->
                        val category = categoryList.value.find { it.id == transaction.categoryId }
                        TransactionItem(
                            transaction = transaction,
                            category = category,
                            multiSelectMode = multiSelectMode,
                            isSelected = selectedIds.contains(transaction.id),
                            onEditClick = { onEditClick(transaction) },
                            onDeleteClick = { onDeleteClick(transaction.id) },
                            onSelect = {
                                selectedIds = if (selectedIds.contains(transaction.id)) {
                                    selectedIds - transaction.id
                                } else {
                                    selectedIds + transaction.id
                                }
                            },
                            onLongClick = {
                                if (!multiSelectMode) {
                                    multiSelectMode = true
                                    selectedIds = setOf(transaction.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗？") },
            confirmButton = {
                TextButton(onClick = ::confirmDelete) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showBatchDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = { Text("确认批量删除") },
            text = { Text("确定要删除选中的 ${selectedIds.size} 条记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransactionsByIds(selectedIds.toList())
                    selectedIds = emptySet()
                    multiSelectMode = false
                    showBatchDeleteDialog = false
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (multiSelectMode && selectedIds.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "已选择 ${selectedIds.size} 项",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(onClick = { showBatchDeleteDialog = true }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "批量删除",
                            tint = Color(0xFFF44336)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthSelector(year: Int, month: Int, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "上月")
        }
        Text(
            text = DateUtils.formatMonthYear(year, month),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "下月")
        }
    }
}

@Composable
fun SummaryCard(income: Double, expense: Double) {
    val balance = income - expense
    val budgetManager = LocalBudgetManager.current
    val budget = budgetManager.monthlyBudget
    val (budgetProgress, isOverBudget) = budgetManager.getBudgetProgress(expense)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "本月结余",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "¥${String.format("%.2f", balance)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "收入",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "¥${String.format("%.2f", income)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "支出",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "¥${String.format("%.2f", expense)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF44336)
                    )
                }
            }
            if (budget > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "本月预算",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { budgetProgress.toFloat() },
                        modifier = Modifier.weight(1f).height(8.dp),
                        color = if (isOverBudget) Color(0xFFF44336) else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¥${String.format("%.0f", expense)}/¥${String.format("%.0f", budget)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverBudget) Color(0xFFF44336) else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun DayHeader(date: Date, transactions: List<Transaction>, _categories: List<Category>) {
    val dayIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val dayExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = DateUtils.formatDate(date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = DateUtils.getDayOfWeek(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                if (dayIncome > 0) {
                    Text(
                        text = "收 ¥${String.format("%.2f", dayIncome)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (dayExpense > 0) {
                    Text(
                        text = "支 ¥${String.format("%.2f", dayExpense)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    category: Category?,
    multiSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onSelect: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .then(
                if (multiSelectMode) {
                    Modifier.combinedClickable(
                        onClick = onSelect,
                        onLongClick = onLongClick
                    )
                } else {
                    Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = onLongClick
                    )
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (multiSelectMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .then(
                    if (category != null) {
                        Modifier
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier.size(40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (transaction.type == TransactionType.INCOME)
                        Color(0xFFE8F5E9)
                    else
                        Color(0xFFFFEBEE)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconMapper.getIcon(category?.icon ?: "more_horiz"),
                        contentDescription = category?.name,
                        tint = if (transaction.type == TransactionType.INCOME)
                            Color(0xFF4CAF50)
                        else
                            Color(0xFFF44336),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category?.name ?: "未知分类",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (transaction.note.isNotEmpty()) {
                Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
        if (!multiSelectMode) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "删除",
                        tint = Color(0xFFF44336)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = (if (transaction.type == TransactionType.INCOME) "+" else "-") +
                    "¥${String.format("%.2f", transaction.amount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (transaction.type == TransactionType.INCOME)
                Color(0xFF4CAF50)
            else
                Color(0xFFF44336)
        )
    }
}

@Composable
fun EmptyState(message: String = "暂无账单记录") {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📝",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (message == "暂无账单记录") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击下方「记账」开始记录吧",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun groupTransactionsByDate(transactions: List<Transaction>): Map<Date, List<Transaction>> {
    val calendar = Calendar.getInstance()
    return transactions.groupBy { transaction ->
        calendar.time = transaction.date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.time
    }
}
