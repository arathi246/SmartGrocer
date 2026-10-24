package com.example.data

import com.example.data.entity.CustomerEntity
import com.example.data.entity.DeliveryEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProductEntity

object SampleData {
    private val DAY_MS = 24L * 60 * 60 * 1000

    fun getSampleProducts(now: Long = System.currentTimeMillis()): List<ProductEntity> {
        return listOf(
            ProductEntity(
                id = 1,
                name = "Royal Basmati Rice (25kg)",
                sku = "RICE-BAS-25K",
                category = "Grains & Rice",
                quantity = 42.0,
                unit = "bag",
                purchasePrice = 1450.0,
                sellingPrice = 1750.0,
                lowStockThreshold = 10.0,
                expiryDateEpoch = now + (180 * DAY_MS), // 6 months
                supplierName = "Heritage Agro Mills",
                batchNumber = "BATCH-2026-A1",
                isDemo = true
            ),
            ProductEntity(
                id = 2,
                name = "Golden Pure Sunflower Oil (1L Pouch)",
                sku = "OIL-SUN-1L",
                category = "Edible Oils",
                quantity = 120.0,
                unit = "packet",
                purchasePrice = 110.0,
                sellingPrice = 135.0,
                lowStockThreshold = 25.0,
                expiryDateEpoch = now + (120 * DAY_MS), // 4 months
                supplierName = "Apex Refineries",
                batchNumber = "OIL-882B",
                isDemo = true
            ),
            ProductEntity(
                id = 3,
                name = "Whole Wheat Chakki Atta (10kg)",
                sku = "FLR-WHT-10K",
                category = "Flour & Bakery",
                quantity = 6.0, // LOW STOCK! Threshold is 15
                unit = "bag",
                purchasePrice = 320.0,
                sellingPrice = 390.0,
                lowStockThreshold = 15.0,
                expiryDateEpoch = now + (60 * DAY_MS), // 2 months
                supplierName = "Kisan Foods Ltd",
                batchNumber = "ATTA-091",
                isDemo = true
            ),
            ProductEntity(
                id = 4,
                name = "Premium Toor Dal Unpolished (1kg)",
                sku = "PUL-TOR-1K",
                category = "Pulses & Lentils",
                quantity = 85.0,
                unit = "packet",
                purchasePrice = 125.0,
                sellingPrice = 155.0,
                lowStockThreshold = 20.0,
                expiryDateEpoch = now + (90 * DAY_MS),
                supplierName = "Green Agro Direct",
                batchNumber = "PULSE-44A",
                isDemo = true
            ),
            ProductEntity(
                id = 5,
                name = "Farm Fresh UHT Milk (1L Tetra Pak)",
                sku = "DRY-MLK-1L",
                category = "Dairy & Beverages",
                quantity = 35.0,
                unit = "carton",
                purchasePrice = 52.0,
                sellingPrice = 68.0,
                lowStockThreshold = 20.0,
                expiryDateEpoch = now + (12 * DAY_MS), // EXPIRING SOON! (12 days) -> FEFO dispatch
                supplierName = "Valley Dairy Coop",
                batchNumber = "MLK-TET-302",
                isDemo = true
            ),
            ProductEntity(
                id = 6,
                name = "Malabar Special Garam Masala (200g)",
                sku = "SPC-MAS-200G",
                category = "Spices & Seasoning",
                quantity = 75.0,
                unit = "box",
                purchasePrice = 65.0,
                sellingPrice = 90.0,
                lowStockThreshold = 15.0,
                expiryDateEpoch = now + (150 * DAY_MS),
                supplierName = "Spice Land Exporters",
                batchNumber = "SPC-2026-03",
                isDemo = true
            ),
            ProductEntity(
                id = 7,
                name = "Rich Plum Cake Gift Box (500g)",
                sku = "BAK-CAK-500G",
                category = "Flour & Bakery",
                quantity = 18.0,
                unit = "box",
                purchasePrice = 140.0,
                sellingPrice = 210.0,
                lowStockThreshold = 10.0,
                expiryDateEpoch = now + (5 * DAY_MS), // EXPIRING VERY SOON! (5 days)
                supplierName = "Golden Crust Bakers",
                batchNumber = "CAKE-DEC-1",
                isDemo = true
            )
        )
    }

    fun getSampleCustomers(): List<CustomerEntity> {
        return listOf(
            CustomerEntity(
                id = 1,
                name = "Green Valley Supermarket",
                phone = "9845012345",
                address = "Market Road, Ward 4, Downtown",
                notes = "Requires morning deliveries before 10 AM. GST registered.",
                isDemo = true
            ),
            CustomerEntity(
                id = 2,
                name = "Annapurna Provisions & Spices",
                phone = "9876543210",
                address = "Shop #12, Cross Bazaar, South Gate",
                notes = "Weekly payment cycle on Saturdays.",
                isDemo = true
            ),
            CustomerEntity(
                id = 3,
                name = "Sunrise Bakeries & Cafe",
                phone = "9447123987",
                address = "Highway Junction, Near Metro Pillar 140",
                notes = "Bulk buyer for flour, sugar, and dairy.",
                isDemo = true
            )
        )
    }

    fun getSampleOrders(now: Long = System.currentTimeMillis()): List<Pair<OrderEntity, List<OrderItemEntity>>> {
        val ord1 = OrderEntity(
            id = 1,
            orderNumber = "ORD-1001",
            customerId = 1,
            customerName = "Green Valley Supermarket",
            customerPhone = "9845012345",
            deliveryAddress = "Market Road, Ward 4, Downtown",
            orderDateEpoch = now - (2 * DAY_MS),
            status = OrderEntity.STATUS_DELIVERED,
            subtotal = 6550.0,
            discount = 150.0,
            totalAmount = 6400.0,
            notes = "Invoice #GV-889. Delivered and accepted.",
            stockDeducted = true,
            isDemo = true
        )
        val items1 = listOf(
            OrderItemEntity(
                id = 1,
                orderId = 1,
                productId = 1,
                productName = "Royal Basmati Rice (25kg)",
                quantity = 2.0,
                unit = "bag",
                unitPrice = 1750.0,
                totalPrice = 3500.0
            ),
            OrderItemEntity(
                id = 2,
                orderId = 1,
                productId = 2,
                productName = "Golden Pure Sunflower Oil (1L Pouch)",
                quantity = 10.0,
                unit = "packet",
                unitPrice = 135.0,
                totalPrice = 1350.0
            ),
            OrderItemEntity(
                id = 3,
                orderId = 1,
                productId = 5,
                productName = "Farm Fresh UHT Milk (1L Tetra Pak)",
                quantity = 25.0,
                unit = "carton",
                unitPrice = 68.0,
                totalPrice = 1700.0
            )
        )

        val ord2 = OrderEntity(
            id = 2,
            orderNumber = "ORD-1002",
            customerId = 2,
            customerName = "Annapurna Provisions & Spices",
            customerPhone = "9876543210",
            deliveryAddress = "Shop #12, Cross Bazaar, South Gate",
            orderDateEpoch = now - (1 * DAY_MS),
            status = OrderEntity.STATUS_DISPATCHED,
            subtotal = 4220.0,
            discount = 0.0,
            totalAmount = 4220.0,
            notes = "Out for delivery with driver Ramesh.",
            stockDeducted = true,
            isDemo = true
        )
        val items2 = listOf(
            OrderItemEntity(
                id = 4,
                orderId = 2,
                productId = 4,
                productName = "Premium Toor Dal Unpolished (1kg)",
                quantity = 20.0,
                unit = "packet",
                unitPrice = 155.0,
                totalPrice = 3100.0
            ),
            OrderItemEntity(
                id = 5,
                orderId = 2,
                productId = 6,
                productName = "Malabar Special Garam Masala (200g)",
                quantity = 12.0,
                unit = "box",
                unitPrice = 90.0,
                totalPrice = 1080.0
            ),
            OrderItemEntity(
                id = 6,
                orderId = 2,
                productId = 2,
                productName = "Golden Pure Sunflower Oil (1L Pouch)",
                quantity = 1.0,
                unit = "packet",
                unitPrice = 135.0,
                totalPrice = 135.0
            )
        )

        val ord3 = OrderEntity(
            id = 3,
            orderNumber = "ORD-1003",
            customerId = 3,
            customerName = "Sunrise Bakeries & Cafe",
            customerPhone = "9447123987",
            deliveryAddress = "Highway Junction, Near Metro Pillar 140",
            orderDateEpoch = now - (4 * 3600 * 1000), // today
            status = OrderEntity.STATUS_CONFIRMED,
            subtotal = 2730.0,
            discount = 0.0,
            totalAmount = 2730.0,
            notes = "Ready to pack. High priority for afternoon batch.",
            stockDeducted = false,
            isDemo = true
        )
        val items3 = listOf(
            OrderItemEntity(
                id = 7,
                orderId = 3,
                productId = 3,
                productName = "Whole Wheat Chakki Atta (10kg)",
                quantity = 4.0,
                unit = "bag",
                unitPrice = 390.0,
                totalPrice = 1560.0
            ),
            OrderItemEntity(
                id = 8,
                orderId = 3,
                productId = 7,
                productName = "Rich Plum Cake Gift Box (500g)",
                quantity = 5.0,
                unit = "box",
                unitPrice = 210.0,
                totalPrice = 1050.0
            ),
            OrderItemEntity(
                id = 9,
                orderId = 3,
                productId = 5,
                productName = "Farm Fresh UHT Milk (1L Tetra Pak)",
                quantity = 2.0,
                unit = "carton",
                unitPrice = 60.0,
                totalPrice = 120.0
            )
        )

        return listOf(
            Pair(ord1, items1),
            Pair(ord2, items2),
            Pair(ord3, items3)
        )
    }

    fun getSamplePayments(now: Long = System.currentTimeMillis()): List<PaymentEntity> {
        return listOf(
            PaymentEntity(
                id = 1,
                customerId = 1,
                customerName = "Green Valley Supermarket",
                orderId = 1,
                amount = 4000.0, // Partial payment against 6400.0 total
                paymentDateEpoch = now - (1 * DAY_MS),
                paymentMethod = PaymentEntity.METHOD_UPI,
                referenceNumber = "UPI-REF-992810",
                notes = "First instalment via GPay. Balance 2400.0 due next week.",
                isDemo = true
            ),
            PaymentEntity(
                id = 2,
                customerId = 2,
                customerName = "Annapurna Provisions & Spices",
                orderId = 2,
                amount = 2000.0, // Partial payment against 4220.0
                paymentDateEpoch = now - (12 * 3600 * 1000),
                paymentMethod = PaymentEntity.METHOD_CASH,
                referenceNumber = "RCPT-0041",
                notes = "Cash advance paid on dispatch.",
                isDemo = true
            )
        )
    }

    fun getSampleDeliveries(now: Long = System.currentTimeMillis()): List<DeliveryEntity> {
        return listOf(
            DeliveryEntity(
                id = 1,
                orderId = 2,
                orderNumber = "ORD-1002",
                customerName = "Annapurna Provisions & Spices",
                deliveryAddress = "Shop #12, Cross Bazaar, South Gate",
                dispatchDateEpoch = now - (2 * 3600 * 1000),
                status = DeliveryEntity.STATUS_DISPATCHED,
                driverName = "Ramesh (Van 4)",
                notes = "Contact shop owner before offloading.",
                isDemo = true
            ),
            DeliveryEntity(
                id = 2,
                orderId = 1,
                orderNumber = "ORD-1001",
                customerName = "Green Valley Supermarket",
                deliveryAddress = "Market Road, Ward 4, Downtown",
                dispatchDateEpoch = now - (2 * DAY_MS),
                status = DeliveryEntity.STATUS_DELIVERED,
                driverName = "Anil (Truck 1)",
                notes = "Signed by receiving manager Mr. Paul.",
                isDemo = true
            )
        )
    }
}
