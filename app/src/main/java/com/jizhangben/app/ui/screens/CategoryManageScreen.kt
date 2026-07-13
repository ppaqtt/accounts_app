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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import com.jizhangben.app.data.model.Category
import com.jizhangben.app.data.model.TransactionType
import com.jizhangben.app.ui.utils.IconMapper
import com.jizhangben.app.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManageScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    var currentTab by remember { mutableStateOf(TransactionType.EXPENSE) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    val expenseCategories = viewModel.getCategoriesByType(TransactionType.EXPENSE).observeAsState(initial = emptyList())
    val incomeCategories = viewModel.getCategoriesByType(TransactionType.INCOME).observeAsState(initial = emptyList())

    val categories = if (currentTab == TransactionType.EXPENSE) expenseCategories.value else incomeCategories.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分类管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        editingCategory = null
                        showAddDialog = true
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "添加")
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
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentTab == TransactionType.EXPENSE,
                    onClick = { currentTab = TransactionType.EXPENSE },
                    label = { Text("支出分类") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = currentTab == TransactionType.INCOME,
                    onClick = { currentTab = TransactionType.INCOME },
                    label = { Text("收入分类") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📂", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "暂无分类，点击右上角添加",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(categories) { category ->
                        CategoryItem(
                            category = category,
                            onEdit = {
                                editingCategory = category
                                showAddDialog = true
                            },
                            onDelete = { categoryToDelete = category }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember(editingCategory) { mutableStateOf(editingCategory?.name ?: "") }
        var icon by remember(editingCategory) { mutableStateOf(editingCategory?.icon ?: "more_horiz") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (editingCategory == null) "添加分类" else "编辑分类") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("分类名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("选择图标", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    IconSelector(selectedIcon = icon, onIconSelected = { icon = it })
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            if (editingCategory != null) {
                                viewModel.updateCategory(
                                    editingCategory!!.copy(name = name, icon = icon)
                                )
                            } else {
                                viewModel.insertCategory(
                                    Category(
                                        name = name,
                                        icon = icon,
                                        type = currentTab
                                    )
                                )
                            }
                            showAddDialog = false
                        }
                    }
                ) {
                    Text(if (editingCategory == null) "添加" else "保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除分类「${categoryToDelete!!.name}」吗？\n注意：已有账单使用该分类时将无法删除。") },
            confirmButton = {
                TextButton(onClick = {
                    categoryToDelete?.let { viewModel.deleteCategory(it) }
                    categoryToDelete = null
                }) {
                    Text("删除", color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                containerColor = if (category.type == TransactionType.INCOME)
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
                    imageVector = IconMapper.getIcon(category.icon),
                    contentDescription = category.name,
                    tint = if (category.type == TransactionType.INCOME)
                        Color(0xFF4CAF50)
                    else
                        Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (category.isDefault) {
            Text(
                text = "默认",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Filled.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!category.isDefault) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "删除", tint = Color(0xFFF44336))
            }
        }
    }
}

@Composable
fun IconSelector(selectedIcon: String, onIconSelected: (String) -> Unit) {
    val iconList = listOf(
        "restaurant", "shopping_cart", "directions_car", "home", "flight",
        "movie", "fitness_center", "work", "school", "medical_services",
        "favorite", "celebration", "pets", "nature", "weekend",
        "account_balance", "payments", "savings", "trending_up", "card_giftcard",
        "more_horiz", "coffee", "local_grocery_store", "phone_iphone", "build"
    )

    LazyColumn {
        items(iconList.chunked(5)) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { iconName ->
                    val isSelected = selectedIcon == iconName
                    Card(
                        shape = CircleShape,
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onIconSelected(iconName) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconMapper.getIcon(iconName),
                                contentDescription = null,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
