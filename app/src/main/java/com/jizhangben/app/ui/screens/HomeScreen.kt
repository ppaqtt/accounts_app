package com.jizhangben.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jizhangben.app.data.model.Category
import com.jizhangben.app.data.model.Transaction
import com.jizhangben.app.data.model.TransactionType
import com.jizhangben.app.ui.utils.DateUtils
import com.jizhangben.app.ui.utils.IconMapper
import com.jizhangben.app.ui.utils.observeAsStateInitial
import com.jizhangben.app.viewmodel.TransactionViewModel
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TransactionViewModel, navController: NavController) {
    var currentYear by remember { mutableIntStateOf(viewModel.currentYear) }
    var currentMonth by remember { mutableIntStateOf(viewModel.currentMonth) }

    val monthTransactions = viewModel.getTransactionsByMonth(currentYear, currentMonth).observeAsStateInitial(initial = emptyList())
    val monthIncome = viewModel.getTotalIncomeByMonth(currentYear, currentMonth).observeAsStateInitial(initial = 0.0)
    val monthExpense = viewModel.getTotalExpenseByMonth(currentYear, currentMonth).observeAsStateInitial(initial = 0.0)
    val categoryList = viewModel.allCategories.observeAsStateInitial(initial = emptyList())

    val groupedTransactions = groupTransactionsByDate(monthTransactions)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
            income = monthIncome ?: 0.0,
            expense = monthExpense ?: 0.0
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "近期账单",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (monthTransactions.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                groupedTransactions.forEach { (date, dayTransactions) ->
                    item {
                        DayHeader(date = date, transactions = dayTransactions, categories = categoryList)
                    }
                    items(dayTransactions) { transaction ->
                        val category = categoryList.find { it.id == transaction.categoryId }
                        TransactionItem(
                            transaction = transaction,
                            category = category,
                            onClick = { }
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
        }
    }
}

@Composable
fun DayHeader(date: Date, transactions: List<Transaction>, categories: List<Category>) {
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
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    category: Category?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
fun EmptyState() {
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
            text = "暂无账单记录",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击下方「记账」开始记录吧",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
