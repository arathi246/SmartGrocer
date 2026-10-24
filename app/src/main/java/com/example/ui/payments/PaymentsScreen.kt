package com.example.ui.payments

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.CustomerEntity
import com.example.data.entity.PaymentEntity
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.viewmodel.CustomerFinancialSummary
import com.example.viewmodel.MainViewModel

@Composable
fun PaymentsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val financials by viewModel.customerFinancials.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val searchQuery by viewModel.customerSearch.collectAsStateWithLifecycle()
    val outstandingOnly by viewModel.customerFilterOutstandingOnly.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showRecordPaymentDialog by remember { mutableStateOf(false) }
    var preselectedCustomerForPayment by remember { mutableStateOf<CustomerEntity?>(null) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var deletingPayment by remember { mutableStateOf<PaymentEntity?>(null) }

    // Filter customers
    val filteredFinancials = financials.filter { item ->
        val matchSearch = searchQuery.isBlank() ||
            item.customer.name.contains(searchQuery, ignoreCase = true) ||
            item.customer.phone.contains(searchQuery, ignoreCase = true)

        val matchOutstanding = !outstandingOnly || item.outstandingBalance > 0.0
        matchSearch && matchOutstanding
    }.sortedByDescending { it.outstandingBalance }

    val totalOutstanding = financials.sumOf { it.outstandingBalance }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    preselectedCustomerForPayment = null
                    showRecordPaymentDialog = true
                },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("record_payment_fab")
            ) {
                Icon(Icons.Default.Payment, contentDescription = "Record Payment")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("payments_screen")
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = GreenPrimary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Customers & Balances", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Payment Receipts (${payments.size})", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTabIndex == 0) {
                // Total Outstanding Header Banner
                Surface(
                    color = GreenPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Outstanding Balance", color = Color(0xFFA5D6A7), fontSize = 12.sp)
                            Text(
                                Formatters.formatCurrency(totalOutstanding),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                        Button(
                            onClick = { showAddCustomerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Shop", fontSize = 12.sp)
                        }
                    }
                }

                // Search & Filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.customerSearch.value = it },
                        placeholder = { Text("Search shop name or phone...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.customerSearch.value = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilterChip(
                        selected = outstandingOnly,
                        onClick = { viewModel.customerFilterOutstandingOnly.value = !outstandingOnly },
                        label = { Text("Due Only", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFEBEE),
                            selectedLabelColor = StatusDanger
                        )
                    )
                }

                // List of Customers
                if (filteredFinancials.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Person,
                        title = "No Customers Found",
                        description = if (outstandingOnly) "No customers currently have outstanding balances!" else "No customers match your search.",
                        actionButtonText = if (customers.isEmpty()) "Add First Customer" else null,
                        onActionClick = { showAddCustomerDialog = true }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredFinancials, key = { it.customer.id }) { item ->
                            CustomerFinancialCard(
                                summary = item,
                                onRecordPayment = {
                                    preselectedCustomerForPayment = item.customer
                                    showRecordPaymentDialog = true
                                },
                                modifier = Modifier.testTag("customer_card_${item.customer.id}")
                            )
                        }
                    }
                }
            } else {
                // Payment Receipts History Tab
                if (payments.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Payments,
                        title = "No Payment Receipts",
                        description = "Recorded payments and settlement receipts will appear here.",
                        actionButtonText = "Record Payment",
                        onActionClick = {
                            preselectedCustomerForPayment = null
                            showRecordPaymentDialog = true
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        items(payments, key = { it.id }) { payment ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(payment.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            "${Formatters.formatDateTime(payment.paymentDateEpoch)} • ${payment.paymentMethod}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (payment.referenceNumber.isNotBlank()) {
                                            Text("Ref: ${payment.referenceNumber}", fontSize = 11.sp, color = GreenPrimary)
                                        }
                                        if (payment.notes.isNotBlank()) {
                                            Text("Note: ${payment.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            Formatters.formatCurrency(payment.amount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = StatusSuccess
                                        )
                                        IconButton(onClick = { deletingPayment = payment }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Payment", tint = StatusDanger, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRecordPaymentDialog) {
        RecordPaymentDialog(
            customers = customers,
            preselectedCustomer = preselectedCustomerForPayment,
            onConfirm = { payment ->
                viewModel.recordPayment(payment) { /* completed */ }
            },
            onDismiss = {
                showRecordPaymentDialog = false
                preselectedCustomerForPayment = null
            }
        )
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onSave = { customer ->
                viewModel.addCustomer(customer)
                showAddCustomerDialog = false
            },
            onDismiss = { showAddCustomerDialog = false }
        )
    }

    deletingPayment?.let { pay ->
        ConfirmDeleteDialog(
            title = "Delete Payment Receipt?",
            message = "Are you sure you want to delete this payment of ₹${pay.amount} for ${pay.customerName}? The customer's outstanding balance will increase accordingly.",
            onConfirm = { viewModel.deletePayment(pay) },
            onDismiss = { deletingPayment = null }
        )
    }
}

@Composable
fun CustomerFinancialCard(
    summary: CustomerFinancialSummary,
    onRecordPayment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.customer.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (summary.customer.phone.isNotBlank()) {
                        Text(
                            text = summary.customer.phone,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (summary.customer.address.isNotBlank()) {
                        Text(
                            text = summary.customer.address,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Balance display
                Column(horizontalAlignment = Alignment.End) {
                    Text("Outstanding Balance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = Formatters.formatCurrency(summary.outstandingBalance),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (summary.outstandingBalance > 0.0) StatusDanger else StatusSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub details: Billed vs Paid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Billed: ${Formatters.formatCurrency(summary.totalBilled)} | Paid: ${Formatters.formatCurrency(summary.totalPaid)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onRecordPayment,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Record Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onSave: (CustomerEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add New Shop / Customer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                errorMessage?.let {
                    Text(it, color = StatusDanger, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Shop / Customer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Delivery Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. GST, Payment cycle)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Shop / Customer name is required."
                                return@Button
                            }
                            onSave(
                                CustomerEntity(
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    address = address.trim(),
                                    notes = notes.trim(),
                                    isDemo = false
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Shop")
                    }
                }
            }
        }
    }
}
