package com.jizhang.xiaomeng.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jizhang.xiaomeng.data.model.Account
import com.jizhang.xiaomeng.data.model.AccountType
import com.jizhang.xiaomeng.data.model.Transaction
import com.jizhang.xiaomeng.data.model.TransactionType
import com.jizhang.xiaomeng.viewmodel.TransactionViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    val accounts = viewModel.allAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val transactions = viewModel.allTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val categories = viewModel.allCategories.collectAsStateWithLifecycle(initialValue = emptyList())

    val balances = remember(accounts.value, transactions.value) {
        accounts.value.associate { account ->
            account.id to calculateBalance(account, transactions.value)
        }
    }
    val totalAssets = balances.values.sum()

    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var deletingAccount by remember { mutableStateOf<Account?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账户与资产") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "添加账户")
                    }
                }
            )
        },
        floatingActionButton = {
            if (accounts.value.size >= 2) {
                FloatingActionButton(
                    onClick = { showTransferDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = "账户转账")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        text = "净资产",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "¥${String.format("%.2f", totalAssets)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (accounts.value.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无账户，点击右上角添加", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accounts.value, key = { it.id }) { account ->
                        AccountItem(
                            account = account,
                            balance = balances[account.id] ?: account.initialBalance,
                            onEdit = { editingAccount = account },
                            onDelete = { deletingAccount = account }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AccountEditDialog(
            account = null,
            onDismiss = { showAddDialog = false },
            onSave = { account ->
                viewModel.insertAccount(account)
                showAddDialog = false
            }
        )
    }

    val editing = editingAccount
    if (editing != null) {
        AccountEditDialog(
            account = editing,
            onDismiss = { editingAccount = null },
            onSave = { account ->
                viewModel.updateAccount(account)
                editingAccount = null
            }
        )
    }

    val deleting = deletingAccount
    if (deleting != null) {
        AlertDialog(
            onDismissRequest = { deletingAccount = null },
            title = { Text("删除账户") },
            text = { Text("确定删除「${deleting.name}」吗？该账户下的历史流水不会被删除。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount(deleting)
                    deletingAccount = null
                }) {
                    Text("删除", color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingAccount = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (showTransferDialog) {
        val expenseCategory = categories.value.firstOrNull { it.type == TransactionType.EXPENSE }
        TransferDialog(
            accounts = accounts.value,
            onDismiss = { showTransferDialog = false },
            onConfirm = { fromId, toId, amount, note ->
                if (expenseCategory != null) {
                    viewModel.insertTransaction(
                        Transaction(
                            amount = amount,
                            type = TransactionType.TRANSFER,
                            categoryId = expenseCategory.id,
                            accountId = fromId,
                            toAccountId = toId,
                            note = note.ifEmpty { "账户转账" },
                            date = Date()
                        )
                    )
                }
                showTransferDialog = false
            }
        )
    }
}

private fun calculateBalance(account: Account, transactions: List<Transaction>): Double {
    var balance = account.initialBalance
    transactions.forEach { tx ->
        when (tx.type) {
            TransactionType.INCOME -> if (tx.accountId == account.id) balance += tx.amount
            TransactionType.EXPENSE -> if (tx.accountId == account.id) balance -= tx.amount
            TransactionType.TRANSFER -> {
                if (tx.accountId == account.id) balance -= tx.amount
                if (tx.toAccountId == account.id) balance += tx.amount
            }
        }
    }
    return balance
}

private fun accountTypeIcon(type: AccountType): ImageVector {
    return when (type) {
        AccountType.CASH -> Icons.Filled.Payments
        AccountType.BANK_CARD -> Icons.Filled.AccountBalance
        AccountType.ALIPAY -> Icons.Filled.AccountBalanceWallet
        AccountType.WECHAT -> Icons.Filled.Chat
        AccountType.OTHER -> Icons.Filled.MoreHoriz
    }
}

@Composable
private fun AccountItem(
    account: Account,
    balance: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = accountTypeIcon(account.type),
                contentDescription = account.type.displayName,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = account.type.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "¥${String.format("%.2f", balance)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "编辑账户", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "删除账户", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountEditDialog(
    account: Account?,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit
) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    var type by remember { mutableStateOf(account?.type ?: AccountType.CASH) }
    var initialBalance by remember {
        mutableStateOf(account?.initialBalance?.let { if (it != 0.0) it.toString() else "" } ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (account == null) "添加账户" else "编辑账户") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("账户名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("账户类型", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountType.values().forEach { item ->
                        FilterChip(
                            selected = type == item,
                            onClick = { type = item },
                            label = { Text(item.displayName) }
                        )
                    }
                }
                OutlinedTextField(
                    value = initialBalance,
                    onValueChange = { value ->
                        initialBalance = value.filter { it.isDigit() || it == '.' || it == '-' }
                    },
                    label = { Text("初始余额（元）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            Account(
                                id = account?.id ?: 0,
                                name = name.trim(),
                                type = type,
                                initialBalance = initialBalance.toDoubleOrNull() ?: 0.0,
                                isDefault = account?.isDefault ?: false
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransferDialog(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long, Double, String) -> Unit
) {
    var fromId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var toId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: 0L) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("账户转账") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("转出账户", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { item ->
                        FilterChip(
                            selected = fromId == item.id,
                            onClick = {
                                fromId = item.id
                                if (toId == item.id) toId = accounts.firstOrNull { it.id != item.id }?.id ?: 0L
                            },
                            label = { Text(item.name) }
                        )
                    }
                }
                Text("转入账户", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { item ->
                        FilterChip(
                            selected = toId == item.id,
                            onClick = {
                                toId = item.id
                                if (fromId == item.id) fromId = accounts.firstOrNull { it.id != item.id }?.id ?: 0L
                            },
                            label = { Text(item.name) }
                        )
                    }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value -> amountText = value.filter { it.isDigit() || it == '.' } },
                    label = { Text("转账金额") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && fromId != 0L && toId != 0L && fromId != toId) {
                        onConfirm(fromId, toId, amount, note)
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0 && fromId != 0L && toId != 0L && fromId != toId
            ) {
                Text("确认转账")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
