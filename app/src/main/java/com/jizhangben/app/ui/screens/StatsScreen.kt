package com.jizhangben.app.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.observeAsState
import com.jizhangben.app.data.dao.CategorySummary
import com.jizhangben.app.data.model.Category
import com.jizhangben.app.data.model.TransactionType
import com.jizhangben.app.ui.utils.DateUtils
import com.jizhangben.app.ui.utils.IconMapper
import com.jizhangben.app.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: TransactionViewModel) {
    var currentYear by remember { mutableIntStateOf(viewModel.currentYear) }
    var currentMonth by remember { mutableIntStateOf(viewModel.currentMonth) }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var isYearView by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("分类", "图表", "对比")

    val income = if (isYearView) {
        viewModel.getTotalIncomeByYear(currentYear).observeAsState(initial = 0.0)
    } else {
        viewModel.getTotalIncomeByMonth(currentYear, currentMonth).observeAsState(initial = 0.0)
    }
    val expense = if (isYearView) {
        viewModel.getTotalExpenseByYear(currentYear).observeAsState(initial = 0.0)
    } else {
        viewModel.getTotalExpenseByMonth(currentYear, currentMonth).observeAsState(initial = 0.0)
    }
    val categories = viewModel.allCategories.collectAsStateWithLifecycle(initialValue = emptyList())

    val categorySummary = if (selectedType == TransactionType.EXPENSE) {
        if (isYearView) {
            viewModel.getExpenseCategorySummaryByYear(currentYear).observeAsState(initial = emptyList())
        } else {
            viewModel.getExpenseCategorySummary(currentYear, currentMonth).observeAsState(initial = emptyList())
        }
    } else {
        if (isYearView) {
            viewModel.getIncomeCategorySummaryByYear(currentYear).observeAsState(initial = emptyList())
        } else {
            viewModel.getIncomeCategorySummary(currentYear, currentMonth).observeAsState(initial = emptyList())
        }
    }

    // 上月数据（用于对比）
    val prevMonth = if (currentMonth == 0) 11 else currentMonth - 1
    val prevYear = if (currentMonth == 0) currentYear - 1 else currentYear
    val prevMonthIncome = viewModel.getTotalIncomeByMonth(prevYear, prevMonth).observeAsState(initial = 0.0)
    val prevMonthExpense = viewModel.getTotalExpenseByMonth(prevYear, prevMonth).observeAsState(initial = 0.0)

    // 去年数据（用于对比）
    val lastYearIncome = viewModel.getTotalIncomeByYear(currentYear - 1).observeAsState(initial = 0.0)
    val lastYearExpense = viewModel.getTotalExpenseByYear(currentYear - 1).observeAsState(initial = 0.0)

    // 今年数据（用于对比）
    val thisYearIncome = viewModel.getTotalIncomeByYear(currentYear).observeAsState(initial = 0.0)
    val thisYearExpense = viewModel.getTotalExpenseByYear(currentYear).observeAsState(initial = 0.0)

    val total = if (selectedType == TransactionType.EXPENSE) expense.value ?: 0.0 else income.value ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        YearMonthSelectorStats(
            year = currentYear,
            month = currentMonth,
            isYearView = isYearView,
            onToggleView = { isYearView = !isYearView },
            onPrevious = {
                if (isYearView) {
                    currentYear--
                } else {
                    if (currentMonth == 0) {
                        currentMonth = 11
                        currentYear--
                    } else {
                        currentMonth--
                    }
                }
            },
            onNext = {
                if (isYearView) {
                    currentYear++
                } else {
                    if (currentMonth == 11) {
                        currentMonth = 0
                        currentYear++
                    } else {
                        currentMonth++
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SummaryStatsCard(
            income = income.value ?: 0.0,
            expense = expense.value ?: 0.0,
            isYearView = isYearView
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                // 分类 tab（原有功能）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { selectedType = TransactionType.EXPENSE },
                        label = { Text("支出分类") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFEBEE),
                            selectedLabelColor = Color(0xFFF44336)
                        )
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME },
                        label = { Text("收入分类") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE8F5E9),
                            selectedLabelColor = Color(0xFF4CAF50)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "分类统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (categorySummary.value.isEmpty() || total == 0.0) {
                    EmptyStatsState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(categorySummary.value) { summary ->
                            val category = categories.value.find { it.id == summary.categoryId }
                            CategoryStatItem(
                                category = category,
                                amount = summary.total,
                                total = total,
                                type = selectedType
                            )
                        }
                    }
                }
            }
            1 -> {
                // 图表 tab
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { selectedType = TransactionType.EXPENSE },
                        label = { Text("支出") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFEBEE),
                            selectedLabelColor = Color(0xFFF44336)
                        )
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME },
                        label = { Text("收入") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE8F5E9),
                            selectedLabelColor = Color(0xFF4CAF50)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 饼图
                    Text(
                        text = "分类占比",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (categorySummary.value.isEmpty() || total == 0.0) {
                        EmptyStatsState()
                    } else {
                        val pieColors = listOf(
                            Color(0xFF4CAF50), Color(0xFFF44336), Color(0xFF2196F3),
                            Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF00BCD4),
                            Color(0xFFFFEB3B), Color(0xFFE91E63), Color(0xFF8BC34A),
                            Color(0xFF3F51B5)
                        )
                        val pieData = categorySummary.value.map { summary ->
                            val category = categories.value.find { it.id == summary.categoryId }
                            val percentage = (summary.total / total).toFloat()
                            (category?.name ?: "未知") to percentage
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            PieChart(data = pieData, colors = pieColors)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 图例
                        Column {
                            pieData.forEachIndexed { index, (name, percentage) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Canvas(modifier = Modifier.size(12.dp)) {
                                        drawRect(color = pieColors[index % pieColors.size])
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${String.format("%.1f", percentage * 100)}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 柱状图
                        Text(
                            text = "月度趋势",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val monthLabels = listOf("1月", "2月", "3月", "4月", "5月", "6月",
                            "7月", "8月", "9月", "10月", "11月", "12月")
                        val barData = monthLabels.mapIndexed { index, label ->
                            label to 0f // 占位，实际应从数据库查询月度数据
                        }
                        val barColor = if (selectedType == TransactionType.EXPENSE)
                            Color(0xFFF44336) else Color(0xFF4CAF50)
                        BarChart(data = barData, barColor = barColor)
                    }
                }
            }
            2 -> {
                // 对比 tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    ComparisonCard(
                        title = "本月 vs 上月 - 支出",
                        currentValue = expense.value ?: 0.0,
                        previousValue = prevMonthExpense.value ?: 0.0,
                        label = "支出"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ComparisonCard(
                        title = "本月 vs 上月 - 收入",
                        currentValue = income.value ?: 0.0,
                        previousValue = prevMonthIncome.value ?: 0.0,
                        label = "收入"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ComparisonCard(
                        title = "今年 vs 去年 - 支出",
                        currentValue = thisYearExpense.value ?: 0.0,
                        previousValue = lastYearExpense.value ?: 0.0,
                        label = "支出"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ComparisonCard(
                        title = "今年 vs 去年 - 收入",
                        currentValue = thisYearIncome.value ?: 0.0,
                        previousValue = lastYearIncome.value ?: 0.0,
                        label = "收入"
                    )
                }
            }
        }
    }
}

@Composable
fun PieChart(data: List<Pair<String, Float>>, colors: List<Color>) {
    Canvas(modifier = Modifier.size(200.dp)) {
        var startAngle = 0f
        data.forEachIndexed { index, (_, percentage) ->
            val sweepAngle = percentage * 360f
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun BarChart(data: List<Pair<String, Float>>, barColor: Color) {
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val barWidth = size.width / (data.size * 2)
        val maxVal = data.maxOfOrNull { it.second }?.let { if (it > 0f) it else 1f } ?: 1f
        data.forEachIndexed { index, (_, value) ->
            val barHeight = (value / maxVal) * size.height * 0.8f
            drawRect(
                color = barColor,
                topLeft = Offset(x = index * barWidth * 2 + barWidth / 2, y = size.height - barHeight),
                size = Size(width = barWidth, height = barHeight)
            )
        }
    }
}

@Composable
fun ComparisonCard(title: String, currentValue: Double, previousValue: Double, label: String) {
    val change = if (previousValue > 0) ((currentValue - previousValue) / previousValue * 100) else 0.0
    val changeText = if (change > 0) "+${String.format("%.1f", change)}%" else "${String.format("%.1f", change)}%"
    val changeColor = if (label == "支出") {
        if (change > 0) Color(0xFFF44336) else Color(0xFF4CAF50)
    } else {
        if (change > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("本期: ¥${String.format("%.2f", currentValue)}")
                Text(changeText, color = changeColor, fontWeight = FontWeight.SemiBold)
            }
            Text("上期: ¥${String.format("%.2f", previousValue)}", color = Color.Gray)
        }
    }
}

@Composable
fun YearMonthSelectorStats(
    year: Int,
    month: Int,
    isYearView: Boolean,
    onToggleView: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "上一${if (isYearView) "年" else "月"}")
            }
            Text(
                text = if (isYearView) "${year}年" else DateUtils.formatMonthYear(year, month),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNext) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "下一${if (isYearView) "年" else "月"}")
            }
        }
        FilterChip(
            selected = isYearView,
            onClick = onToggleView,
            label = { Text(if (isYearView) "切换到月视图" else "切换到年视图") }
        )
    }
}

@Composable
fun SummaryStatsCard(income: Double, expense: Double, isYearView: Boolean = false) {
    val balance = income - expense
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
                text = if (isYearView) "年度结余" else "本月结余",
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
                        text = "总收入",
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
                        text = "总支出",
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
        }
    }
}

@Composable
fun CategoryStatItem(
    category: Category?,
    amount: Double,
    total: Double,
    type: TransactionType
) {
    val percentage = if (total > 0) (amount / total * 100).toFloat() else 0f
    val color = if (type == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFF44336)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            shape = CircleShape,
            modifier = Modifier.size(40.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (type == TransactionType.INCOME)
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
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category?.name ?: "未知",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "¥${String.format("%.2f", amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.weight(1f).height(6.dp),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${String.format("%.1f", percentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyStatsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📊",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无统计数据",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
