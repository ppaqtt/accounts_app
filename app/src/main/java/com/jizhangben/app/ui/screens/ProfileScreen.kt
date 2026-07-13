package com.jizhangben.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jizhangben.app.ui.theme.LocalThemeManager
import com.jizhangben.app.ui.theme.ThemeMode
import com.jizhangben.app.ui.utils.BudgetManager
import com.jizhangben.app.ui.utils.CsvUtils
import com.jizhangben.app.ui.utils.LocalBudgetManager
import com.jizhangben.app.ui.utils.LocalBookManager
import com.jizhangben.app.ui.utils.LocalReminderManager
import com.jizhangben.app.ui.utils.LocalSecurityManager
import com.jizhangben.app.ui.utils.ProvideReminderManager
import com.jizhangben.app.ui.utils.ReminderManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jizhangben.app.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: TransactionViewModel, navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showExportResultDialog by remember { mutableStateOf(false) }
    var showImportResultDialog by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var showLoadingDialog by remember { mutableStateOf(false) }
    var exportResult by remember { mutableIntStateOf(0) }
    var importResult by remember { mutableStateOf<CsvUtils.ImportResult?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // 导出文件选择器
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            showLoadingDialog = true
            coroutineScope.launch {
                try {
                    context.contentResolver.openOutputStream(it)?.use { os ->
                        exportResult = viewModel.exportToCsv(os)
                    }
                    showLoadingDialog = false
                    showExportResultDialog = true
                } catch (e: Exception) {
                    showLoadingDialog = false
                    exportResult = -1
                    showExportResultDialog = true
                }
            }
        }
    }

    fun startExport() {
        val fileName = "xiaomeng_jizhang_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
        createDocumentLauncher.launch(fileName)
    }

    // 导入文件选择器
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingImportUri = it
            showImportConfirmDialog = true
        }
    }

    fun startImport() {
        openDocumentLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv", "text/plain", "*/*"))
    }

    // 统计数据
    val allTransactions = viewModel.allTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val totalCount = allTransactions.value.size
    val totalIncome = allTransactions.value.filter { it.type.name == "INCOME" }.sumOf { it.amount }
    val totalExpense = allTransactions.value.filter { it.type.name == "EXPENSE" }.sumOf { it.amount }
    val totalBalance = totalIncome - totalExpense

    // 计算记账天数
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val uniqueDates = allTransactions.value.map { dateFormat.format(it.date) }.distinct()
    val recordDays = uniqueDates.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // 用户信息卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🐱",
                            fontSize = 36.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "小萌记账用户",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "记录每一笔收支，掌控财务自由",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 数据统计卡片
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.DateRange,
                label = "记账天数",
                value = "$recordDays",
                color = Color(0xFF2196F3)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.AccountBalanceWallet,
                label = "账单笔数",
                value = "$totalCount",
                color = Color(0xFF9C27B0)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.AutoGraph,
                label = "当前结余",
                value = "¥${String.format("%.0f", totalBalance)}",
                color = if (totalBalance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 收支汇总卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "收支总览",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "总收入",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "¥${String.format("%.2f", totalIncome)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "总支出",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "¥${String.format("%.2f", totalExpense)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 功能列表
        Text(
            text = "功能",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                // 导出账单
                ListItem(
                    leadingContent = {
                        Icon(Icons.Filled.Upload, contentDescription = null, tint = Color(0xFF4CAF50))
                    },
                    headlineContent = { Text("导出账单") },
                    supportingContent = { Text("导出为CSV表格文件") },
                    trailingContent = { Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.clickable { startExport() }
                )
                HorizontalDivider()

                // 导入账单
                ListItem(
                    leadingContent = {
                        Icon(Icons.Filled.Download, contentDescription = null, tint = Color(0xFF2196F3))
                    },
                    headlineContent = { Text("导入账单") },
                    supportingContent = { Text("支持小萌记账、微信、QQ、支付宝") },
                    trailingContent = { Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.clickable { startImport() }
                )
                HorizontalDivider()

                // 设置
                ListItem(
                    leadingContent = {
                        Icon(Icons.Filled.Settings, contentDescription = null, tint = Color(0xFF607D8B))
                    },
                    headlineContent = { Text("设置") },
                    supportingContent = { Text("应用偏好设置") },
                    trailingContent = { Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.clickable { showSettingsSheet = true }
                )
                HorizontalDivider()

                // 清除数据
                ListItem(
                    leadingContent = {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFF44336))
                    },
                    headlineContent = { Text("清除所有数据", color = Color(0xFFF44336)) },
                    supportingContent = { Text("删除全部记账记录") },
                    trailingContent = { Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.clickable { showClearDataDialog = true }
                )
                HorizontalDivider()

                // 关于
                ListItem(
                    leadingContent = {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF9C27B0))
                    },
                    headlineContent = { Text("关于") },
                    supportingContent = { Text("版本 1.0.0") },
                    trailingContent = { Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.clickable { showAboutDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 理财小贴士
        val tips = listOf(
            "每天坚持记账，养成良好的理财习惯",
            "定期查看统计页面，了解收支情况",
            "将每月收入的30%用于储蓄，积少成多",
            "区分\"需要\"和\"想要\"，理性消费",
            "设置每月预算，避免超支",
            "投资前先做好风险评估，不要把鸡蛋放在一个篮子里"
        )
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val todayTip = tips[dayOfYear % tips.size]

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "💡 今日理财小贴士",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = todayTip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "小萌记账 v1.0.0  ·  Made with ❤️",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }

    // 关于弹窗
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🐱 小萌记账", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "版本 1.1.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "一款简洁、实用的个人记账应用，帮助您轻松记录每一笔收支，掌握自己的财务状况。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "✨ 主要功能",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• 收支记账与分类管理\n• 月度统计与数据可视化\n• 账单编辑与删除\n• CSV账单导入导出\n• 支持微信、QQ、支付宝账单导入\n• 数据本地存储，保护隐私",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "📧 联系我们",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "邮箱：ppdxpz@qq.com\nQQ群：https://qm.qq.com/q/eLl5Fl5s4g",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Made with ❤️  ·  © 2025 小萌记账\nICP备案：正在准备",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("确定")
                }
            }
        )
    }

    // 更新日志弹窗
    if (showChangelogDialog) {
        AlertDialog(
            onDismissRequest = { showChangelogDialog = false },
            title = { Text("更新日志", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("v1.1.0 - 2025.07.12", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("✨ 新增功能")
                    Text("• 更新日志功能")
                    Text("• 深色模式支持（跟随系统/手动切换）")
                    Text("• 动态取色开关（Android 12+）")
                    Text("• 账单搜索与筛选")
                    Text("• 分类管理功能（增删改分类）")
                    Text("• 月度预算管理与超支提醒")
                    Text("• 年度统计视图（月/年切换）")
                    Text("• 应用锁密码保护（6位数字）")
                    Text("• 每日记账提醒通知")
                    Text("• 多账本支持（日常/工作/旅行）")
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("v1.0.0 - 2025.06.01", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🎉 首次发布")
                    Text("• 收支记账与分类管理")
                    Text("• 月度统计与数据可视化")
                    Text("• 账单编辑与删除")
                    Text("• CSV账单导入导出")
                    Text("• 支持微信、QQ、支付宝账单导入")
                    Text("• 数据本地存储，保护隐私")
                }
            },
            confirmButton = {
                TextButton(onClick = { showChangelogDialog = false }) {
                    Text("确定")
                }
            }
        )
    }

    // 清除数据确认弹窗
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("确认清除数据") },
            text = {
                Text("此操作将删除所有记账记录，且不可恢复。确定要继续吗？")
            },
            confirmButton = {
                Button(
                    onClick = {
                        allTransactions.forEach { viewModel.deleteTransaction(it) }
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 设置底部弹窗
    if (showSettingsSheet) {
        val sheetState = rememberModalBottomSheetState()
        var defaultExpense by remember { mutableStateOf(true) }
        var showAmountByDefault by remember { mutableStateOf(true) }
        var showReminderTimePicker by remember { mutableStateOf(false) }
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 外观设置
                Text(
                    text = "外观设置",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val themeManager = LocalThemeManager.current
                var showThemePicker by remember { mutableStateOf(false) }

                ListItem(
                    leadingContent = { Icon(Icons.Filled.Star, contentDescription = null) },
                    headlineContent = { Text("主题模式") },
                    supportingContent = { Text(themeManager.themeMode.displayName) },
                    modifier = Modifier.clickable { showThemePicker = true }
                )
                HorizontalDivider()

                ListItem(
                    leadingContent = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    headlineContent = { Text("动态取色") },
                    supportingContent = { Text("根据壁纸自动变色（Android 12+）") },
                    trailingContent = {
                        Switch(
                            checked = themeManager.dynamicColor,
                            onCheckedChange = { themeManager.dynamicColor = it }
                        )
                    }
                )

                if (showThemePicker) {
                    AlertDialog(
                        onDismissRequest = { showThemePicker = false },
                        title = { Text("选择主题模式") },
                        text = {
                            Column {
                                ThemeMode.values().forEach { mode ->
                                    ListItem(
                                        headlineContent = { Text(mode.displayName) },
                                        trailingContent = {
                                            if (themeManager.themeMode == mode) {
                                                Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                        },
                                        modifier = Modifier.clickable {
                                            themeManager.themeMode = mode
                                            showThemePicker = false
                                        }
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showThemePicker = false }) {
                                Text("取消")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 安全设置
                Text(
                    text = "安全设置",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val securityManager = LocalSecurityManager.current
                var showPasscodeDialog by remember { mutableStateOf(false) }
                var passcodeStep by remember { mutableStateOf(1) }
                var passcode1 by remember { mutableStateOf("") }
                var passcode2 by remember { mutableStateOf("") }
                var passcodeError by remember { mutableStateOf<String?>(null) }

                fun openPasscodeSetup() {
                    passcodeStep = 1
                    passcode1 = ""
                    passcode2 = ""
                    passcodeError = null
                    showPasscodeDialog = true
                }

                ListItem(
                    leadingContent = { Icon(Icons.Filled.Security, contentDescription = null, tint = Color(0xFF9C27B0)) },
                    headlineContent = { Text("应用锁") },
                    supportingContent = { Text(if (securityManager.isPasscodeEnabled) "已开启" else "未开启") },
                    trailingContent = {
                        Switch(
                            checked = securityManager.isPasscodeEnabled,
                            onCheckedChange = { enabled ->
                                if (!enabled) {
                                    securityManager.removePasscode()
                                } else {
                                    openPasscodeSetup()
                                }
                            }
                        )
                    }
                )
                HorizontalDivider()

                if (securityManager.isPasscodeEnabled) {
                    ListItem(
                        leadingContent = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                        headlineContent = { Text("修改密码") },
                        supportingContent = { Text("更改6位数字密码") },
                        modifier = Modifier.clickable { openPasscodeSetup() }
                    )
                    HorizontalDivider()
                }

                if (showPasscodeDialog) {
                    AlertDialog(
                        onDismissRequest = { showPasscodeDialog = false },
                        title = { Text(if (passcodeStep == 1) "设置新密码" else "确认密码") },
                        text = {
                            Column {
                                Text(
                                    if (passcodeStep == 1) "请输入6位数字密码" else "请再次输入密码",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = if (passcodeStep == 1) passcode1 else passcode2,
                                    onValueChange = { value ->
                                        val newValue = value.filter { c -> c.isDigit() }.take(6)
                                        if (passcodeStep == 1) passcode1 = newValue else passcode2 = newValue
                                        passcodeError = null
                                    },
                                    label = { Text("密码") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    isError = passcodeError != null
                                )
                                if (passcodeError != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = passcodeError!!,
                                        color = Color(0xFFF44336),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (passcodeStep == 1) {
                                    if (passcode1.length == 6) {
                                        passcodeStep = 2
                                    } else {
                                        passcodeError = "请输入6位数字密码"
                                    }
                                } else {
                                    if (passcode2.length == 6 && passcode1 == passcode2) {
                                        securityManager.savePasscode(passcode1)
                                        showPasscodeDialog = false
                                    } else if (passcode1 != passcode2) {
                                        passcodeError = "两次密码不一致"
                                        passcode2 = ""
                                    } else {
                                        passcodeError = "请输入6位数字密码"
                                    }
                                }
                            }) {
                                Text(if (passcodeStep == 1) "下一步" else "确认")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPasscodeDialog = false }) {
                                Text("取消")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 记账设置
                Text(
                    text = "记账设置",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                ListItem(
                    leadingContent = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) },
                    headlineContent = { Text("默认记账类型") },
                    supportingContent = { Text(if (defaultExpense) "支出" else "收入") },
                    trailingContent = {
                        TextButton(onClick = { defaultExpense = !defaultExpense }) {
                            Text(if (defaultExpense) "切换为收入" else "切换为支出")
                        }
                    }
                )
                HorizontalDivider()

                ListItem(
                    leadingContent = { Icon(Icons.Filled.AutoGraph, contentDescription = null) },
                    headlineContent = { Text("首页显示金额") },
                    supportingContent = { Text("在首页默认显示收支金额") },
                    trailingContent = {
                        Switch(
                            checked = showAmountByDefault,
                            onCheckedChange = { showAmountByDefault = it }
                        )
                    }
                )
                HorizontalDivider()

                ListItem(
                    leadingContent = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                    headlineContent = { Text("每日记账提醒") },
                    supportingContent = {
                        val reminderManager = LocalReminderManager.current
                        Text(if (reminderManager.isEnabled) "每天 ${String.format("%02d:%02d", reminderManager.reminderHour, reminderManager.reminderMinute)}" else "未开启")
                    },
                    trailingContent = {
                        val reminderManager = LocalReminderManager.current
                        Switch(
                            checked = reminderManager.isEnabled,
                            onCheckedChange = { reminderManager.isEnabled = it }
                        )
                    },
                    modifier = Modifier.clickable {
                        showReminderTimePicker = true
                    }
                )
                HorizontalDivider()

                if (showReminderTimePicker) {
                    val reminderManager = LocalReminderManager.current
                    var tempHour by remember { mutableStateOf(reminderManager.reminderHour) }
                    var tempMinute by remember { mutableStateOf(reminderManager.reminderMinute) }

                    AlertDialog(
                        onDismissRequest = { showReminderTimePicker = false },
                        title = { Text("设置提醒时间") },
                        text = {
                            Column {
                                Text("选择每天提醒记账的时间")
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = String.format("%02d", tempHour),
                                        onValueChange = { value ->
                                            val v = value.filter { c -> c.isDigit() }.take(2)
                                            tempHour = (v.toIntOrNull() ?: 0).coerceIn(0, 23)
                                        },
                                        modifier = Modifier.width(80.dp),
                                        label = { Text("时") },
                                        singleLine = true,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(":", style = MaterialTheme.typography.titleLarge)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = String.format("%02d", tempMinute),
                                        onValueChange = { value ->
                                            val v = value.filter { c -> c.isDigit() }.take(2)
                                            tempMinute = (v.toIntOrNull() ?: 0).coerceIn(0, 59)
                                        },
                                        modifier = Modifier.width(80.dp),
                                        label = { Text("分") },
                                        singleLine = true,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                reminderManager.reminderHour = tempHour
                                reminderManager.reminderMinute = tempMinute
                                if (!reminderManager.isEnabled) {
                                    reminderManager.isEnabled = true
                                }
                                showReminderTimePicker = false
                            }) {
                                Text("保存")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showReminderTimePicker = false }) {
                                Text("取消")
                            }
                        }
                    )
                }

                ListItem(
                    leadingContent = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    headlineContent = { Text("分类管理") },
                    supportingContent = { Text("管理收支分类") },
                    modifier = Modifier.clickable {
                        showSettingsSheet = false
                        navController.navigate("category_manage")
                    }
                )
                HorizontalDivider()

                var showBudgetDialog by remember { mutableStateOf(false) }
                val budgetManager = LocalBudgetManager.current
                var budgetInput by remember { mutableStateOf(budgetManager.monthlyBudget.let { if (it > 0) it.toString() else "" }) }

                ListItem(
                    leadingContent = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) },
                    headlineContent = { Text("月度预算") },
                    supportingContent = { Text(if (budgetManager.monthlyBudget > 0) "¥${budgetManager.monthlyBudget.toInt()}/月" else "未设置") },
                    modifier = Modifier.clickable { showBudgetDialog = true }
                )

                if (showBudgetDialog) {
                    AlertDialog(
                        onDismissRequest = { showBudgetDialog = false },
                        title = { Text("设置月度预算") },
                        text = {
                            OutlinedTextField(
                                value = budgetInput,
                                onValueChange = { value -> budgetInput = value.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("预算金额（元）") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val amount = budgetInput.toDoubleOrNull() ?: 0.0
                                budgetManager.monthlyBudget = amount
                                showBudgetDialog = false
                            }) {
                                Text("保存")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showBudgetDialog = false }) {
                                Text("取消")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 账本管理
                Text(
                    text = "账本管理",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val bookManager = LocalBookManager.current
                var showBookManager by remember { mutableStateOf(false) }

                ListItem(
                    leadingContent = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFFF9800)) },
                    headlineContent = { Text("当前账本") },
                    supportingContent = { Text(bookManager.currentBook) },
                    modifier = Modifier.clickable { showBookManager = true }
                )
                HorizontalDivider()

                ListItem(
                    leadingContent = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    headlineContent = { Text("管理账本") },
                    supportingContent = { Text("添加、编辑、删除账本") },
                    modifier = Modifier.clickable { showBookManager = true }
                )

                if (showBookManager) {
                    var bookDialogMode by remember { mutableStateOf(0) }
                    var newBookName by remember { mutableStateOf("") }
                    var editingBook by remember { mutableStateOf("") }
                    var bookError by remember { mutableStateOf<String?>(null) }

                    fun openAddBook() {
                        bookDialogMode = 1
                        newBookName = ""
                        bookError = null
                    }

                    fun openRenameBook(name: String) {
                        bookDialogMode = 2
                        editingBook = name
                        newBookName = name
                        bookError = null
                    }

                    AlertDialog(
                        onDismissRequest = { showBookManager = false },
                        title = { Text("账本管理") },
                        text = {
                            Column {
                                Text(
                                    text = "选择当前账本",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                bookManager.bookList.forEach { book ->
                                    ListItem(
                                        headlineContent = { Text(book) },
                                        trailingContent = {
                                            Row {
                                                if (bookManager.currentBook == book) {
                                                    Icon(
                                                        Icons.Filled.Star,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier.clickable {
                                            bookManager.currentBook = book
                                        }
                                    )
                                    HorizontalDivider()
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                if (bookDialogMode == 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TextButton(onClick = { openAddBook() }) {
                                            Text("+ 新建账本")
                                        }
                                    }
                                }
                                if (bookDialogMode > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = newBookName,
                                        onValueChange = { newBookName = it; bookError = null },
                                        label = { Text(if (bookDialogMode == 1) "新账本名称" else "新名称") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        isError = bookError != null
                                    )
                                    if (bookError != null) {
                                        Text(
                                            text = bookError!!,
                                            color = Color(0xFFF44336),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            if (bookDialogMode > 0) {
                                TextButton(onClick = {
                                    if (bookDialogMode == 1) {
                                        if (bookManager.addBook(newBookName)) {
                                            bookManager.currentBook = newBookName
                                            showBookManager = false
                                        } else {
                                            bookError = "账本已存在或名称为空"
                                        }
                                    } else {
                                        if (bookManager.renameBook(editingBook, newBookName)) {
                                            showBookManager = false
                                        } else {
                                            bookError = "名称无效或已存在"
                                        }
                                    }
                                }) {
                                    Text("确定")
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                if (bookDialogMode > 0) {
                                    bookDialogMode = 0
                                } else {
                                    showBookManager = false
                                }
                            }) {
                                Text(if (bookDialogMode > 0) "返回" else "关闭")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 数据管理
                Text(
                    text = "数据管理",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                ListItem(
                    leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                    headlineContent = { Text("数据存储") },
                    supportingContent = { Text("数据存储在本地，保护您的隐私") }
                )
                HorizontalDivider()

                ListItem(
                    leadingContent = { Icon(Icons.Filled.Upload, contentDescription = null) },
                    headlineContent = { Text("备份数据") },
                    supportingContent = { Text("导出CSV文件备份数据") },
                    modifier = Modifier.clickable {
                        showSettingsSheet = false
                        startExport()
                    }
                )
                HorizontalDivider()

                ListItem(
                    leadingContent = { Icon(Icons.Filled.Download, contentDescription = null) },
                    headlineContent = { Text("恢复数据") },
                    supportingContent = { Text("从CSV文件导入数据") },
                    modifier = Modifier.clickable {
                        showSettingsSheet = false
                        startImport()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 关于应用
                Text(
                    text = "关于应用",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                ListItem(
                    leadingContent = { Icon(Icons.Filled.Star, contentDescription = null) },
                    headlineContent = { Text("当前版本") },
                    supportingContent = { Text("v1.1.0") }
                )
                HorizontalDivider()

                ListItem(
                    leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                    headlineContent = { Text("更新日志") },
                    supportingContent = { Text("查看版本更新内容") },
                    modifier = Modifier.clickable {
                        showSettingsSheet = false
                        showChangelogDialog = true
                    }
                )
                HorizontalDivider()

                ListItem(
                    leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                    headlineContent = { Text("关于小萌记账") },
                    supportingContent = { Text("了解应用详情") },
                    modifier = Modifier.clickable {
                        showSettingsSheet = false
                        showAboutDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // 加载中对话框
    if (showLoadingDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("处理中") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("请稍候...")
                }
            },
            confirmButton = {}
        )
    }

    // 导出结果对话框
    if (showExportResultDialog) {
        AlertDialog(
            onDismissRequest = { showExportResultDialog = false },
            title = { Text("导出结果") },
            text = {
                if (exportResult >= 0) {
                    Text("导出成功！共导出 $exportResult 条账单记录。")
                } else {
                    Text("导出失败，请重试。")
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportResultDialog = false }) {
                    Text("确定")
                }
            }
        )
    }

    // 导入确认对话框
    if (showImportConfirmDialog && pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirmDialog = false
                pendingImportUri = null
            },
            title = { Text("确认导入") },
            text = {
                Text("导入的账单将追加到现有数据中，确定要继续吗？")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImportConfirmDialog = false
                        val uri = pendingImportUri
                        pendingImportUri = null
                        uri?.let {
                            showLoadingDialog = true
                            coroutineScope.launch {
                                try {
                                    context.contentResolver.openInputStream(it)?.use { inputStream ->
                                        importResult = viewModel.importFromCsv(inputStream)
                                    }
                                    showLoadingDialog = false
                                    showImportResultDialog = true
                                } catch (e: Exception) {
                                    showLoadingDialog = false
                                    importResult = CsvUtils.ImportResult(
                                        transactions = emptyList(),
                                        totalLines = 0,
                                        successCount = 0,
                                        failedCount = 0,
                                        format = CsvUtils.CsvFormat.CUSTOM
                                    )
                                    showImportResultDialog = true
                                }
                            }
                        }
                    }
                ) {
                    Text("确认导入")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportConfirmDialog = false
                    pendingImportUri = null
                }) {
                    Text("取消")
                }
            }
        )
    }

    // 导入结果对话框
    if (showImportResultDialog && importResult != null) {
        val result = importResult!!
        AlertDialog(
            onDismissRequest = {
                showImportResultDialog = false
                importResult = null
            },
            title = { Text("导入结果") },
            text = {
                Column {
                    Text("识别格式：${result.format.displayName}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("总计行数：${result.totalLines}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "成功导入：${result.successCount} 条",
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "失败行数：${result.failedCount} 条",
                        color = if (result.failedCount > 0) Color(0xFFF44336) else Color(0xFF9E9E9E)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showImportResultDialog = false
                    importResult = null
                }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
