package com.example.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ProductEntity
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FefoBadge
import com.example.ui.components.Formatters
import com.example.ui.components.LowStockBadge
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.viewmodel.MainViewModel

@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val searchQuery by viewModel.inventorySearch.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.inventoryCategoryFilter.collectAsStateWithLifecycle()
    val quickFilter by viewModel.inventoryQuickFilter.collectAsStateWithLifecycle()
    val sortBy by viewModel.inventorySortBy.collectAsStateWithLifecycle()
    val warnDays by viewModel.expiryWarningDays.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var adjustingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var viewingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var deletingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()

    // Filter products
    val filteredProducts = products.filter { prod ->
        // Search
        val matchSearch = searchQuery.isBlank() ||
            prod.name.contains(searchQuery, ignoreCase = true) ||
            prod.sku.contains(searchQuery, ignoreCase = true)

        // Category
        val matchCategory = selectedCategory == "All" || prod.category == selectedCategory

        // Quick filter
        val matchQuick = when (quickFilter) {
            "Low Stock" -> prod.isLowStock
            "Expiring Soon" -> prod.isNearingExpiry(warnDays, now)
            "Expired" -> prod.isExpired(now)
            else -> true
        }

        matchSearch && matchCategory && matchQuick
    }.let { list ->
        // Sort
        when (sortBy) {
            "Quantity" -> list.sortedBy { it.quantity }
            "Expiry (FEFO)" -> list.sortedBy { it.expiryDateEpoch ?: Long.MAX_VALUE }
            else -> list.sortedBy { it.name.lowercase() }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("inventory_screen")
        ) {
            // Search & Sort bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.inventorySearch.value = it },
                    placeholder = { Text("Search product name or SKU...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.inventorySearch.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("inventory_search_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(
                        onClick = { sortMenuExpanded = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = "Sort Products", tint = GreenPrimary)
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort: Name (A-Z)") },
                            onClick = {
                                viewModel.inventorySortBy.value = "Name"
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort: Quantity (Low to High)") },
                            onClick = {
                                viewModel.inventorySortBy.value = "Quantity"
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort: Expiry Date (FEFO Priority)") },
                            onClick = {
                                viewModel.inventorySortBy.value = "Expiry (FEFO)"
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Quick Filters (All, Low Stock, Expiring Soon, Expired)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Low Stock", "Expiring Soon", "Expired").forEach { qf ->
                    FilterChip(
                        selected = quickFilter == qf,
                        onClick = { viewModel.inventoryQuickFilter.value = qf },
                        label = { Text(qf, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (qf == "Expired" || qf == "Low Stock") Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = if (qf == "Expired" || qf == "Low Stock") StatusDanger else GreenPrimary
                        )
                    )
                }
            }

            // Category Horizontal Scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val allCats = listOf("All") + STANDARD_CATEGORIES
                allCats.forEach { cat ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedCategory == cat) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            viewModel.inventoryCategoryFilter.value = cat
                        }
                    ) {
                        Text(
                            text = cat,
                            color = if (selectedCategory == cat) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Summary Count & Active Sort Label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredProducts.size} product${if (filteredProducts.size != 1) "s" else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Sorted by: $sortBy",
                    fontSize = 11.sp,
                    color = GreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Products List
            if (filteredProducts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Inventory2,
                    title = "No Products Found",
                    description = if (searchQuery.isNotBlank() || quickFilter != "All" || selectedCategory != "All")
                        "No inventory matches your active filter criteria."
                    else
                        "Your warehouse inventory is currently empty.",
                    actionButtonText = if (products.isEmpty()) "Add First Product" else "Clear Filters",
                    onActionClick = {
                        if (products.isEmpty()) {
                            showAddDialog = true
                        } else {
                            viewModel.inventorySearch.value = ""
                            viewModel.inventoryCategoryFilter.value = "All"
                            viewModel.inventoryQuickFilter.value = "All"
                        }
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            now = now,
                            onClick = { viewingProduct = product },
                            onAdjustStock = { adjustingProduct = product },
                            modifier = Modifier.testTag("product_item_${product.id}")
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddEditProductDialog(
            initialProduct = null,
            onSave = { newProd -> viewModel.addProduct(newProd) },
            onDismiss = { showAddDialog = false }
        )
    }

    editingProduct?.let { prod ->
        AddEditProductDialog(
            initialProduct = prod,
            onSave = { updated -> viewModel.updateProduct(updated) },
            onDismiss = { editingProduct = null }
        )
    }

    adjustingProduct?.let { prod ->
        AdjustStockDialog(
            product = prod,
            onConfirm = { changeAmount, reason, note ->
                viewModel.adjustProductStock(prod.id, changeAmount, reason, note)
            },
            onDismiss = { adjustingProduct = null }
        )
    }

    viewingProduct?.let { prod ->
        ProductDetailDialog(
            product = prod,
            onAdjustStockClick = {
                viewingProduct = null
                adjustingProduct = prod
            },
            onEditClick = {
                viewingProduct = null
                editingProduct = prod
            },
            onDeleteClick = {
                viewingProduct = null
                deletingProduct = prod
            },
            onDismiss = { viewingProduct = null }
        )
    }

    deletingProduct?.let { prod ->
        ConfirmDeleteDialog(
            title = "Delete Product?",
            message = "Are you sure you want to delete '${prod.name}'? This cannot be undone.",
            onConfirm = { viewModel.deleteProduct(prod) },
            onDismiss = { deletingProduct = null }
        )
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    now: Long,
    onClick: () -> Unit,
    onAdjustStock: () -> Unit,
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "SKU: ${product.sku} | ${product.category}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${product.quantity} ${product.unit}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (product.isLowStock) StatusDanger else GreenPrimary
                    )
                    Text(
                        text = "₹${product.sellingPrice}/${product.unit}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (product.expiryDateEpoch != null) {
                        FefoBadge(daysUntilExpiry = product.daysUntilExpiry(now))
                    }
                    if (product.isLowStock) {
                        LowStockBadge(
                            quantity = product.quantity,
                            threshold = product.lowStockThreshold,
                            unit = product.unit
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable(onClick = onAdjustStock)
                ) {
                    Text(
                        text = "Adjust Stock",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GreenPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
