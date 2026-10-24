package com.example.ui.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.OrderEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GreenPrimary
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.NavigationDestination

@Composable
fun OrdersScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val searchQuery by viewModel.ordersSearch.collectAsStateWithLifecycle()
    val statusFilter by viewModel.ordersStatusFilter.collectAsStateWithLifecycle()

    var selectedOrderForDetail by remember { mutableStateOf<OrderEntity?>(null) }

    val filteredOrders = orders.filter { ord ->
        val matchSearch = searchQuery.isBlank() ||
            ord.orderNumber.contains(searchQuery, ignoreCase = true) ||
            ord.customerName.contains(searchQuery, ignoreCase = true)

        val matchStatus = statusFilter == "All" || ord.status == statusFilter
        matchSearch && matchStatus
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(NavigationDestination.CreateOrder) },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_order_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Order")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("orders_screen")
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.ordersSearch.value = it },
                placeholder = { Text("Search by Order # or Customer...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.ordersSearch.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("orders_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val allStatuses = listOf("All") + OrderEntity.ALL_STATUSES
                allStatuses.forEach { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { viewModel.ordersStatusFilter.value = status },
                        label = { Text(status, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = GreenPrimary
                        )
                    )
                }
            }

            // Count label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${filteredOrders.size} order${if (filteredOrders.size != 1) "s" else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Orders list
            if (filteredOrders.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Assignment,
                    title = "No Orders Found",
                    description = if (searchQuery.isNotBlank() || statusFilter != "All")
                        "No orders match your filter criteria."
                    else
                        "You haven't logged any customer orders yet.",
                    actionButtonText = if (orders.isEmpty()) "Create First Order" else "Clear Filter",
                    onActionClick = {
                        if (orders.isEmpty()) {
                            viewModel.navigateTo(NavigationDestination.CreateOrder)
                        } else {
                            viewModel.ordersSearch.value = ""
                            viewModel.ordersStatusFilter.value = "All"
                        }
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
                        OrderListItemCard(
                            order = order,
                            onClick = { selectedOrderForDetail = order },
                            modifier = Modifier.testTag("order_item_${order.orderNumber}")
                        )
                    }
                }
            }
        }
    }

    selectedOrderForDetail?.let { ord ->
        OrderDetailDialog(
            order = ord,
            viewModel = viewModel,
            onDismiss = { selectedOrderForDetail = null }
        )
    }
}

@Composable
fun OrderListItemCard(
    order: OrderEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.orderNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = order.customerName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (order.deliveryAddress.isNotBlank()) {
                Text(
                    text = order.deliveryAddress,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = Formatters.formatDateTime(order.orderDateEpoch),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = Formatters.formatCurrency(order.totalAmount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GreenPrimary
                )
            }
        }
    }
}
