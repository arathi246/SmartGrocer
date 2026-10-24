package com.example.ui.orders

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.CustomerEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.ProductEntity
import com.example.ui.components.FefoBadge
import com.example.ui.components.Formatters
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.viewmodel.MainViewModel

data class OrderDraftItem(
    val product: ProductEntity,
    var quantity: Double,
    var unitPrice: Double
) {
    val totalPrice: Double
        get() = quantity * unitPrice

    val hasStockWarning: Boolean
        get() = quantity > product.quantity
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val products by viewModel.products.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val existingOrders by viewModel.orders.collectAsStateWithLifecycle()

    // Generate next order number
    val nextOrderNum = remember(existingOrders) {
        val maxId = existingOrders.mapNotNull { ord ->
            ord.orderNumber.removePrefix("ORD-").toIntOrNull()
        }.maxOrNull() ?: 1000
        "ORD-${maxId + 1}"
    }

    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerNameInput by remember { mutableStateOf("") }
    var customerPhoneInput by remember { mutableStateOf("") }
    var customerAddressInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var discountInput by remember { mutableStateOf("0") }
    var orderStatus by remember { mutableStateOf(OrderEntity.STATUS_CONFIRMED) }

    val draftItems = remember { mutableStateListOf<OrderDraftItem>() }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val subtotal = draftItems.sumOf { it.totalPrice }
    val discount = discountInput.toDoubleOrNull() ?: 0.0
    val totalAmount = (subtotal - discount).coerceAtLeast(0.0)

    val anyStockWarning = draftItems.any { it.hasStockWarning }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Order", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("create_order_screen")
        ) {
            // Order Number & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Order Number", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(nextOrderNum, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GreenPrimary)
                }

                // Status picker
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = orderStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Initial Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier
                            .width(160.dp)
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        listOf(
                            OrderEntity.STATUS_DRAFT,
                            OrderEntity.STATUS_CONFIRMED,
                            OrderEntity.STATUS_PACKED,
                            OrderEntity.STATUS_DISPATCHED
                        ).forEach { statusOpt ->
                            DropdownMenuItem(
                                text = { Text(statusOpt) },
                                onClick = {
                                    orderStatus = statusOpt
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error banner if any
            errorMessage?.let {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = it,
                        color = StatusDanger,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Customer Selector / Entry Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Customer / Shop Information", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (customers.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = customerDropdownExpanded,
                            onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCustomer?.name ?: (if (customerNameInput.isNotBlank()) "New: $customerNameInput" else "Select Existing Customer"),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = customerDropdownExpanded,
                                onDismissRequest = { customerDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("+ Enter New Customer Name", color = GreenPrimary, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        selectedCustomer = null
                                        customerNameInput = ""
                                        customerDropdownExpanded = false
                                    }
                                )
                                customers.forEach { cust ->
                                    DropdownMenuItem(
                                        text = { Text(cust.name) },
                                        onClick = {
                                            selectedCustomer = cust
                                            customerNameInput = cust.name
                                            customerPhoneInput = cust.phone
                                            customerAddressInput = cust.address
                                            customerDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (selectedCustomer == null) {
                        OutlinedTextField(
                            value = customerNameInput,
                            onValueChange = { customerNameInput = it },
                            label = { Text("Customer / Shop Name *") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("order_customer_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = customerPhoneInput,
                        onValueChange = { customerPhoneInput = it },
                        label = { Text("Contact Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerAddressInput,
                        onValueChange = { customerAddressInput = it },
                        label = { Text("Delivery Address / Shop Location") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Line Items Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ORDER LINE ITEMS", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
                Button(
                    onClick = { showAddProductDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    modifier = Modifier.testTag("add_item_to_order_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (draftItems.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No products added to this order yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { showAddProductDialog = true }) {
                            Text("+ Tap here to select food products", color = GreenPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                draftItems.forEachIndexed { index, draftItem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (draftItem.hasStockWarning) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(draftItem.product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "In Stock: ${draftItem.product.quantity} ${draftItem.product.unit}",
                                        fontSize = 12.sp,
                                        color = if (draftItem.hasStockWarning) StatusDanger else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { draftItems.removeAt(index) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove Item", tint = StatusDanger)
                                }
                            }

                            if (draftItem.hasStockWarning) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Requested ${draftItem.quantity} exceeds available ${draftItem.product.quantity}!",
                                        color = StatusDanger,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = draftItem.quantity.toString(),
                                    onValueChange = { newVal ->
                                        val q = newVal.toDoubleOrNull() ?: 0.0
                                        draftItems[index] = draftItem.copy(quantity = q)
                                    },
                                    label = { Text("Quantity (${draftItem.product.unit})") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = draftItem.unitPrice.toString(),
                                    onValueChange = { newVal ->
                                        val p = newVal.toDoubleOrNull() ?: 0.0
                                        draftItems[index] = draftItem.copy(unitPrice = p)
                                    },
                                    label = { Text("Price (₹)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Line Total", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        Formatters.formatCurrency(draftItem.totalPrice),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Totals & Discount Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", fontSize = 14.sp)
                        Text(Formatters.formatCurrency(subtotal), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Special Discount (₹)", fontSize = 14.sp)
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.width(120.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("FINAL TOTAL", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            Formatters.formatCurrency(totalAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = GreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Order Notes
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Delivery / Order Instructions (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Stock Warning Notice
            if (anyStockWarning) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusDanger)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "One or more products exceed warehouse stock. Please adjust quantities before saving or restocking.",
                            color = StatusDanger,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Create Order Action Button
            Button(
                onClick = {
                    val custName = customerNameInput.trim()
                    if (custName.isBlank()) {
                        errorMessage = "Customer name is required."
                        return@Button
                    }
                    if (draftItems.isEmpty()) {
                        errorMessage = "Please add at least one line item to the order."
                        return@Button
                    }
                    if (anyStockWarning && orderStatus == OrderEntity.STATUS_DISPATCHED) {
                        errorMessage = "Cannot dispatch order with insufficient warehouse stock!"
                        return@Button
                    }

                    // Auto create customer record if new
                    val customerId = selectedCustomer?.id ?: 0L

                    val order = OrderEntity(
                        orderNumber = nextOrderNum,
                        customerId = customerId,
                        customerName = custName,
                        customerPhone = customerPhoneInput.trim(),
                        deliveryAddress = customerAddressInput.trim(),
                        orderDateEpoch = System.currentTimeMillis(),
                        status = orderStatus,
                        subtotal = subtotal,
                        discount = discount,
                        totalAmount = totalAmount,
                        notes = notesInput.trim(),
                        stockDeducted = false,
                        isDemo = false
                    )

                    val lineItems = draftItems.map { draft ->
                        OrderItemEntity(
                            orderId = 0,
                            productId = draft.product.id,
                            productName = draft.product.name,
                            quantity = draft.quantity,
                            unit = draft.product.unit,
                            unitPrice = draft.unitPrice,
                            totalPrice = draft.totalPrice
                        )
                    }

                    viewModel.createOrder(order, lineItems) { success ->
                        if (success) {
                            onBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_order_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Confirm & Create Order", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Modal to pick and add product to draft
    if (showAddProductDialog) {
        AddProductToOrderDialog(
            products = products,
            onAdd = { product, quantity, price ->
                draftItems.add(OrderDraftItem(product, quantity, price))
                showAddProductDialog = false
            },
            onDismiss = { showAddProductDialog = false }
        )
    }
}

@Composable
fun AddProductToOrderDialog(
    products: List<ProductEntity>,
    onAdd: (ProductEntity, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var quantityText by remember { mutableStateOf("1") }
    var priceText by remember { mutableStateOf(selectedProduct?.sellingPrice?.toString() ?: "0") }
    val now = System.currentTimeMillis()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Add Product to Order", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                Text("Select Product:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable product list selector
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    products.forEach { prod ->
                        val isSelected = selectedProduct?.id == prod.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    selectedProduct = prod
                                    priceText = prod.sellingPrice.toString()
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) GreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Stock: ${prod.quantity} ${prod.unit} | ₹${prod.sellingPrice}", fontSize = 11.sp)
                                }
                                if (prod.expiryDateEpoch != null) {
                                    FefoBadge(daysUntilExpiry = prod.daysUntilExpiry(now))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                selectedProduct?.let { prod ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Quantity (${prod.unit})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Unit Price (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val q = quantityText.toDoubleOrNull() ?: 0.0
                    if (q > prod.quantity) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Warning: Requested $q exceeds available stock ${prod.quantity}!",
                            color = StatusDanger,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val prod = selectedProduct ?: return@Button
                            val q = quantityText.toDoubleOrNull() ?: 1.0
                            val p = priceText.toDoubleOrNull() ?: prod.sellingPrice
                            onAdd(prod, q, p)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add to Order")
                    }
                }
            }
        }
    }
}
