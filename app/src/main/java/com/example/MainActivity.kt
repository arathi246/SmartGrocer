package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ai.AiAssistantScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.deliveries.DeliveriesScreen
import com.example.ui.inventory.AddEditProductDialog
import com.example.ui.inventory.InventoryScreen
import com.example.ui.more.MoreScreen
import com.example.ui.orders.CreateOrderScreen
import com.example.ui.orders.OrderDetailDialog
import com.example.ui.orders.OrdersScreen
import com.example.ui.payments.PaymentsScreen
import com.example.ui.payments.RecordPaymentDialog
import com.example.ui.reports.ReportsScreen
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.NavigationDestination
import kotlinx.coroutines.flow.collectLatest

data class BottomNavItem(
    val label: String,
    val destination: NavigationDestination,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SmartFoodApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartFoodApp(viewModel: MainViewModel) {
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showQuickAddProductDialog by remember { mutableStateOf(false) }
    var showQuickRecordPaymentDialog by remember { mutableStateOf(false) }

    // Listen for UI snackbar messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem(
            label = "Dashboard",
            destination = NavigationDestination.Dashboard,
            selectedIcon = Icons.Filled.Dashboard,
            unselectedIcon = Icons.Outlined.Dashboard,
            testTag = "nav_dashboard"
        ),
        BottomNavItem(
            label = "Orders",
            destination = NavigationDestination.Orders,
            selectedIcon = Icons.Filled.Assignment,
            unselectedIcon = Icons.Outlined.Assignment,
            testTag = "nav_orders"
        ),
        BottomNavItem(
            label = "Inventory",
            destination = NavigationDestination.Inventory,
            selectedIcon = Icons.Filled.Inventory2,
            unselectedIcon = Icons.Outlined.Inventory2,
            testTag = "nav_inventory"
        ),
        BottomNavItem(
            label = "Payments",
            destination = NavigationDestination.Payments,
            selectedIcon = Icons.Filled.Payments,
            unselectedIcon = Icons.Outlined.Payments,
            testTag = "nav_payments"
        ),
        BottomNavItem(
            label = "More",
            destination = NavigationDestination.More,
            selectedIcon = Icons.Filled.MoreHoriz,
            unselectedIcon = Icons.Outlined.MoreHoriz,
            testTag = "nav_more"
        )
    )

    val isTopLevelScreen = currentDestination is NavigationDestination.Dashboard ||
        currentDestination is NavigationDestination.Orders ||
        currentDestination is NavigationDestination.Inventory ||
        currentDestination is NavigationDestination.Payments ||
        currentDestination is NavigationDestination.More

    BackHandler(enabled = !isTopLevelScreen) {
        viewModel.navigateBack()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_nav_bar"),
                    tonalElevation = 4.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = when (item.destination) {
                            NavigationDestination.Dashboard -> currentDestination is NavigationDestination.Dashboard
                            NavigationDestination.Orders -> currentDestination is NavigationDestination.Orders
                            NavigationDestination.Inventory -> currentDestination is NavigationDestination.Inventory
                            NavigationDestination.Payments -> currentDestination is NavigationDestination.Payments
                            NavigationDestination.More -> currentDestination is NavigationDestination.More
                            else -> false
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(item.destination) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = GreenPrimary.copy(alpha = 0.15f),
                                selectedIconColor = GreenPrimary,
                                selectedTextColor = GreenPrimary
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val dest = currentDestination) {
                is NavigationDestination.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onAddProductClick = { showQuickAddProductDialog = true },
                        onRecordPaymentClick = { showQuickRecordPaymentDialog = true }
                    )
                }
                is NavigationDestination.Orders -> {
                    OrdersScreen(viewModel = viewModel)
                }
                is NavigationDestination.Inventory -> {
                    InventoryScreen(viewModel = viewModel)
                }
                is NavigationDestination.Payments -> {
                    PaymentsScreen(viewModel = viewModel)
                }
                is NavigationDestination.More -> {
                    MoreScreen(viewModel = viewModel)
                }
                is NavigationDestination.CreateOrder -> {
                    CreateOrderScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                is NavigationDestination.Deliveries -> {
                    DeliveriesScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                is NavigationDestination.Reports -> {
                    ReportsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                is NavigationDestination.AiAssistant -> {
                    AiAssistantScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                is NavigationDestination.OrderDetail -> {
                    val order = orders.firstOrNull { it.id == dest.orderId }
                    if (order != null) {
                        OrderDetailDialog(
                            order = order,
                            viewModel = viewModel,
                            onDismiss = { viewModel.navigateBack() }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }
                else -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onAddProductClick = { showQuickAddProductDialog = true },
                        onRecordPaymentClick = { showQuickRecordPaymentDialog = true }
                    )
                }
            }
        }
    }

    // Global quick action dialogs from Dashboard
    if (showQuickAddProductDialog) {
        AddEditProductDialog(
            initialProduct = null,
            onSave = { newProd -> viewModel.addProduct(newProd) },
            onDismiss = { showQuickAddProductDialog = false }
        )
    }

    if (showQuickRecordPaymentDialog) {
        RecordPaymentDialog(
            customers = customers,
            preselectedCustomer = null,
            onConfirm = { payment -> viewModel.recordPayment(payment) {} },
            onDismiss = { showQuickRecordPaymentDialog = false }
        )
    }
}
