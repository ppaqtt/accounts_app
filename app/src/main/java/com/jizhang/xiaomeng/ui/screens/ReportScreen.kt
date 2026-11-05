package com.jizhang.xiaomeng.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.jizhang.xiaomeng.ui.utils.DateUtils
import com.jizhang.xiaomeng.ui.utils.LocalBookManager
import com.jizhang.xiaomeng.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val book = LocalBookManager.current.currentBook
    var reportType by remember { mutableStateOf("weekly") }
    var periodOffset by remember { mutableIntStateOf(0) }

    val range = remember(reportType, periodOffset) { buildReportRange(reportType, periodOffset) }
    val transactions = remember(book, range) {
        viewModel.getTransactionsByBookAndDateRange(book, range.start, range.end)
    }.collectAsStateWithLifecycle(initialValue = emptyList())
    val categories = viewModel.allCategories.collectAsStateWithLifecycle(initialValue = emptyList())

    val income = transactions.value.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expense = transactions.value.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val balance = income - expense

    val expenseRanking = transactions.value
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.categoryId }
        .map { (categoryId, list) -> categoryId to list.sumOf { it.amount } }
        .sortedByDescending { it.second }

    val dayFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
    val dailyTrend = transactions.value
        .groupBy { dayFormat.format(it.date) }
        .map { (day, list) -> day to list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } }
        .sortedBy { it.first }

    fun shareReport() {
        val topCategory = expenseRanking.firstOrNull()?.let { (categoryId, amount) ->
            val name = categories.value.find { it.id == categoryId }?.name ?: "未知分类"
            "$name ¥${String.format("%.2f", amount)}"
        } ?: "暂无支出"

        val text = buildString {
            appendLine("小萌记账 · ${if (reportType == "weekly") "周报" else "月报"}")
            appendLine("账本：$book")
            appendLine("周期：${range.label}")
            appendLine("收入：¥${String.format("%.2f", income)}")
            appendLine("支出：¥${String.format("%.2f", expense)}")
            appendLine("结余：¥${String.format("%.2f", balance)}")
            appendLine("最大支出分类：$topCategory")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "分享收支报告"))
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
                    onClick = { reportType = "weekly"; periodOffset = 0 },
                    label = { Text("周报") }
                )
                FilterChip(
                    selected = reportType == "monthly",
                    onClick = { reportType = "monthly"; periodOffset = 0 },
                    label = { Text("月报") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { periodOffset-- }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "上一周期")
                }
                Text(
                    text = range.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { periodOffset++ }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "下一周期")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                            Text("总收入", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "¥${String.format("%.2f", income)}",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("总支出", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "¥${String.format("%.2f", expense)}",
                                color = Color(0xFFF44336),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("结余", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        val maxAmount = expenseRanking.maxOf { it.second }.coerceAtLeast(1.0)
                        expenseRanking.forEach { (categoryId, amount) ->
                            val categoryName = categories.value.find { it.id == categoryId }?.name ?: "未知分类"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(categoryName, modifier = Modifier.width(72.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(4.dp)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((amount / maxAmount).toFloat().coerceIn(0.001f, 1f))
                                            .height(8.dp)
                                            .background(Color(0xFFF44336), RoundedCornerShape(4.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "¥${String.format("%.0f", amount)}",
                                    style = MaterialTheme.typography.bodySmall
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
                    Text("每日支出趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (dailyTrend.isEmpty()) {
                        Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        val maxDay = dailyTrend.maxOf { it.second }.coerceAtLeast(1.0)
                        dailyTrend.forEach { (day, amount) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(day, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(48.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(10.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(5.dp)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((amount / maxDay).toFloat().coerceIn(0.001f, 1f))
                                            .height(10.dp)
                                            .background(Color(0xFFF44336), RoundedCornerShape(5.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "¥${String.format("%.0f", amount)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
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

private data class ReportRange(val start: Long, val end: Long, val label: String)

private fun buildReportRange(type: String, offset: Int): ReportRange {
    val calendar = Calendar.getInstance()
    return if (type == "weekly") {
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        if (calendar.timeInMillis > System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, -7)
        }
        calendar.add(Calendar.WEEK_OF_YEAR, offset)
        setStartOfDay(calendar)
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 6)
        setEndOfDay(calendar)
        val end = calendar.timeInMillis
        ReportRange(
            start,
            end,
            "${DateUtils.formatDate(Date(start))} - ${DateUtils.formatDate(Date(end))}"
        )
    } else {
        calendar.add(Calendar.MONTH, offset)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        setStartOfDay(calendar)
        val start = calendar.timeInMillis
        calendar.set(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        )
        setEndOfDay(calendar)
        val end = calendar.timeInMillis
        ReportRange(
            start,
            end,
            "${calendar.get(Calendar.YEAR)}年${calendar.get(Calendar.MONTH) + 1}月"
        )
    }
}

private fun setStartOfDay(calendar: Calendar) {
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
}

private fun setEndOfDay(calendar: Calendar) {
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
}
