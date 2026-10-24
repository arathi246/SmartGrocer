package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.CustomerEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProductEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: AppRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = AppDatabase.getDatabase(context)
        repository = AppRepository(database)
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Smart Food Distribution Manager", appName)
    }

    @Test
    fun `test product stock and FEFO logic`() {
        val now = System.currentTimeMillis()
        val dayMs = 24L * 60 * 60 * 1000

        val product = ProductEntity(
            name = "Test Rice",
            sku = "RICE-01",
            category = "Grains & Rice",
            quantity = 5.0,
            unit = "bag",
            purchasePrice = 100.0,
            sellingPrice = 130.0,
            lowStockThreshold = 10.0,
            expiryDateEpoch = now + (10 * dayMs) // 10 days
        )

        assertTrue(product.isLowStock)
        assertFalse(product.isExpired(now))
        assertTrue(product.isNearingExpiry(30, now))
    }

    @Test
    fun `test order stock deduction on dispatch`() = runBlocking {
        // Clear all to have clean state
        repository.clearEverything()

        // Insert a product with 50 units
        val prodId = repository.insertProduct(
            ProductEntity(
                name = "Wheat Flour",
                sku = "FLR-WHT",
                category = "Flour",
                quantity = 50.0,
                unit = "bag",
                purchasePrice = 200.0,
                sellingPrice = 250.0,
                lowStockThreshold = 10.0
            )
        )

        val custId = repository.insertCustomer(
            CustomerEntity(name = "Test Bakery", phone = "1234567890")
        )

        val order = OrderEntity(
            orderNumber = "ORD-TEST-1",
            customerId = custId,
            customerName = "Test Bakery",
            status = OrderEntity.STATUS_CONFIRMED,
            subtotal = 500.0,
            totalAmount = 500.0
        )

        val items = listOf(
            OrderItemEntity(
                orderId = 0,
                productId = prodId,
                productName = "Wheat Flour",
                quantity = 10.0,
                unit = "bag",
                unitPrice = 250.0,
                totalPrice = 500.0
            )
        )

        // Create order
        val createResult = repository.createOrder(order, items)
        assertTrue(createResult.isSuccess)
        val orderId = createResult.getOrThrow()

        // Stock should still be 50 because status is Confirmed, not Dispatched
        var currentProds = repository.allProducts.first()
        assertEquals(50.0, currentProds.first().quantity, 0.01)

        // Now dispatch the order
        val dispatchResult = repository.updateOrderStatus(orderId, OrderEntity.STATUS_DISPATCHED)
        assertTrue(dispatchResult.isSuccess)

        // Stock should now be deducted by 10 (remaining: 40)
        currentProds = repository.allProducts.first()
        assertEquals(40.0, currentProds.first().quantity, 0.01)

        // If order is cancelled, stock should be restored to 50
        val cancelResult = repository.updateOrderStatus(orderId, OrderEntity.STATUS_CANCELLED)
        assertTrue(cancelResult.isSuccess)

        currentProds = repository.allProducts.first()
        assertEquals(50.0, currentProds.first().quantity, 0.01)
    }

    @Test
    fun `test customer financial balance calculations`() = runBlocking {
        repository.clearEverything()

        val custId = repository.insertCustomer(
            CustomerEntity(name = "Metro Mart", phone = "9998887776")
        )

        // Add order of 1500.0
        repository.createOrder(
            OrderEntity(
                orderNumber = "ORD-FIN-1",
                customerId = custId,
                customerName = "Metro Mart",
                status = OrderEntity.STATUS_DELIVERED,
                subtotal = 1500.0,
                totalAmount = 1500.0
            ),
            listOf(
                OrderItemEntity(
                    orderId = 0,
                    productId = 999,
                    productName = "Sample",
                    quantity = 1.0,
                    unit = "box",
                    unitPrice = 1500.0,
                    totalPrice = 1500.0
                )
            ),
            autoCreateDelivery = false
        )

        // Record partial payment of 500.0
        repository.insertPayment(
            PaymentEntity(
                customerId = custId,
                customerName = "Metro Mart",
                amount = 500.0,
                paymentMethod = PaymentEntity.METHOD_CASH
            )
        )

        val orders = repository.allOrders.first()
        val payments = repository.allPayments.first()

        val totalBilled = orders.filter { it.customerId == custId && it.status != OrderEntity.STATUS_CANCELLED }.sumOf { it.totalAmount }
        val totalPaid = payments.filter { it.customerId == custId }.sumOf { it.amount }
        val balance = totalBilled - totalPaid

        assertEquals(1500.0, totalBilled, 0.01)
        assertEquals(500.0, totalPaid, 0.01)
        assertEquals(1000.0, balance, 0.01)
    }
}
