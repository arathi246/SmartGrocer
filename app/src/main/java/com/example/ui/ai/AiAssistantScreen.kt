package com.example.ui.ai

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.ProductEntity
import com.example.ui.components.Formatters
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.NavigationDestination

data class RestockSuggestion(
    val product: ProductEntity,
    val suggestedQuantity: Double,
    val reason: String,
    val urgency: String // "High", "Medium", "Caution"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val products by viewModel.products.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val orderItems by viewModel.orderItems.collectAsStateWithLifecycle()
    val financials by viewModel.customerFinancials.collectAsStateWithLifecycle()
    val warnDays by viewModel.expiryWarningDays.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Restock Assistant, 1: Order Text Parser, 2: Q&A

    // Q&A state
    var questionText by remember { mutableStateOf("") }
    var answerText by remember { mutableStateOf<String?>(null) }

    // Order parser state
    var pastedOrderText by remember { mutableStateOf("") }
    var parsedOrderPreview by remember { mutableStateOf<List<Pair<ProductEntity, Double>>?>(null) }
    var parseErrorMessage by remember { mutableStateOf<String?>(null) }

    // Compute Restocking Suggestions
    val restockSuggestions = remember(products, orderItems) {
        val suggestions = mutableListOf<RestockSuggestion>()
        val productSalesCount = orderItems.groupBy { it.productId }
            .mapValues { (_, items) -> items.sumOf { it.quantity } }

        for (prod in products) {
            val totalSold = productSalesCount[prod.id] ?: 0.0
            val deficit = prod.lowStockThreshold - prod.quantity

            if (prod.quantity <= 0.0) {
                val reorder = (prod.lowStockThreshold * 2).coerceAtLeast(10.0)
                suggestions.add(
                    RestockSuggestion(
                        product = prod,
                        suggestedQuantity = reorder,
                        reason = "OUT OF STOCK! Zero stock available. Reorder $reorder ${prod.unit} based on 2x threshold buffer.",
                        urgency = "High"
                    )
                )
            } else if (prod.isLowStock) {
                val reorder = (deficit + prod.lowStockThreshold).coerceAtLeast(5.0)
                suggestions.add(
                    RestockSuggestion(
                        product = prod,
                        suggestedQuantity = reorder,
                        reason = "Stock (${prod.quantity} ${prod.unit}) is at or below threshold (${prod.lowStockThreshold} ${prod.unit}). Historical sales: $totalSold ${prod.unit}.",
                        urgency = "High"
                    )
                )
            } else if (totalSold > prod.quantity) {
                val reorder = (totalSold - prod.quantity + prod.lowStockThreshold).coerceAtLeast(5.0)
                suggestions.add(
                    RestockSuggestion(
                        product = prod,
                        suggestedQuantity = reorder,
                        reason = "High velocity item! Total ordered ($totalSold) outpaces current stock (${prod.quantity}). Recommended replenishment buffer: $reorder ${prod.unit}.",
                        urgency = "Medium"
                    )
                )
            }
        }
        suggestions
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Operations Assistant", fontWeight = FontWeight.Bold) },
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
                .testTag("ai_assistant_screen")
        ) {
            // Safe AI Notice Banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Advisory only: Estimates require human review. AI does not autonomously alter finances or inventory.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = GreenPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Restock", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Parse Order", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Ask Data", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Restock suggestions tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "INTELLIGENT REPLENISHMENT ADVISOR",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        }

                        if (restockSuggestions.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("All Stock Healthy! 🎉", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "All warehouse inventory levels are currently above reorder thresholds with healthy velocity buffers.",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(restockSuggestions) { suggestion ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
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
                                            Text(
                                                suggestion.product.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (suggestion.urgency == "High") Color(0xFFFFEBEE) else Color(0xFFFFF3E0)
                                            ) {
                                                Text(
                                                    "${suggestion.urgency} Priority",
                                                    color = if (suggestion.urgency == "High") StatusDanger else AmberAccent,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            "Suggested Order: +${suggestion.suggestedQuantity} ${suggestion.product.unit}",
                                            fontWeight = FontWeight.Bold,
                                            color = GreenPrimary,
                                            fontSize = 13.sp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            suggestion.reason,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Order Text Parser tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("Order Text Entry Assistant", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Paste customer WhatsApp messages or order lists. The parser matches food products against your warehouse catalogue for review.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = pastedOrderText,
                            onValueChange = { pastedOrderText = it },
                            placeholder = { Text("Example:\n2 bags Basmati Rice\n5 packets Sunflower Oil\n3 Atta") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val lines = pastedOrderText.split("\n", ",").map { it.trim() }.filter { it.isNotBlank() }
                                val matched = mutableListOf<Pair<ProductEntity, Double>>()
                                for (line in lines) {
                                    // Extract leading digits if any
                                    val regex = Regex("^(\\d+(?:\\.\\d+)?)\\s*(.*)$")
                                    val match = regex.find(line)
                                    val qty = match?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
                                    val queryText = match?.groupValues?.get(2)?.trim() ?: line

                                    val found = products.firstOrNull { prod ->
                                        prod.name.contains(queryText, ignoreCase = true) ||
                                        queryText.contains(prod.name, ignoreCase = true) ||
                                        prod.sku.contains(queryText, ignoreCase = true) ||
                                        prod.category.contains(queryText, ignoreCase = true)
                                    }
                                    if (found != null) {
                                        matched.add(Pair(found, qty))
                                    }
                                }
                                if (matched.isEmpty()) {
                                    parseErrorMessage = "Could not automatically match products. Ensure names match your warehouse catalogue."
                                    parsedOrderPreview = null
                                } else {
                                    parseErrorMessage = null
                                    parsedOrderPreview = matched
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Parse Text into Draft Order")
                        }

                        parseErrorMessage?.let {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(it, color = StatusDanger, fontSize = 12.sp)
                        }

                        parsedOrderPreview?.let { preview ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Parsed Draft Order Items (Review)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            preview.forEach { (prod, qty) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Stock: ${prod.quantity} ${prod.unit}", fontSize = 11.sp)
                                        }
                                        Text("$qty ${prod.unit}", fontWeight = FontWeight.Bold, color = GreenPrimary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    // Navigates to standard create order
                                    viewModel.navigateTo(NavigationDestination.CreateOrder)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Open in Order Form to Finalize")
                            }
                        }
                    }
                }

                2 -> {
                    // Natural-language data questions
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("Ask About Your Business Data", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Instant answers generated directly from your saved products, orders, deliveries, and payment records.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Question Chips
                        Text("Suggested Questions:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        val sampleQuestions = listOf(
                            "Which products are low in stock?",
                            "Which food items are expiring soon?",
                            "Which customers owe outstanding payments?",
                            "How many deliveries are pending?"
                        )
                        sampleQuestions.forEach { q ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        questionText = q
                                        answerText = answerBusinessQuestion(q, products, orders, financials, warnDays)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(q, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = questionText,
                                onValueChange = { questionText = it },
                                placeholder = { Text("Ask anything about stock, payments...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (questionText.isNotBlank()) {
                                        answerText = answerBusinessQuestion(questionText, products, orders, financials, warnDays)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Ask", tint = GreenPrimary)
                            }
                        }

                        answerText?.let { answer ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Business Summary:", fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(answer, fontSize = 13.sp, lineHeight = 19.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun answerBusinessQuestion(
    question: String,
    products: List<ProductEntity>,
    orders: List<OrderEntity>,
    financials: List<com.example.viewmodel.CustomerFinancialSummary>,
    warnDays: Long
): String {
    val q = question.lowercase()
    val now = System.currentTimeMillis()

    return when {
        q.contains("low in stock") || q.contains("low stock") -> {
            val low = products.filter { it.isLowStock }
            if (low.isEmpty()) {
                "No products are currently low in stock. All inventory levels are above configured thresholds."
            } else {
                "Currently, ${low.size} product(s) are low in stock:\n" +
                    low.joinToString("\n") { "• ${it.name}: only ${it.quantity} ${it.unit} left (Threshold: ${it.lowStockThreshold} ${it.unit})" }
            }
        }
        q.contains("expir") || q.contains("fefo") -> {
            val expiring = products.filter { it.isNearingExpiry(warnDays, now) || it.isExpired(now) }
            if (expiring.isEmpty()) {
                "No food products are expiring within the next $warnDays days."
            } else {
                "Found ${expiring.size} product(s) requiring attention:\n" +
                    expiring.joinToString("\n") { p ->
                        val days = p.daysUntilExpiry(now) ?: 0
                        val status = if (days < 0) "EXPIRED (${-days} days ago - DO NOT DISPATCH)" else "$days days left (FEFO priority)"
                        "• ${p.name} (Stock: ${p.quantity} ${p.unit}) -> $status"
                    }
            }
        }
        q.contains("owe") || q.contains("payment") || q.contains("balance") || q.contains("due") -> {
            val debtors = financials.filter { it.outstandingBalance > 0.0 }
            val total = debtors.sumOf { it.outstandingBalance }
            if (debtors.isEmpty()) {
                "All customer invoices and orders are fully settled! Outstanding balance is ₹0.00."
            } else {
                "Total outstanding across ${debtors.size} customer(s) is ${Formatters.formatCurrency(total)}:\n" +
                    debtors.joinToString("\n") { "• ${it.customer.name}: ${Formatters.formatCurrency(it.outstandingBalance)} outstanding (Billed: ${Formatters.formatCurrency(it.totalBilled)}, Paid: ${Formatters.formatCurrency(it.totalPaid)})" }
            }
        }
        q.contains("deliver") || q.contains("pending") -> {
            val pendingOrders = orders.filter { it.status == OrderEntity.STATUS_DISPATCHED || it.status == OrderEntity.STATUS_PACKED }
            "There are ${pendingOrders.size} order(s) currently packed or out for dispatch:\n" +
                pendingOrders.joinToString("\n") { "• ${it.orderNumber} for ${it.customerName} (${it.status})" }
        }
        else -> {
            "Summary: Warehouse has ${products.size} product varieties, ${orders.size} recorded orders, and ${financials.count { it.outstandingBalance > 0.0 }} customer(s) with pending balance payments. Ask about 'low stock', 'expiring items', or 'customer balances' for specifics."
        }
    }
}
