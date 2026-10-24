package com.example.ui.orders

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.Formatters
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailDialog(
    order: OrderEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var items by remember { mutableStateOf<List<OrderItemEntity>>(emptyList()) }
    var selectedStatus by remember { mutableStateOf(order.status) }
    var statusExpanded by remember { mutableStateOf(false) }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(order.id) {
        items = viewModel.getOrderItems(order.id)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = order.orderNumber,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Formatters.formatDateTime(order.orderDateEpoch),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(status = order.status)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Customer Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = order.customerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (order.customerPhone.isNotBlank()) {
                            Text("Phone: ${order.customerPhone}", fontSize = 12.sp)
                        }
                        if (order.deliveryAddress.isNotBlank()) {
                            Text("Address: ${order.deliveryAddress}", fontSize = 12.sp)
                        }
                        if (order.notes.isNotBlank()) {
                            Text("Notes: ${order.notes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stock deduction status notice
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (order.stockDeducted) Color(0xFFE8F5E9) else Color(0xFFFFF8E1),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (order.stockDeducted) Icons.Default.Check else Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = if (order.stockDeducted) StatusSuccess else AmberAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (order.stockDeducted)
                                "Inventory deducted automatically upon dispatch."
                            else
                                "Inventory will be deducted once marked Dispatched.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (order.stockDeducted) StatusSuccess else AmberAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Line Items List
                Text("ORDER ITEMS", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))

                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.productName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(
                                "${item.quantity} ${item.unit} × ₹${item.unitPrice}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            Formatters.formatCurrency(item.totalPrice),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Totals
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", fontSize = 13.sp)
                    Text(Formatters.formatCurrency(order.subtotal), fontSize = 13.sp)
                }
                if (order.discount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Discount", fontSize = 13.sp, color = StatusDanger)
                        Text("-${Formatters.formatCurrency(order.discount)}", fontSize = 13.sp, color = StatusDanger)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        Formatters.formatCurrency(order.totalAmount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GreenPrimary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Update Status Dropdown
                Text("Update Order Status:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedStatus,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        OrderEntity.ALL_STATUSES.forEach { statusOption ->
                            DropdownMenuItem(
                                text = { Text(statusOption) },
                                onClick = {
                                    statusExpanded = false
                                    if (statusOption == OrderEntity.STATUS_CANCELLED) {
                                        showCancelConfirmDialog = true
                                    } else {
                                        selectedStatus = statusOption
                                        viewModel.updateOrderStatus(order.id, statusOption)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Share / Print Button
                OutlinedButton(
                    onClick = {
                        shareOrderSummary(context, order, items)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share / Print Order Summary")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Delete Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showDeleteConfirmDialog = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Order", color = StatusDanger)
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }

    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = { Text("Confirm Order Cancellation", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (order.stockDeducted)
                        "Are you sure you want to cancel ${order.orderNumber}? The deducted inventory stock will be automatically restored to the warehouse."
                    else
                        "Are you sure you want to cancel ${order.orderNumber}?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedStatus = OrderEntity.STATUS_CANCELLED
                        viewModel.updateOrderStatus(order.id, OrderEntity.STATUS_CANCELLED)
                        showCancelConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                ) {
                    Text("Confirm Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text("Keep Order")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        ConfirmDeleteDialog(
            title = "Delete Order?",
            message = "Are you sure you want to permanently delete order ${order.orderNumber}? If stock was already deducted, it will be restored.",
            onConfirm = {
                viewModel.deleteOrder(order)
                onDismiss()
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }
}

fun shareOrderSummary(context: Context, order: OrderEntity, items: List<OrderItemEntity>) {
    val sb = StringBuilder()
    sb.append("==============================\n")
    sb.append("SMART FOOD DISTRIBUTION\n")
    sb.append("ORDER INVOICE / DISPATCH SUMMARY\n")
    sb.append("==============================\n")
    sb.append("Order No: ${order.orderNumber}\n")
    sb.append("Date: ${Formatters.formatDateTime(order.orderDateEpoch)}\n")
    sb.append("Customer: ${order.customerName}\n")
    if (order.customerPhone.isNotBlank()) sb.append("Phone: ${order.customerPhone}\n")
    if (order.deliveryAddress.isNotBlank()) sb.append("Delivery: ${order.deliveryAddress}\n")
    sb.append("Status: ${order.status}\n")
    sb.append("------------------------------\n")
    sb.append("ITEMS:\n")
    for (item in items) {
        sb.append("• ${item.productName}\n")
        sb.append("  ${item.quantity} ${item.unit} × ₹${item.unitPrice} = ₹${item.totalPrice}\n")
    }
    sb.append("------------------------------\n")
    sb.append("Subtotal: ₹${order.subtotal}\n")
    if (order.discount > 0) sb.append("Discount: -₹${order.discount}\n")
    sb.append("TOTAL AMOUNT: ₹${order.totalAmount}\n")
    if (order.notes.isNotBlank()) sb.append("Notes: ${order.notes}\n")
    sb.append("==============================\n")
    sb.append("Thank you for your business!")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Order Summary"))
}
