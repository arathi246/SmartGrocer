package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.OrderEntity
import com.example.ui.components.FefoBadge
import com.example.ui.components.Formatters
import com.example.ui.components.LowStockBadge
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.NavigationDestination

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onAddProductClick: () -> Unit,
    onRecordPaymentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val warnDays by viewModel.expiryWarningDays.collectAsStateWithLifecycle()

    val now = System.currentTimeMillis()
    val lowStockList = products.filter { it.isLowStock }
    val nearExpiryList = products.filter { it.isNearingExpiry(warnDays, now) || it.isExpired(now) }
        .sortedBy { it.expiryDateEpoch ?: Long.MAX_VALUE } // FEFO principle!

    val pendingOrdersList = orders.filter {
        it.status == OrderEntity.STATUS_DRAFT ||
        it.status == OrderEntity.STATUS_CONFIRMED ||
        it.status == OrderEntity.STATUS_PACKED
    }.take(3)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome & Business Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GreenPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Smart Food Distribution",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Daily Operations & Dispatch Hub",
                                color = Color(0xFFC8E6C9),
                                fontSize = 13.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Quick Action Buttons
                    Text(
                        text = "QUICK ACTIONS",
                        color = Color(0xFFA5D6A7),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(NavigationDestination.CreateOrder) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_add_order_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberAccent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onAddProductClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_add_product_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = GreenPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Product", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onRecordPaymentClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_record_payment_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Payment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Summary Metric Cards
        item {
            Text(
                text = "OPERATIONAL OVERVIEW",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Orders Awaiting Processing
        item {
            MetricCard(
                title = "Orders Awaiting Processing",
                value = "${metrics.pendingOrdersCount}",
                subtitle = "Draft, Confirmed & Packed",
                icon = Icons.Default.Assignment,
                iconBgColor = Color(0xFFFFF3E0),
                iconTint = AmberAccent,
                onClick = {
                    viewModel.ordersStatusFilter.value = "All"
                    viewModel.navigateTo(NavigationDestination.Orders)
                },
                testTag = "card_pending_orders"
            )
        }

        // 2. Products Low in Stock
        item {
            MetricCard(
                title = "Products Low in Stock",
                value = "${metrics.lowStockCount}",
                subtitle = "Items at or below reorder threshold",
                icon = Icons.Default.Warning,
                iconBgColor = Color(0xFFFFEBEE),
                iconTint = StatusDanger,
                onClick = {
                    viewModel.inventoryQuickFilter.value = "Low Stock"
                    viewModel.navigateTo(NavigationDestination.Inventory)
                },
                testTag = "card_low_stock"
            )
        }

        // 3. Products Nearing Expiry (FEFO)
        item {
            MetricCard(
                title = "Products Nearing Expiry (FEFO)",
                value = "${metrics.nearExpiryCount}",
                subtitle = "Expiring within $warnDays days (Dispatch first)",
                icon = Icons.Default.EventNote,
                iconBgColor = Color(0xFFFFF8E1),
                iconTint = StatusWarning,
                onClick = {
                    viewModel.inventoryQuickFilter.value = "Expiring Soon"
                    viewModel.inventorySortBy.value = "Expiry (FEFO)"
                    viewModel.navigateTo(NavigationDestination.Inventory)
                },
                testTag = "card_near_expiry"
            )
        }

        // 4. Total Outstanding Customer Payments
        item {
            MetricCard(
                title = "Outstanding Customer Balance",
                value = Formatters.formatCurrency(metrics.totalOutstandingPayments),
                subtitle = "Total unpaid deliveries pending collection",
                icon = Icons.Default.Payments,
                iconBgColor = Color(0xFFE8F5E9),
                iconTint = GreenPrimary,
                onClick = {
                    viewModel.customerFilterOutstandingOnly.value = true
                    viewModel.navigateTo(NavigationDestination.Payments)
                },
                testTag = "card_outstanding_payments"
            )
        }

        // 5. Deliveries Still Pending
        item {
            MetricCard(
                title = "Deliveries in Progress / Pending",
                value = "${metrics.pendingDeliveriesCount}",
                subtitle = "Awaiting vehicle dispatch or offloading",
                icon = Icons.Default.LocalShipping,
                iconBgColor = Color(0xFFE1F5FE),
                iconTint = StatusInfo,
                onClick = {
                    viewModel.deliveryFilter.value = "Pending"
                    viewModel.navigateTo(NavigationDestination.Deliveries)
                },
                testTag = "card_pending_deliveries"
            )
        }

        // Quick Sections: FEFO Dispatch Priority
        if (nearExpiryList.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FEFO Dispatch Priority",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "View All",
                        color = GreenPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            viewModel.inventoryQuickFilter.value = "Expiring Soon"
                            viewModel.inventorySortBy.value = "Expiry (FEFO)"
                            viewModel.navigateTo(NavigationDestination.Inventory)
                        }
                    )
                }
            }

            items(nearExpiryList.take(3)) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Stock: ${product.quantity} ${product.unit} | SKU: ${product.sku}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FefoBadge(daysUntilExpiry = product.daysUntilExpiry(now))
                    }
                }
            }
        }

        // Orders Awaiting Action
        if (pendingOrdersList.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Orders Awaiting Action",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View All",
                        color = GreenPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            viewModel.navigateTo(NavigationDestination.Orders)
                        }
                    )
                }
            }

            items(pendingOrdersList) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(NavigationDestination.OrderDetail(order.id)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = order.orderNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(status = order.status)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = order.customerName,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = Formatters.formatCurrency(order.totalAmount),
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
