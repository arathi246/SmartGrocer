package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.SampleData
import com.example.data.entity.CustomerEntity
import com.example.data.entity.DeliveryEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.StockMovementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AppRepository(private val database: AppDatabase) {
    private val productDao = database.productDao()
    private val movementDao = database.stockMovementDao()
    private val customerDao = database.customerDao()
    private val orderDao = database.orderDao()
    private val orderItemDao = database.orderItemDao()
    private val paymentDao = database.paymentDao()
    private val deliveryDao = database.deliveryDao()

    // Reactive streams
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val allOrderItems: Flow<List<OrderItemEntity>> = orderItemDao.getAllOrderItems()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()
    val allDeliveries: Flow<List<DeliveryEntity>> = deliveryDao.getAllDeliveries()
    val allStockMovements: Flow<List<StockMovementEntity>> = movementDao.getAllMovements()

    // ---------------- PRODUCT OPERATIONS ----------------
    suspend fun insertProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        val id = productDao.insertProduct(product)
        movementDao.insertMovement(
            StockMovementEntity(
                productId = id,
                productName = product.name,
                changeAmount = product.quantity,
                remainingQuantity = product.quantity,
                reason = "Initial Stock / Added",
                note = "Product registered with initial quantity"
            )
        )
        id
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun adjustStock(
        productId: Long,
        changeAmount: Double, // positive or negative
        reason: String,
        note: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val product = productDao.getProductById(productId)
            ?: return@withContext Result.failure(Exception("Product not found"))

        val newQuantity = product.quantity + changeAmount
        if (newQuantity < 0) {
            return@withContext Result.failure(Exception("Cannot reduce stock below zero! Current stock: ${product.quantity} ${product.unit}"))
        }

        productDao.updateQuantity(productId, newQuantity)
        movementDao.insertMovement(
            StockMovementEntity(
                productId = productId,
                productName = product.name,
                changeAmount = changeAmount,
                remainingQuantity = newQuantity,
                reason = reason,
                note = note
            )
        )
        Result.success(Unit)
    }

    // ---------------- CUSTOMER OPERATIONS ----------------
    suspend fun insertCustomer(customer: CustomerEntity): Long = withContext(Dispatchers.IO) {
        customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        customerDao.deleteCustomer(customer)
    }

    // ---------------- ORDER OPERATIONS ----------------
    suspend fun createOrder(
        order: OrderEntity,
        items: List<OrderItemEntity>,
        autoCreateDelivery: Boolean = true
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (items.isEmpty()) {
            return@withContext Result.failure(Exception("Order must contain at least one product."))
        }

        // Validate stock availability
        val currentProducts = allProducts.first().associateBy { it.id }
        for (item in items) {
            val prod = currentProducts[item.productId]
            if (prod != null && item.quantity > prod.quantity) {
                return@withContext Result.failure(
                    Exception("Requested ${item.quantity} ${item.unit} for '${item.productName}', but only ${prod.quantity} ${item.unit} is available in stock!")
                )
            }
        }

        val orderId = orderDao.insertOrder(order)
        val itemsWithOrderId = items.map { it.copy(orderId = orderId) }
        orderItemDao.insertItems(itemsWithOrderId)

        if (autoCreateDelivery) {
            deliveryDao.insertDelivery(
                DeliveryEntity(
                    orderId = orderId,
                    orderNumber = order.orderNumber,
                    customerName = order.customerName,
                    deliveryAddress = order.deliveryAddress,
                    dispatchDateEpoch = order.orderDateEpoch,
                    status = DeliveryEntity.STATUS_PENDING,
                    notes = order.notes,
                    isDemo = order.isDemo
                )
            )
        }

        // If created directly as Dispatched or Delivered, deduct stock immediately
        if (order.status == OrderEntity.STATUS_DISPATCHED || order.status == OrderEntity.STATUS_DELIVERED) {
            deductOrderStock(order.copy(id = orderId), itemsWithOrderId)
        }

        Result.success(orderId)
    }

    suspend fun updateOrderStatus(orderId: Long, newStatus: String): Result<Unit> = withContext(Dispatchers.IO) {
        val order = orderDao.getOrderById(orderId)
            ?: return@withContext Result.failure(Exception("Order #$orderId not found."))

        val items = orderItemDao.getItemsForOrder(orderId)
        val oldStatus = order.status

        // 1. Stock Deduction Logic
        var newStockDeducted = order.stockDeducted
        if ((newStatus == OrderEntity.STATUS_DISPATCHED || newStatus == OrderEntity.STATUS_DELIVERED) && !order.stockDeducted) {
            // Deduct stock safely
            val deductResult = deductOrderStock(order, items)
            if (deductResult.isFailure) {
                return@withContext deductResult
            }
            newStockDeducted = true
        } else if (newStatus == OrderEntity.STATUS_CANCELLED && order.stockDeducted) {
            // Restore stock safely
            restoreOrderStock(order, items)
            newStockDeducted = false
        }

        val updatedOrder = order.copy(
            status = newStatus,
            stockDeducted = newStockDeducted
        )
        orderDao.updateOrder(updatedOrder)

        // 2. Sync corresponding Delivery status
        val delivery = deliveryDao.getDeliveryByOrderId(orderId)
        if (delivery != null) {
            val newDeliveryStatus = when (newStatus) {
                OrderEntity.STATUS_DISPATCHED -> DeliveryEntity.STATUS_DISPATCHED
                OrderEntity.STATUS_DELIVERED -> DeliveryEntity.STATUS_DELIVERED
                OrderEntity.STATUS_CANCELLED -> DeliveryEntity.STATUS_FAILED
                else -> delivery.status
            }
            deliveryDao.updateDelivery(delivery.copy(status = newDeliveryStatus))
        }

        Result.success(Unit)
    }

    private suspend fun deductOrderStock(order: OrderEntity, items: List<OrderItemEntity>): Result<Unit> {
        val currentProducts = allProducts.first().associateBy { it.id }
        // Pre-check all items to ensure none will drop below zero
        for (item in items) {
            val prod = currentProducts[item.productId]
            if (prod != null && item.quantity > prod.quantity) {
                return Result.failure(
                    Exception("Cannot dispatch! Item '${item.productName}' requires ${item.quantity} ${item.unit}, but stock is only ${prod.quantity} ${item.unit}.")
                )
            }
        }

        for (item in items) {
            val prod = currentProducts[item.productId]
            if (prod != null) {
                val newQty = (prod.quantity - item.quantity).coerceAtLeast(0.0)
                productDao.updateQuantity(item.productId, newQty)
                movementDao.insertMovement(
                    StockMovementEntity(
                        productId = item.productId,
                        productName = item.productName,
                        changeAmount = -item.quantity,
                        remainingQuantity = newQty,
                        reason = "Order Dispatched",
                        note = "Dispatched for ${order.orderNumber} (${order.customerName})"
                    )
                )
            }
        }
        return Result.success(Unit)
    }

    private suspend fun restoreOrderStock(order: OrderEntity, items: List<OrderItemEntity>) {
        val currentProducts = allProducts.first().associateBy { it.id }
        for (item in items) {
            val prod = currentProducts[item.productId]
            if (prod != null) {
                val newQty = prod.quantity + item.quantity
                productDao.updateQuantity(item.productId, newQty)
                movementDao.insertMovement(
                    StockMovementEntity(
                        productId = item.productId,
                        productName = item.productName,
                        changeAmount = item.quantity,
                        remainingQuantity = newQty,
                        reason = "Order Cancelled",
                        note = "Stock restored from cancelled order ${order.orderNumber}"
                    )
                )
            }
        }
    }

    suspend fun deleteOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        val items = orderItemDao.getItemsForOrder(order.id)
        if (order.stockDeducted) {
            restoreOrderStock(order, items)
        }
        orderItemDao.deleteItemsForOrder(order.id)
        orderDao.deleteOrder(order)
        val delivery = deliveryDao.getDeliveryByOrderId(order.id)
        if (delivery != null) {
            deliveryDao.deleteDelivery(delivery)
        }
    }

    suspend fun getItemsForOrder(orderId: Long): List<OrderItemEntity> = withContext(Dispatchers.IO) {
        orderItemDao.getItemsForOrder(orderId)
    }

    // ---------------- PAYMENT OPERATIONS ----------------
    suspend fun insertPayment(payment: PaymentEntity): Result<Long> = withContext(Dispatchers.IO) {
        if (payment.amount <= 0.0) {
            return@withContext Result.failure(Exception("Payment amount must be greater than zero."))
        }
        val id = paymentDao.insertPayment(payment)
        Result.success(id)
    }

    suspend fun deletePayment(payment: PaymentEntity) = withContext(Dispatchers.IO) {
        paymentDao.deletePayment(payment)
    }

    // ---------------- DELIVERY OPERATIONS ----------------
    suspend fun updateDelivery(delivery: DeliveryEntity) = withContext(Dispatchers.IO) {
        deliveryDao.updateDelivery(delivery)
        // If delivery marked delivered, also sync order status to Delivered
        if (delivery.status == DeliveryEntity.STATUS_DELIVERED) {
            val order = orderDao.getOrderById(delivery.orderId)
            if (order != null && order.status != OrderEntity.STATUS_DELIVERED) {
                updateOrderStatus(order.id, OrderEntity.STATUS_DELIVERED)
            }
        }
    }

    // ---------------- SEED & RESET ----------------
    suspend fun loadDemoData() = withContext(Dispatchers.IO) {
        clearAllDemoData()
        val now = System.currentTimeMillis()
        val products = SampleData.getSampleProducts(now)
        productDao.insertAllProducts(products)

        val customers = SampleData.getSampleCustomers()
        customerDao.insertAllCustomers(customers)

        val sampleOrders = SampleData.getSampleOrders(now)
        for ((order, items) in sampleOrders) {
            orderDao.insertOrder(order)
            orderItemDao.insertItems(items)
        }

        val payments = SampleData.getSamplePayments(now)
        paymentDao.insertAllPayments(payments)

        val deliveries = SampleData.getSampleDeliveries(now)
        deliveryDao.insertAllDeliveries(deliveries)
    }

    suspend fun clearAllDemoData() = withContext(Dispatchers.IO) {
        productDao.deleteAllDemoProducts()
        customerDao.deleteAllDemoCustomers()
        orderDao.deleteAllDemoOrders()
        paymentDao.deleteAllDemoPayments()
        deliveryDao.deleteAllDemoDeliveries()
    }

    suspend fun clearEverything() = withContext(Dispatchers.IO) {
        productDao.deleteAll()
        movementDao.deleteAll()
        customerDao.deleteAll()
        orderDao.deleteAll()
        orderItemDao.deleteAll()
        paymentDao.deleteAll()
        deliveryDao.deleteAll()
    }
}
