package com.jizhangben.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jizhangben.app.ui.screens.AddTransactionScreen
import com.jizhangben.app.ui.screens.CategoryBudgetScreen
import com.jizhangben.app.ui.screens.CategoryManageScreen
import com.jizhangben.app.ui.screens.HomeScreen
import com.jizhangben.app.ui.screens.ProfileScreen
import com.jizhangben.app.ui.screens.RecurringTransactionScreen
import com.jizhangben.app.ui.screens.ReportScreen
import com.jizhangben.app.ui.screens.StatsScreen
import com.jizhangben.app.ui.screens.TransactionDetailScreen
import com.jizhangben.app.viewmodel.TransactionViewModel

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
fun JiZhangBenApp(viewModel: TransactionViewModel) {
    val navController = rememberNavController()

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
