package com.jizhangben.app.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jizhangben.app.data.dao.CategorySummary
import com.jizhangben.app.data.model.Category
import com.jizhangben.app.data.model.TransactionType
import com.jizhangben.app.ui.utils.DateUtils
import com.jizhangben.app.ui.utils.IconMapper
import com.jizhangben.app.ui.utils.observeAsStateInitial
import com.jizhangben.app.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: TransactionViewModel) {
    var currentYear by remember { mutableIntStateOf(viewModel.currentYear) }
    var currentMonth by remember { mutableIntStateOf(viewModel.currentMonth) }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }

    val income = viewModel.getTotalIncomeByMonth(currentYear, currentMonth)
        .observeAsStateInitial(initial = 0.0)
    val expense = viewModel.getTotalExpenseByMonth(currentYear, currentMonth)
        .observeAsStateInitial(initial = 0.0)
    val categories = viewModel.allCategories.observeAsStateInitial(initial = emptyList())

    val categorySummary = if (selectedType == TransactionType.EXPENSE) {
        viewModel.getExpenseCategorySummary(currentYear, currentMonth)
            .observeAsStateInitial(initial = emptyList())
    } else {
        viewModel.getIncomeCategorySummary(currentYear, currentMonth)
            .observeAsStateInitial(initial = emptyList())
    }

    val total = if (selectedType == TransactionType.EXPENSE) expense ?: 0.0 else income ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        MonthSelectorStats(
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

        SummaryStatsCard(
            income = income ?: 0.0,
            expense = expense ?: 0.0
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        if (categorySummary.isEmpty() || total == 0.0) {
            EmptyStatsState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(categorySummary) { summary ->
                    val category = categories.find { it.id == summary.categoryId }
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
}

@Composable
fun MonthSelectorStats(year: Int, month: Int, onPrevious: () -> Unit, onNext: () -> Unit) {
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
fun SummaryStatsCard(income: Double, expense: Double) {
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
