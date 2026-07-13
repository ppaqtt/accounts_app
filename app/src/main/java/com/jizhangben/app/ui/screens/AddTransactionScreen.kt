package com.jizhangben.app.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.observeAsState
import androidx.navigation.NavController
import com.jizhangben.app.data.model.Category
import com.jizhangben.app.data.model.Transaction
import com.jizhangben.app.data.model.TransactionType
import com.jizhangben.app.ui.utils.DateUtils
import com.jizhangben.app.ui.utils.IconMapper
import com.jizhangben.app.viewmodel.TransactionViewModel
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(viewModel: TransactionViewModel, navController: NavController) {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableDoubleStateOf(0.0) }
    var amountText by remember { mutableStateOf("0") }
    var note by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableLongStateOf(-1L) }
    var selectedDate by remember { mutableStateOf(Date()) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val categories = viewModel.getCategoriesByType(selectedType)
        .observeAsState(initial = emptyList())

    // 统一处理分类选择：当分类列表变化或切换类型时，自动选中第一个分类
    LaunchedEffect(categories.value, selectedType) {
        if (categories.value.isNotEmpty()) {
            val currentExists = categories.value.any { it.id == selectedCategoryId }
            if (!currentExists) {
                selectedCategoryId = categories.value[0].id
            }
        }
    }

    fun showDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                selectedDate = cal.time
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun onNumberClick(num: String) {
        if (num == "." && amountText.contains(".")) return
        if (amountText == "0" && num != ".") {
            amountText = num
        } else {
            // 限制小数点后最多两位
            if (amountText.contains(".")) {
                val afterDot = amountText.substringAfter(".")
                if (afterDot.length >= 2) return
            }
            amountText += num
        }
        amount = amountText.toDoubleOrNull() ?: 0.0
    }

    fun onDeleteClick() {
        if (amountText.isNotEmpty() && amountText != "0") {
            amountText = amountText.dropLast(1)
            if (amountText.isEmpty() || amountText == ".") {
                amountText = "0"
            }
            amount = amountText.toDoubleOrNull() ?: 0.0
        }
    }

    fun onClearClick() {
        amountText = "0"
        amount = 0.0
        note = ""
        selectedCategoryId = categories.value.firstOrNull()?.id ?: -1L
    }

    fun saveTransaction() {
        if (amount <= 0 || selectedCategoryId == -1L) return

        val transaction = Transaction(
            amount = amount,
            type = selectedType,
            categoryId = selectedCategoryId,
            note = note,
            date = selectedDate
        )
        viewModel.insertTransaction(transaction)

        // 重置表单
        amountText = "0"
        amount = 0.0
        note = ""
        selectedCategoryId = categories.value.firstOrNull()?.id ?: -1L
        navController.navigate("home") {
            popUpTo("home") { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "记一笔",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 支出/收入切换
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

        Spacer(modifier = Modifier.height(12.dp))

        // 金额显示
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "金额",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¥${String.format("%.2f", amount)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedType == TransactionType.INCOME)
                        Color(0xFF4CAF50)
                    else
                        Color(0xFFF44336)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 分类选择
        Text(
            text = "选择分类",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.height(120.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(categories.value) { category ->
                CategoryItem(
                    category = category,
                    isSelected = selectedCategoryId == category.id,
                    type = selectedType,
                    onClick = { selectedCategoryId = category.id }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 日期选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "日期",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = DateUtils.formatDate(selectedDate),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { showDatePicker() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 备注
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("备注（可选）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 数字键盘
        NumberPad(
            onNumberClick = ::onNumberClick,
            onDeleteClick = ::onDeleteClick,
            onClearClick = ::onClearClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 保存按钮 - 始终显示在底部
        Button(
            onClick = ::saveTransaction,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = amount > 0 && selectedCategoryId != -1L
        ) {
            Icon(Icons.Filled.Check, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "保存",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    type: TransactionType,
    onClick: () -> Unit
) {
    val selectedColor = if (type == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFF44336)
    val selectedBg = if (type == TransactionType.INCOME) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) selectedBg else MaterialTheme.colorScheme.surface,
            tonalElevation = if (isSelected) 0.dp else 1.dp,
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconMapper.getIcon(category.icon),
                    contentDescription = category.name,
                    tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
fun NumberPad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit
) {
    val buttons = listOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        "C", "0", "⌫"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0 until 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    val button = buttons[index]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clickable {
                                when (button) {
                                    "⌫" -> onDeleteClick()
                                    "C" -> onClearClick()
                                    else -> onNumberClick(button)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = button,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            fontSize = 22.sp
                        )
                    }
                }
            }
        }
    }
}
