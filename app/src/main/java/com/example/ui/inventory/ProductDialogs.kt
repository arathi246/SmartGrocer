package com.example.ui.inventory

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.ProductEntity
import com.example.ui.components.FefoBadge
import com.example.ui.components.Formatters
import com.example.ui.components.LowStockBadge
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import java.util.Calendar

val STANDARD_CATEGORIES = listOf(
    "Grains & Rice",
    "Edible Oils",
    "Flour & Bakery",
    "Pulses & Lentils",
    "Dairy & Beverages",
    "Spices & Seasoning",
    "Packaged Snacks",
    "Sauces & Condiments",
    "General Provisions"
)

val STANDARD_UNITS = listOf(
    "packet",
    "box",
    "carton",
    "kg",
    "bag",
    "litre",
    "tin",
    "bottle",
    "piece"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    initialProduct: ProductEntity? = null,
    onSave: (ProductEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var sku by remember { mutableStateOf(initialProduct?.sku ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: STANDARD_CATEGORIES.first()) }
    var quantityText by remember { mutableStateOf(initialProduct?.quantity?.toString() ?: "10") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: STANDARD_UNITS.first()) }
    var purchasePriceText by remember { mutableStateOf(initialProduct?.purchasePrice?.toString() ?: "100") }
    var sellingPriceText by remember { mutableStateOf(initialProduct?.sellingPrice?.toString() ?: "120") }
    var lowStockThresholdText by remember { mutableStateOf(initialProduct?.lowStockThreshold?.toString() ?: "10") }
    var expiryDateEpoch by remember { mutableStateOf(initialProduct?.expiryDateEpoch) }
    var supplierName by remember { mutableStateOf(initialProduct?.supplierName ?: "") }
    var batchNumber by remember { mutableStateOf(initialProduct?.batchNumber ?: "") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                Text(
                    text = if (initialProduct == null) "Add New Product" else "Edit Product",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                errorMessage?.let {
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = it,
                            color = StatusDanger,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                // SKU & Category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it.uppercase() },
                        label = { Text("SKU / Code *") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_sku_input")
                    )

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            STANDARD_CATEGORIES.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Quantity & Unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Initial Stock *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_quantity_input")
                    )

                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = !unitExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            STANDARD_UNITS.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Pricing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = purchasePriceText,
                        onValueChange = { purchasePriceText = it },
                        label = { Text("Cost Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it },
                        label = { Text("Selling Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Low Stock Threshold
                OutlinedTextField(
                    value = lowStockThresholdText,
                    onValueChange = { lowStockThresholdText = it },
                    label = { Text("Low Stock Alert Threshold ($unit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Expiry Date Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = expiryDateEpoch?.let { Formatters.formatDate(it) } ?: "Not Set (No Expiry)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expiry Date (FEFO tracking)") },
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date")
                            }
                        }
                    )
                    if (expiryDateEpoch != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(onClick = { expiryDateEpoch = null }) {
                            Text("Clear", color = StatusDanger)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Batch Number & Supplier
                OutlinedTextField(
                    value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = { Text("Batch Number (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text("Supplier Name (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
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
                            // Validations
                            if (name.isBlank()) {
                                errorMessage = "Product name is required."
                                return@Button
                            }
                            if (sku.isBlank()) {
                                errorMessage = "SKU/Code is required."
                                return@Button
                            }
                            val qty = quantityText.toDoubleOrNull()
                            if (qty == null || qty < 0) {
                                errorMessage = "Please enter a valid stock quantity (≥ 0)."
                                return@Button
                            }
                            val pPrice = purchasePriceText.toDoubleOrNull()
                            val sPrice = sellingPriceText.toDoubleOrNull()
                            if (pPrice == null || pPrice < 0 || sPrice == null || sPrice < 0) {
                                errorMessage = "Prices must be positive numbers."
                                return@Button
                            }
                            val threshold = lowStockThresholdText.toDoubleOrNull() ?: 10.0

                            val updated = (initialProduct ?: ProductEntity(
                                name = name.trim(),
                                sku = sku.trim(),
                                category = category,
                                quantity = qty,
                                unit = unit,
                                purchasePrice = pPrice,
                                sellingPrice = sPrice,
                                lowStockThreshold = threshold,
                                expiryDateEpoch = expiryDateEpoch,
                                supplierName = supplierName.trim(),
                                batchNumber = batchNumber.trim(),
                                isDemo = false
                            )).copy(
                                name = name.trim(),
                                sku = sku.trim(),
                                category = category,
                                quantity = qty,
                                unit = unit,
                                purchasePrice = pPrice,
                                sellingPrice = sPrice,
                                lowStockThreshold = threshold,
                                expiryDateEpoch = expiryDateEpoch,
                                supplierName = supplierName.trim(),
                                batchNumber = batchNumber.trim()
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_product_button")
                    ) {
                        Text(if (initialProduct == null) "Add Product" else "Save Changes")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expiryDateEpoch ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        expiryDateEpoch = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun AdjustStockDialog(
    product: ProductEntity,
    onConfirm: (changeAmount: Double, reason: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var isAddition by remember { mutableStateOf(true) }
    var quantityText by remember { mutableStateOf("1") }
    var selectedReason by remember { mutableStateOf("Purchase Received") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val additionReasons = listOf("Purchase Received", "Inventory Correction (+)", "Customer Return")
    val deductionReasons = listOf("Sale / Order", "Damaged / Spoiled", "Expired Food Scrap", "Inventory Correction (-)")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Adjust Stock: ${product.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Current Stock: ${product.quantity} ${product.unit}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                errorMessage?.let {
                    Text(it, color = StatusDanger, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }

                // Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isAddition = true
                            selectedReason = additionReasons.first()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAddition) StatusSuccess else Color(0xFFE0E0E0),
                            contentColor = if (isAddition) Color.White else Color.Black
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Stock")
                    }

                    Button(
                        onClick = {
                            isAddition = false
                            selectedReason = deductionReasons.first()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isAddition) StatusDanger else Color(0xFFE0E0E0),
                            contentColor = if (!isAddition) Color.White else Color.Black
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reduce Stock")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity (${product.unit}) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adjust_stock_quantity_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Reason for adjustment:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                val currentReasons = if (isAddition) additionReasons else deductionReasons
                currentReasons.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(reason, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Supplier / Ref (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = quantityText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Enter a valid positive number"
                        return@Button
                    }
                    if (!isAddition && amount > product.quantity) {
                        errorMessage = "Cannot deduct $amount ${product.unit}. Current stock is only ${product.quantity}!"
                        return@Button
                    }
                    val finalChange = if (isAddition) amount else -amount
                    onConfirm(finalChange, selectedReason, note.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isAddition) GreenPrimary else StatusDanger),
                modifier = Modifier.testTag("confirm_adjust_stock_button")
            ) {
                Text("Confirm Adjustment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProductDetailDialog(
    product: ProductEntity,
    onAdjustStockClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val now = System.currentTimeMillis()
    val daysUntilExpiry = product.daysUntilExpiry(now)

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "SKU: ${product.sku} | Category: ${product.category}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Row {
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = GreenPrimary)
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = StatusDanger)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (daysUntilExpiry != null) {
                        FefoBadge(daysUntilExpiry = daysUntilExpiry)
                    }
                    LowStockBadge(
                        quantity = product.quantity,
                        threshold = product.lowStockThreshold,
                        unit = product.unit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stock & Pricing Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Available Stock", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${product.quantity} ${product.unit}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (product.isLowStock) StatusDanger else GreenPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Selling Price", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = Formatters.formatCurrency(product.sellingPrice),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cost Price: ${Formatters.formatCurrency(product.purchasePrice)}", fontSize = 12.sp)
                            Text("Low Stock Level: ${product.lowStockThreshold} ${product.unit}", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // FEFO & Expiry Info
                if (daysUntilExpiry != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (daysUntilExpiry < 0) Color(0xFFFFEBEE) else Color(0xFFF1F8E9)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (daysUntilExpiry < 0) "FOOD SAFETY WARNING: EXPIRED" else "FEFO Dispatch Notice",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (daysUntilExpiry < 0) StatusDanger else GreenPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (daysUntilExpiry < 0)
                                    "This product expired ${-daysUntilExpiry} days ago. NEVER dispatch expired food to customers."
                                else
                                    "Expiry Date: ${Formatters.formatDate(product.expiryDateEpoch)} ($daysUntilExpiry days left). Apply First-Expiry-First-Out for dispatch.",
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Batch & Supplier
                if (product.batchNumber.isNotBlank() || product.supplierName.isNotBlank()) {
                    Text("Batch & Supplier Details", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (product.batchNumber.isNotBlank()) {
                        Text("Batch / Lot #: ${product.batchNumber}", fontSize = 12.sp)
                    }
                    if (product.supplierName.isNotBlank()) {
                        Text("Supplier: ${product.supplierName}", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Quick Stock Adjustment Button
                Button(
                    onClick = onAdjustStockClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Adjust Stock (Receive / Damage / Correct)")
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
