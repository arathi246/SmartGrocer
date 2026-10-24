package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.CustomerEntity
import com.example.data.entity.DeliveryEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.StockMovementEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class NavigationDestination {
    object Dashboard : NavigationDestination()
    object Orders : NavigationDestination()
    object Inventory : NavigationDestination()
    object Payments : NavigationDestination()
    object More : NavigationDestination()

    // Sub-destinations
    object CreateOrder : NavigationDestination()
    data class OrderDetail(val orderId: Long) : NavigationDestination()
    object Deliveries : NavigationDestination()
    object Reports : NavigationDestination()
    object AiAssistant : NavigationDestination()
    object Settings : NavigationDestination()
}

data class CustomerFinancialSummary(
    val customer: CustomerEntity,
    val totalBilled: Double,
    val totalPaid: Double,
    val outstandingBalance: Double
)

data class DashboardMetrics(
    val pendingOrdersCount: Int = 0,
    val lowStockCount: Int = 0,
    val nearExpiryCount: Int = 0,
    val totalOutstandingPayments: Double = 0.0,
    val pendingDeliveriesCount: Int = 0
)

data class SalesMetrics(
    val salesToday: Double = 0.0,
    val salesThisWeek: Double = 0.0,
    val salesThisMonth: Double = 0.0,
    val totalSalesAllTime: Double = 0.0,
    val totalOrdersCount: Int = 0,
    val deliveredOrdersCount: Int = 0,
    val statusBreakdown: Map<String, Int> = emptyMap()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database)
    }

    // Navigation Stack for full BackHandler support
    private val _navStack = MutableStateFlow<List<NavigationDestination>>(listOf(NavigationDestination.Dashboard))
    val currentDestination: StateFlow<NavigationDestination> = _navStack.combine(MutableStateFlow(Unit)) { stack, _ ->
        stack.lastOrNull() ?: NavigationDestination.Dashboard
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NavigationDestination.Dashboard)

    fun navigateTo(dest: NavigationDestination) {
        val currentList = _navStack.value.toMutableList()
        // If switching between primary bottom tabs, replace root
        if (dest is NavigationDestination.Dashboard ||
            dest is NavigationDestination.Orders ||
            dest is NavigationDestination.Inventory ||
            dest is NavigationDestination.Payments ||
            dest is NavigationDestination.More
        ) {
            _navStack.value = listOf(dest)
        } else {
            currentList.add(dest)
            _navStack.value = currentList
        }
    }

    fun navigateBack(): Boolean {
        val currentList = _navStack.value.toMutableList()
        return if (currentList.size > 1) {
            currentList.removeAt(currentList.lastIndex)
            _navStack.value = currentList
            true
        } else {
            false
        }
    }

    // Reactive Raw Data
    val products = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orderItems = repository.allOrderItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliveries = repository.allDeliveries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockMovements = repository.allStockMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Message Events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Filter states
    val inventorySearch = MutableStateFlow("")
    val inventoryCategoryFilter = MutableStateFlow("All")
    val inventoryQuickFilter = MutableStateFlow("All") // "All", "Low Stock", "Expiring Soon", "Expired"
    val inventorySortBy = MutableStateFlow("Name") // "Name", "Quantity", "Expiry (FEFO)"
    val expiryWarningDays = MutableStateFlow(30L)

    val ordersSearch = MutableStateFlow("")
    val ordersStatusFilter = MutableStateFlow("All")

    val customerSearch = MutableStateFlow("")
    val customerFilterOutstandingOnly = MutableStateFlow(false)

    val deliveryFilter = MutableStateFlow("All")

    // Seed Demo Data automatically on first install if database is completely empty
    init {
        viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            if (products.value.isEmpty() && customers.value.isEmpty() && orders.value.isEmpty()) {
                repository.loadDemoData()
            }
        }
    }

    // ---------------- FINANCIAL SUMMARIES ----------------
    val customerFinancials: StateFlow<List<CustomerFinancialSummary>> = combine(
        customers,
        orders,
        payments
    ) { custList, orderList, payList ->
        custList.map { customer ->
            val customerOrders = orderList.filter {
                it.customerId == customer.id && it.status != OrderEntity.STATUS_CANCELLED
            }
            val totalBilled = customerOrders.sumOf { it.totalAmount }
            val customerPayments = payList.filter { it.customerId == customer.id }
            val totalPaid = customerPayments.sumOf { it.amount }
            val outstanding = (totalBilled - totalPaid).coerceAtLeast(0.0)

            CustomerFinancialSummary(
                customer = customer,
                totalBilled = totalBilled,
                totalPaid = totalPaid,
                outstandingBalance = outstanding
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------- DASHBOARD METRICS ----------------
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        orders,
        products,
        customerFinancials,
        deliveries,
        expiryWarningDays
    ) { orderList, prodList, financials, delivList, warnDays ->
        val now = System.currentTimeMillis()
        val pendingOrders = orderList.count {
            it.status == OrderEntity.STATUS_DRAFT ||
            it.status == OrderEntity.STATUS_CONFIRMED ||
            it.status == OrderEntity.STATUS_PACKED
        }
        val lowStock = prodList.count { it.isLowStock }
        val nearExpiry = prodList.count { it.isNearingExpiry(warnDays, now) || it.isExpired(now) }
        val totalOutstanding = financials.sumOf { it.outstandingBalance }
        val pendingDeliv = delivList.count {
            it.status == DeliveryEntity.STATUS_PENDING || it.status == DeliveryEntity.STATUS_DISPATCHED
        }

        DashboardMetrics(
            pendingOrdersCount = pendingOrders,
            lowStockCount = lowStock,
            nearExpiryCount = nearExpiry,
            totalOutstandingPayments = totalOutstanding,
            pendingDeliveriesCount = pendingDeliv
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // ---------------- SALES & REPORTS ----------------
    val salesMetrics: StateFlow<SalesMetrics> = orders.combine(MutableStateFlow(Unit)) { orderList, _ ->
        val validOrders = orderList.filter { it.status != OrderEntity.STATUS_CANCELLED }
        val now = Calendar.getInstance()

        // Start of today
        val startOfTodayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = startOfTodayCal.timeInMillis

        // Start of this week
        val startOfWeekCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }
        val startOfWeek = startOfWeekCal.timeInMillis

        // Start of this month
        val startOfMonthCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val startOfMonth = startOfMonthCal.timeInMillis

        var salesToday = 0.0
        var salesThisWeek = 0.0
        var salesThisMonth = 0.0
        var totalSales = 0.0

        for (ord in validOrders) {
            totalSales += ord.totalAmount
            if (ord.orderDateEpoch >= startOfToday) {
                salesToday += ord.totalAmount
            }
            if (ord.orderDateEpoch >= startOfWeek) {
                salesThisWeek += ord.totalAmount
            }
            if (ord.orderDateEpoch >= startOfMonth) {
                salesThisMonth += ord.totalAmount
            }
        }

        val statusMap = orderList.groupBy { it.status }.mapValues { it.value.size }
        val deliveredCount = orderList.count { it.status == OrderEntity.STATUS_DELIVERED }

        SalesMetrics(
            salesToday = salesToday,
            salesThisWeek = salesThisWeek,
            salesThisMonth = salesThisMonth,
            totalSalesAllTime = totalSales,
            totalOrdersCount = orderList.size,
            deliveredOrdersCount = deliveredCount,
            statusBreakdown = statusMap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SalesMetrics())

    // ---------------- PRODUCT ACTIONS ----------------
    fun addProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                repository.insertProduct(product)
                _userMessage.emit("Product '${product.name}' added successfully.")
            } catch (e: Exception) {
                _userMessage.emit("Error adding product: ${e.message}")
            }
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                repository.updateProduct(product)
                _userMessage.emit("Product '${product.name}' updated.")
            } catch (e: Exception) {
                _userMessage.emit("Error updating product: ${e.message}")
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product)
                _userMessage.emit("Product '${product.name}' deleted.")
            } catch (e: Exception) {
                _userMessage.emit("Error deleting product: ${e.message}")
            }
        }
    }

    fun adjustProductStock(productId: Long, changeAmount: Double, reason: String, note: String) {
        viewModelScope.launch {
            val result = repository.adjustStock(productId, changeAmount, reason, note)
            result.fold(
                onSuccess = {
                    val sign = if (changeAmount >= 0) "+$changeAmount" else "$changeAmount"
                    _userMessage.emit("Stock adjusted ($sign): $reason")
                },
                onFailure = {
                    _userMessage.emit("Adjustment failed: ${it.message}")
                }
            )
        }
    }

    // ---------------- CUSTOMER ACTIONS ----------------
    fun addCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            try {
                repository.insertCustomer(customer)
                _userMessage.emit("Customer '${customer.name}' registered.")
            } catch (e: Exception) {
                _userMessage.emit("Error adding customer: ${e.message}")
            }
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            try {
                repository.updateCustomer(customer)
                _userMessage.emit("Customer updated.")
            } catch (e: Exception) {
                _userMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            try {
                repository.deleteCustomer(customer)
                _userMessage.emit("Customer '${customer.name}' removed.")
            } catch (e: Exception) {
                _userMessage.emit("Error: ${e.message}")
            }
        }
    }

    // ---------------- ORDER ACTIONS ----------------
    fun createOrder(order: OrderEntity, items: List<OrderItemEntity>, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.createOrder(order, items)
            result.fold(
                onSuccess = { orderId ->
                    _userMessage.emit("Order ${order.orderNumber} created successfully.")
                    onComplete(true)
                },
                onFailure = { error ->
                    _userMessage.emit("Order creation failed: ${error.message}")
                    onComplete(false)
                }
            )
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: String) {
        viewModelScope.launch {
            val result = repository.updateOrderStatus(orderId, newStatus)
            result.fold(
                onSuccess = {
                    _userMessage.emit("Order status changed to $newStatus.")
                },
                onFailure = { error ->
                    _userMessage.emit("Status update error: ${error.message}")
                }
            )
        }
    }

    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch {
            try {
                repository.deleteOrder(order)
                _userMessage.emit("Order ${order.orderNumber} deleted.")
            } catch (e: Exception) {
                _userMessage.emit("Error deleting order: ${e.message}")
            }
        }
    }

    suspend fun getOrderItems(orderId: Long): List<OrderItemEntity> {
        return repository.getItemsForOrder(orderId)
    }

    // ---------------- PAYMENT ACTIONS ----------------
    fun recordPayment(payment: PaymentEntity, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.insertPayment(payment)
            result.fold(
                onSuccess = {
                    _userMessage.emit("Payment of ₹${String.format(Locale.US, "%.2f", payment.amount)} recorded for ${payment.customerName}.")
                    onComplete(true)
                },
                onFailure = { error ->
                    _userMessage.emit("Payment recording error: ${error.message}")
                    onComplete(false)
                }
            )
        }
    }

    fun deletePayment(payment: PaymentEntity) {
        viewModelScope.launch {
            try {
                repository.deletePayment(payment)
                _userMessage.emit("Payment entry removed.")
            } catch (e: Exception) {
                _userMessage.emit("Error: ${e.message}")
            }
        }
    }

    // ---------------- DELIVERY ACTIONS ----------------
    fun updateDelivery(delivery: DeliveryEntity) {
        viewModelScope.launch {
            try {
                repository.updateDelivery(delivery)
                _userMessage.emit("Delivery status updated to ${delivery.status}.")
            } catch (e: Exception) {
                _userMessage.emit("Error: ${e.message}")
            }
        }
    }

    // ---------------- DEMO & RESET DATA ----------------
    fun loadSampleData() {
        viewModelScope.launch {
            try {
                repository.loadDemoData()
                _userMessage.emit("Sample demo data reloaded successfully.")
            } catch (e: Exception) {
                _userMessage.emit("Error loading sample data: ${e.message}")
            }
        }
    }

    fun clearAllDemoData() {
        viewModelScope.launch {
            try {
                repository.clearAllDemoData()
                _userMessage.emit("All demo sample records cleared.")
            } catch (e: Exception) {
                _userMessage.emit("Error clearing demo records: ${e.message}")
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                repository.clearEverything()
                _userMessage.emit("All database records cleared.")
            } catch (e: Exception) {
                _userMessage.emit("Error: ${e.message}")
            }
        }
    }

    // ---------------- EXPORT HELPERS ----------------
    fun generateCsvExport(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val sb = StringBuilder()

        sb.append("=== SMART FOOD DISTRIBUTION MANAGER - DATA EXPORT ===\n")
        sb.append("Exported At: ").append(sdf.format(Date())).append("\n\n")

        sb.append("--- INVENTORY PRODUCTS ---\n")
        sb.append("ID,Name,SKU,Category,Quantity,Unit,PurchasePrice,SellingPrice,LowStockThreshold,ExpiryDate,Supplier\n")
        for (p in products.value) {
            val exp = p.expiryDateEpoch?.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it)) } ?: "None"
            sb.append("${p.id},\"${p.name}\",\"${p.sku}\",\"${p.category}\",${p.quantity},\"${p.unit}\",${p.purchasePrice},${p.sellingPrice},${p.lowStockThreshold},\"$exp\",\"${p.supplierName}\"\n")
        }

        sb.append("\n--- CUSTOMERS & BALANCES ---\n")
        sb.append("ID,Name,Phone,Address,TotalBilled,TotalPaid,OutstandingBalance\n")
        for (f in customerFinancials.value) {
            sb.append("${f.customer.id},\"${f.customer.name}\",\"${f.customer.phone}\",\"${f.customer.address}\",${f.totalBilled},${f.totalPaid},${f.outstandingBalance}\n")
        }

        sb.append("\n--- ORDERS ---\n")
        sb.append("ID,OrderNumber,Customer,Date,Status,TotalAmount,StockDeducted\n")
        for (o in orders.value) {
            val dateStr = sdf.format(Date(o.orderDateEpoch))
            sb.append("${o.id},\"${o.orderNumber}\",\"${o.customerName}\",\"$dateStr\",\"${o.status}\",${o.totalAmount},${o.stockDeducted}\n")
        }

        sb.append("\n--- PAYMENTS ---\n")
        sb.append("ID,Customer,Amount,Date,Method,Reference\n")
        for (pay in payments.value) {
            val dateStr = sdf.format(Date(pay.paymentDateEpoch))
            sb.append("${pay.id},\"${pay.customerName}\",${pay.amount},\"$dateStr\",\"${pay.paymentMethod}\",\"${pay.referenceNumber}\"\n")
        }

        return sb.toString()
    }
}
