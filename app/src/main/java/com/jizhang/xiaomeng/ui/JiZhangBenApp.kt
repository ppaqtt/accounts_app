package com.jizhang.xiaomeng.ui

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jizhang.xiaomeng.ui.screens.AddTransactionScreen
import com.jizhang.xiaomeng.ui.screens.CategoryBudgetScreen
import com.jizhang.xiaomeng.ui.screens.CategoryManageScreen
import com.jizhang.xiaomeng.ui.screens.HomeScreen
import com.jizhang.xiaomeng.ui.screens.ProfileScreen
import com.jizhang.xiaomeng.ui.screens.RecurringTransactionScreen
import com.jizhang.xiaomeng.ui.screens.ReportScreen
import com.jizhang.xiaomeng.ui.screens.StatsScreen
import com.jizhang.xiaomeng.ui.screens.TransactionDetailScreen
import com.jizhang.xiaomeng.ui.utils.AppChangelog
import com.jizhang.xiaomeng.viewmodel.TransactionViewModel

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "首页", Icons.Filled.Home)
    object Add : Screen("add", "记账", Icons.Filled.Add)
    object Stats : Screen("stats", "统计", Icons.Filled.BarChart)
    object Profile : Screen("profile", "我的", Icons.Filled.Person)
}

val items = listOf(
    Screen.Home,
    Screen.Add,
    Screen.Stats,
    Screen.Profile
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JiZhangBenApp(
    viewModel: TransactionViewModel,
    openAddRequest: Int = 0,
    onOpenAddHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val changelogPrefs = remember {
        context.getSharedPreferences("changelog_prefs", Context.MODE_PRIVATE)
    }
    var showWhatsNew by remember {
        mutableStateOf(changelogPrefs.getString("seen_version", null) != AppChangelog.currentVersion)
    }
    fun dismissWhatsNew() {
        changelogPrefs.edit().putString("seen_version", AppChangelog.currentVersion).apply()
        showWhatsNew = false
    }

    LaunchedEffect(openAddRequest) {
        if (openAddRequest > 0) {
            navController.navigate("add") { launchSingleTop = true }
            onOpenAddHandled()
        }
    }

    if (showWhatsNew) {
        AlertDialog(
            onDismissRequest = { dismissWhatsNew() },
            title = { Text("🐱 新版本 v${AppChangelog.currentVersion}") },
            text = {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    AppChangelog.currentEntries.forEach { entry ->
                        Text("• $entry")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dismissWhatsNew() }) { Text("知道了") }
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel, navController = navController)
            }
            composable(Screen.Add.route) {
                AddTransactionScreen(viewModel = viewModel, navController = navController)
            }
            composable("edit/{transactionId}") { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId")?.toLongOrNull() ?: -1L
                AddTransactionScreen(
                    viewModel = viewModel,
                    navController = navController,
                    transactionId = transactionId
                )
            }
            composable(Screen.Stats.route) {
                StatsScreen(viewModel = viewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(viewModel = viewModel, navController = navController)
            }
            composable("category_manage") {
                CategoryManageScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("transaction_detail/{transactionId}") { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId")?.toLongOrNull() ?: 0L
                TransactionDetailScreen(
                    viewModel = viewModel,
                    transactionId = transactionId,
                    navController = navController
                )
            }
            composable("recurring_transactions") {
                RecurringTransactionScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("category_budget") {
                CategoryBudgetScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("report") {
                ReportScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
