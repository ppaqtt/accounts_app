package com.jizhang.xiaomeng.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jizhang.xiaomeng.data.model.TransactionType
import com.jizhang.xiaomeng.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnualReportScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    var year by remember { mutableIntStateOf(viewModel.currentYear) }
    val transactions = viewModel.allTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val categories = viewModel.allCategories.collectAsStateWithLifecycle(initialValue = emptyList())

    val yearTransactions = remember(transactions.value, year) {
        transactions.value.filter {
            val cal = Calendar.getInstance().apply { time = it.date }
            cal.get(Calendar.YEAR) == year && it.type != TransactionType.TRANSFER
        }
    }
    val income = yearTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expense = yearTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val balance = income - expense

    val monthlyExpense = remember(yearTransactions) {
        (0..11).map { month ->
            yearTransactions.filter {
                val cal = Calendar.getInstance().apply { time = it.date }
                cal.get(Calendar.MONTH) == month && it.type == TransactionType.EXPENSE
            }.sumOf { it.amount }
        }
    }
    val monthlyIncome = remember(yearTransactions) {
        (0..11).map { month ->
            yearTransactions.filter {
                val cal = Calendar.getInstance().apply { time = it.date }
                cal.get(Calendar.MONTH) == month && it.type == TransactionType.INCOME
            }.sumOf { it.amount }
        }
    }

    val topCategories = remember(yearTransactions, categories.value) {
        yearTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .map { (categoryId, list) ->
                val category = categories.value.find { it.id == categoryId }
                (category?.name ?: "其他") to list.sumOf { it.amount }
            }
            .sortedByDescending { it.second }
            .take(5)
    }

    val dailyExpense = remember(yearTransactions) {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        yearTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { format.format(it.date) }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }
    val maxDailyExpense = dailyExpense.values.maxOrNull() ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("年度账单") },
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
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { year-- }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "上一年")
                }
                Text(
                    text = "${year}年",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { year++ }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "下一年")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "年度结余",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "¥${String.format("%.2f", balance)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("总收入", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "¥${String.format("%.2f", income)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("总支出", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "¥${String.format("%.2f", expense)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF44336)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("月度支出趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (expense <= 0.0) {
                        Text("暂无数据", color = Color.Gray)
                    } else {
                        BarChart(
                            data = monthlyExpense.mapIndexed { index, amount ->
                                "${index + 1}月" to amount.toFloat()
                            },
                            barColor = Color(0xFFF44336)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        monthlyExpense.forEachIndexed { index, amount ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${index + 1}月",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(40.dp)
                                )
                                Text(
                                    text = "收 ¥${String.format("%.0f", monthlyIncome[index])}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4CAF50)
                                )
                                Text(
                                    text = "支 ¥${String.format("%.0f", amount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF44336)
                                )
                            }
                            if (index < monthlyExpense.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("支出 Top 分类", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (topCategories.isEmpty() || expense <= 0.0) {
                        Text("暂无数据", color = Color.Gray)
                    } else {
                        val pieColors = listOf(
                            Color(0xFF4CAF50), Color(0xFFF44336), Color(0xFF2196F3),
                            Color(0xFFFF9800), Color(0xFF9C27B0)
                        )
                        val pieData = topCategories.map { (name, amount) ->
                            name to (amount / expense).toFloat()
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            PieChart(data = pieData, colors = pieColors)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        topCategories.forEachIndexed { index, (name, amount) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(pieColors[index % pieColors.size], RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "¥${String.format("%.2f", amount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("消费日历", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "颜色越深，当天支出越多",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    for (month in 0..11) {
                        val monthTotal = monthlyExpense[month]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${month + 1}月",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (monthTotal > 0) "¥${String.format("%.0f", monthTotal)}" else "-",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        MonthHeatmap(
                            year = year,
                            month = month,
                            dailyExpense = dailyExpense,
                            maxDailyExpense = maxDailyExpense
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MonthHeatmap(
    year: Int,
    month: Int,
    dailyExpense: Map<String, Double>,
    maxDailyExpense: Double
) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOffset = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7

    val cells = mutableListOf<Int?>()
    repeat(firstDayOffset) { cells.add(null) }
    for (day in 1..maxDay) cells.add(day)
    while (cells.size % 7 != 0) cells.add(null)

    Column {
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    if (day == null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(1.dp)
                        )
                    } else {
                        val key = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                        val amount = dailyExpense[key] ?: 0.0
                        val cellColor = if (amount > 0 && maxDailyExpense > 0) {
                            val ratio = (amount / maxDailyExpense).coerceIn(0.0, 1.0)
                            MaterialTheme.colorScheme.primary.copy(alpha = (0.25 + ratio * 0.75).toFloat())
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(1.dp)
                                .background(cellColor, RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}
