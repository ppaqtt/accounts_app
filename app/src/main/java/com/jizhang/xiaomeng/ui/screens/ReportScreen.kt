package com.jizhang.xiaomeng.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jizhang.xiaomeng.data.model.Category
import com.jizhang.xiaomeng.data.model.Transaction
import com.jizhang.xiaomeng.data.model.TransactionType
import com.jizhang.xiaomeng.ui.utils.DateUtils
import com.jizhang.xiaomeng.ui.utils.ReportExportData
import com.jizhang.xiaomeng.ui.utils.ReportExporter
import com.jizhang.xiaomeng.ui.utils.ReportTopItem
import com.jizhang.xiaomeng.viewmodel.TransactionViewModel
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val EXPENSE_COLOR = 0xFFEF4444
private const val INCOME_COLOR = 0xFF10B981

private data class CategoryRank(
    val categoryId: Long,
    val name: String,
    val amount: Double,
    val count: Int,
    val percent: Float,
    val noteKeyword: String,
    val noteCount: Int
)

private data class ReportData(
    val income: Double,
    val expense: Double,
    val prevIncome: Double,
    val prevExpense: Double,
    val dailyExpenses: Map<Int, Double>,
    val topCategories: List<CategoryRank>,
    val prevCategoryAmounts: Map<Long, Double>,
    val daysInMonth: Int,
    val firstDayOffset: Int,
    val monthlyExpenses: List<Pair<String, Double>>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: TransactionViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle(initialValue = emptyList())

    var year by remember { mutableIntStateOf(viewModel.currentYear) }
    var month by remember { mutableIntStateOf(viewModel.currentMonth) }
    var showExportError by remember { mutableStateOf(false) }

    val data = remember(allTransactions, allCategories, year, month) {
        buildReportData(allTransactions, allCategories, year, month)
    }
    val summaryLines = remember(data) { buildSummaryLines(data) }
    val summaryText = remember(summaryLines, year, month) {
        "【小萌记账】${year}年${month + 1}月账单小结\n" + summaryLines.joinToString("\n")
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
                    IconButton(onClick = { shareText(context, summaryText) }) {
                        Icon(Icons.Filled.Share, contentDescription = "分享本月小结")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            MonthSwitcher(
                year = year,
                month = month,
                onPrevious = {
                    if (month == 0) {
                        month = 11
                        year--
                    } else {
                        month--
                    }
                },
                onNext = {
                    if (month == 11) {
                        month = 0
                        year++
                    } else {
                        month++
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            ReportSummaryCard(income = data.income, expense = data.expense)

            Spacer(modifier = Modifier.height(16.dp))
            ComparisonCard(data = data)

            Spacer(modifier = Modifier.height(16.dp))
            HeatmapCard(data = data, reportYear = year, reportMonth = month)

            Spacer(modifier = Modifier.height(16.dp))
            TopCategoriesCard(data = data)

            Spacer(modifier = Modifier.height(16.dp))
            SummaryTextCard(lines = summaryLines)

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    val success = ReportExporter.shareLongImage(
                        context,
                        data.toExportData(year, month, summaryLines)
                    )
                    if (!success) showExportError = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("导出长图")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { shareText(context, summaryText) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("分享本月小结")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showExportError) {
        AlertDialog(
            onDismissRequest = { showExportError = false },
            title = { Text("导出失败") },
            text = { Text("生成报表长图失败，请稍后重试。") },
            confirmButton = {
                TextButton(onClick = { showExportError = false }) { Text("知道了") }
            }
        )
    }
}

@Composable
private fun MonthSwitcher(
    year: Int,
    month: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "上个月")
        }
        Text(
            text = DateUtils.formatMonthYear(year, month),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "下个月")
        }
    }
}

@Composable
private fun ReportSummaryCard(income: Double, expense: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryMetric("支出", money(expense), Color(EXPENSE_COLOR))
            SummaryMetric("收入", money(income), Color(INCOME_COLOR))
            SummaryMetric(
                "结余",
                money(income - expense),
                Color(if (income - expense >= 0) INCOME_COLOR else EXPENSE_COLOR)
            )
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun ComparisonCard(data: ReportData) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "月度对比",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "近 6 个月支出趋势",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            MonthlyBarChart(months = data.monthlyExpenses)
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            ComparisonRow("支出", data.expense, data.prevExpense)
            Spacer(modifier = Modifier.height(8.dp))
            ComparisonRow("收入", data.income, data.prevIncome)
        }
    }
}

@Composable
private fun MonthlyBarChart(months: List<Pair<String, Double>>) {
    val maxValue = (months.maxOfOrNull { it.second } ?: 0.0).coerceAtLeast(1.0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        months.forEach { (label, value) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (value > 0) compactMoney(value) else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(((value / maxValue) * 78).dp.coerceAtLeast(2.dp))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ComparisonRow(label: String, current: Double, previous: Double) {
    val changeText: String
    val changeColor: Color
    if (previous <= 0.0) {
        changeText = if (current > 0) "本月开始有记录" else "暂无记录"
        changeColor = MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        val percent = (current - previous) / previous * 100.0
        val direction = if (percent >= 0) "增加" else "减少"
        changeColor = if (percent >= 0) Color(EXPENSE_COLOR) else Color(INCOME_COLOR)
        changeText = "$direction ${formatPercent(abs(percent))}%"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "本月 ${money(current)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = changeText,
            style = MaterialTheme.typography.bodySmall,
            color = changeColor
        )
    }
}

@Composable
private fun HeatmapCard(data: ReportData, reportYear: Int, reportMonth: Int) {
    val maxDaily = data.dailyExpenses.values.maxOrNull() ?: 0.0
    val rows = if (data.daysInMonth == 0) 0 else (data.firstDayOffset + data.daysInMonth + 6) / 7
    val today = Calendar.getInstance()
    val isCurrentMonth = today.get(Calendar.YEAR) == reportYear && today.get(Calendar.MONTH) == reportMonth
    val todayDay = today.get(Calendar.DAY_OF_MONTH)

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "消费日历热力图",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "颜色越深，当天支出越多",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val index = row * 7 + col
                        val day = index - data.firstDayOffset + 1
                        if (day in 1..data.daysInMonth) {
                            val amount = data.dailyExpenses[day] ?: 0.0
                            val ratio = if (maxDaily > 0) (amount / maxDaily).toFloat() else 0f
                            val isToday = isCurrentMonth && day == todayDay
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (ratio <= 0f) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        else lerp(Color(0xFFFFCDD2), Color(0xFFD32F2F), ratio)
                                    )
                                    .then(
                                        if (isToday) Modifier.border(
                                            width = 1.5.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(6.dp)
                                        ) else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$day",
                                    fontSize = 11.sp,
                                    color = if (ratio > 0.55f) Color.White
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "少",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { ratio ->
                    Box(
                        modifier = Modifier
                            .size(width = 22.dp, height = 12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (ratio <= 0f) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                else lerp(Color(0xFFFFCDD2), Color(0xFFD32F2F), ratio)
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = "多",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "本月支出 ${money(data.expense)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TopCategoriesCard(data: ReportData) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "分类 Top5",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (data.topCategories.isEmpty()) {
                Text(
                    text = "本月暂无支出记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                data.topCategories.forEachIndexed { index, item ->
                    if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(20.dp)
                        )
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = money(item.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${formatPercent(item.percent.toDouble())}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(42.dp),
                            textAlign = TextAlign.End
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = (item.percent / 100f).coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(EXPENSE_COLOR)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryTextCard(lines: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "本月小结",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            lines.forEach { line ->
                Text(
                    text = "· $line",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

private fun buildReportData(
    transactions: List<Transaction>,
    categories: List<Category>,
    year: Int,
    month: Int
): ReportData {
    val calendar = Calendar.getInstance()
    fun inMonth(transaction: Transaction, targetYear: Int, targetMonth: Int): Boolean {
        calendar.time = transaction.date
        return calendar.get(Calendar.YEAR) == targetYear && calendar.get(Calendar.MONTH) == targetMonth
    }

    val prevMonth = if (month == 0) 11 else month - 1
    val prevYear = if (month == 0) year - 1 else year

    val monthTransactions = transactions.filter { inMonth(it, year, month) }
    val prevTransactions = transactions.filter { inMonth(it, prevYear, prevMonth) }

    val income = monthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expense = monthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val prevIncome = prevTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val prevExpense = prevTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    val dailyExpenses = mutableMapOf<Int, Double>()
    monthTransactions.filter { it.type == TransactionType.EXPENSE }.forEach { transaction ->
        calendar.time = transaction.date
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        dailyExpenses[day] = (dailyExpenses[day] ?: 0.0) + transaction.amount
    }

    val topCategories = monthTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.categoryId }
        .map { (categoryId, list) ->
            val amount = list.sumOf { it.amount }
            val notes = list.map { it.note.trim() }.filter { it.isNotEmpty() }
            val keywordCounts = notes
                .map { keywordOf(it) }
                .filter { it.isNotBlank() }
                .groupingBy { it }
                .eachCount()
            val keyword = keywordCounts.maxByOrNull { it.value }
            CategoryRank(
                categoryId = categoryId,
                name = categories.firstOrNull { it.id == categoryId }?.name ?: "其他",
                amount = amount,
                count = list.size,
                percent = if (expense > 0) (amount / expense * 100).toFloat() else 0f,
                noteKeyword = keyword?.key ?: "",
                noteCount = keyword?.value ?: 0
            )
        }
        .sortedByDescending { it.amount }
        .take(5)

    val prevCategoryAmounts = prevTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.categoryId }
        .mapValues { (_, list) -> list.sumOf { it.amount } }

    val firstDay = Calendar.getInstance().apply {
        set(year, month, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val firstDayOffset = (firstDay.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH)

    val monthlyExpenses = (5 downTo 0).map { offset ->
        val target = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, -offset)
        }
        val targetYear = target.get(Calendar.YEAR)
        val targetMonth = target.get(Calendar.MONTH)
        val total = transactions
            .filter { it.type == TransactionType.EXPENSE && inMonth(it, targetYear, targetMonth) }
            .sumOf { it.amount }
        "${targetMonth + 1}月" to total
    }

    return ReportData(
        income = income,
        expense = expense,
        prevIncome = prevIncome,
        prevExpense = prevExpense,
        dailyExpenses = dailyExpenses,
        topCategories = topCategories,
        prevCategoryAmounts = prevCategoryAmounts,
        daysInMonth = daysInMonth,
        firstDayOffset = firstDayOffset,
        monthlyExpenses = monthlyExpenses
    )
}

private fun buildSummaryLines(data: ReportData): List<String> {
    val lines = mutableListOf<String>()
    if (data.expense == 0.0 && data.income == 0.0) {
        lines.add("本月还没有记账记录，记下第一笔就能看到分析啦。")
        return lines
    }
    lines.add(
        "本月支出 ${money(data.expense)}，收入 ${money(data.income)}，" +
                "结余 ${money(data.income - data.expense)}。"
    )

    val top = data.topCategories.firstOrNull()
    if (top != null) {
        lines.add("最大开销是「${top.name}」${money(top.amount)}，占总支出的 ${formatPercent(top.percent.toDouble())}%。")
        val prevAmount = data.prevCategoryAmounts[top.categoryId] ?: 0.0
        val keywordPart = if (top.noteKeyword.isNotBlank() && top.noteCount > 1) {
            "，主要来自 ${top.noteCount} 笔${top.noteKeyword}"
        } else {
            "，共 ${top.count} 笔"
        }
        if (prevAmount > 0) {
            val change = (top.amount - prevAmount) / prevAmount * 100
            val direction = if (change >= 0) "多" else "少"
            lines.add("${top.name}比上月$direction ${formatPercent(abs(change))}%$keywordPart。")
        } else if (top.amount > 0) {
            lines.add("${top.name}是本月新增开销$keywordPart。")
        }
    }

    if (data.prevExpense > 0) {
        val change = (data.expense - data.prevExpense) / data.prevExpense * 100
        val isUp = change >= 0
        lines.add(
            "总支出比上月${if (isUp) "多" else "少"} ${formatPercent(abs(change))}%，" +
                    if (isUp) "注意控制一下节奏。" else "继续保持。"
        )
    }
    return lines
}

private fun keywordOf(note: String): String {
    return note
        .split(" ", "，", ",", "-", "_", "·", "/", "|")
        .firstOrNull { it.isNotBlank() }
        ?.trim()
        ?.take(6)
        ?: ""
}

private fun ReportData.toExportData(
    year: Int,
    month: Int,
    summaryLines: List<String>
): ReportExportData {
    return ReportExportData(
        year = year,
        month = month,
        income = income,
        expense = expense,
        balance = income - expense,
        prevIncome = prevIncome,
        prevExpense = prevExpense,
        topCategories = topCategories.map { ReportTopItem(it.name, it.amount, it.percent) },
        dailyExpenses = dailyExpenses,
        firstDayOffset = firstDayOffset,
        daysInMonth = daysInMonth,
        summaryLines = summaryLines
    )
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "分享本月小结"))
}

private fun money(value: Double): String =
    "¥" + String.format(Locale.CHINA, "%,.2f", value)

private fun compactMoney(value: Double): String {
    return if (value >= 10000) {
        String.format(Locale.CHINA, "%.1f万", value / 10000)
    } else {
        String.format(Locale.CHINA, "%.0f", value)
    }
}

private fun formatPercent(value: Double): String =
    String.format(Locale.CHINA, "%.0f", value)
