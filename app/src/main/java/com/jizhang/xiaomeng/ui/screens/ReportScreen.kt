package com.jizhang.xiaomeng.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jizhang.xiaomeng.data.model.TransactionType
import com.jizhang.xiaomeng.ui.utils.IconMapper
import com.jizhang.xiaomeng.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class PeriodRange(val start: Long, val end: Long, val label: String, val days: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    var reportType by remember { mutableStateOf("weekly") }
    val transactions = viewModel.allTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val categories = viewModel.allCategories.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current

    val period = remember(reportType) { buildPeriodRange(reportType) }
    val periodTransactions = remember(transactions.value, period) {
        transactions.value.filter { it.date.time in period.start..period.end && it.type != TransactionType.TRANSFER }
    }
    val income = periodTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expense = periodTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val balance = income - expense

    val expenseRanking = remember(periodTransactions, categories.value) {
        periodTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .map { (categoryId, list) ->
                val category = categories.value.find { it.id == categoryId }
                ExpenseRankItem(
                    name = category?.name ?: "其他",
                    icon = category?.icon ?: "more_horiz",
                    amount = list.sumOf { it.amount }
                )
            }
            .sortedByDescending { it.amount }
            .take(5)
    }

    val dailyExpense = remember(periodTransactions, period) {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val map = periodTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { format.format(it.date) }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        period.days.map { day -> day to (map[day] ?: 0.0) }
    }

    fun buildShareText(): String {
        val title = if (reportType == "weekly") "周报" else "月报"
        val rankingText = if (expenseRanking.isEmpty()) {
            "暂无支出"
        } else {
            expenseRanking.joinToString("、") { "${it.name} ¥${String.format("%.2f", it.amount)}" }
        }
        return buildString {
            appendLine("【小萌记账】${period.label}$title")
            appendLine("总收入：¥${String.format("%.2f", income)}")
            appendLine("总支出：¥${String.format("%.2f", expense)}")
            appendLine("结余：¥${String.format("%.2f", balance)}")
            append("支出排行：$rankingText")
        }
    }

    fun shareReport() {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, buildShareText())
        }
        context.startActivity(Intent.createChooser(sendIntent, "分享收支报告"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("收支报告") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { shareReport() }) {
                        Icon(Icons.Filled.Share, contentDescription = "分享")
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = reportType == "weekly",
                    onClick = { reportType = "weekly" },
                    label = { Text("周报") }
                )
                FilterChip(
                    selected = reportType == "monthly",
                    onClick = { reportType = "monthly" },
                    label = { Text("月报") }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = period.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("收支概要", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("总收入", color = Color.Gray)
                            Text(
                                "¥${String.format("%.2f", income)}",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("总支出", color = Color.Gray)
                            Text(
                                "¥${String.format("%.2f", expense)}",
                                color = Color(0xFFF44336),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("结余", color = Color.Gray)
                            Text("¥${String.format("%.2f", balance)}", fontWeight = FontWeight.Bold)
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
                    Text("支出排行", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (expenseRanking.isEmpty()) {
                        Text("暂无数据", color = Color.Gray)
                    } else {
                        expenseRanking.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(20.dp)
                                )
                                Icon(
                                    imageVector = IconMapper.getIcon(item.icon),
                                    contentDescription = item.name,
                                    tint = Color(0xFFF44336)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(item.name, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "¥${String.format("%.2f", item.amount)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = {
                                            if (expense > 0) (item.amount / expense).toFloat().coerceIn(0f, 1f) else 0f
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp),
                                        color = Color(0xFFF44336),
                                        trackColor = MaterialTheme.colorScheme.surface
                                    )
                                }
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
                    Text("每日支出趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (expense <= 0.0) {
                        Text("暂无数据", color = Color.Gray)
                    } else {
                        val barData = dailyExpense.map { (day, amount) ->
                            day.substringAfterLast("-").toInt().toString() to amount.toFloat()
                        }
                        BarChart(data = barData, barColor = Color(0xFFF44336))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = dailyExpense.firstOrNull()?.first ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dailyExpense.lastOrNull()?.first ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { shareReport() }, modifier = Modifier.fillMaxWidth()) {
                Text("分享报告")
            }
        }
    }
}

private data class ExpenseRankItem(val name: String, val icon: String, val amount: Double)

private fun buildPeriodRange(reportType: String): PeriodRange {
    val now = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val labelFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())

    if (reportType == "weekly") {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val dayOfWeek = get(Calendar.DAY_OF_WEEK)
            val diff = (dayOfWeek + 5) % 7
            add(Calendar.DAY_OF_MONTH, -diff)
        }
        val end = (start.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val days = (0..6).map { offset ->
            val day = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, offset) }
            dateFormat.format(day.time)
        }
        return PeriodRange(
            start = start.timeInMillis,
            end = end.timeInMillis,
            label = "${labelFormat.format(start.time)} - ${labelFormat.format(end.time)}",
            days = days
        )
    }

    val start = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val end = (start.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    val days = (1..start.getActualMaximum(Calendar.DAY_OF_MONTH)).map { day ->
        val dayCalendar = (start.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
        dateFormat.format(dayCalendar.time)
    }
    return PeriodRange(
        start = start.timeInMillis,
        end = end.timeInMillis,
        label = SimpleDateFormat("yyyy年MM月", Locale.getDefault()).format(now.time),
        days = days
    )
}
