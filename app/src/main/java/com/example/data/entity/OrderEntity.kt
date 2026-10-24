package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val customerId: Long,
    val customerName: String,
    val customerPhone: String = "",
    val deliveryAddress: String = "",
    val orderDateEpoch: Long = System.currentTimeMillis(),
    val status: String, // Draft, Confirmed, Packed, Dispatched, Delivered, Cancelled
    val subtotal: Double,
    val discount: Double = 0.0,
    val totalAmount: Double,
    val notes: String = "",
    val stockDeducted: Boolean = false,
    val isDemo: Boolean = false
) {
    companion object {
        const val STATUS_DRAFT = "Draft"
        const val STATUS_CONFIRMED = "Confirmed"
        const val STATUS_PACKED = "Packed"
        const val STATUS_DISPATCHED = "Dispatched"
        const val STATUS_DELIVERED = "Delivered"
        const val STATUS_CANCELLED = "Cancelled"

        val ALL_STATUSES = listOf(
            STATUS_DRAFT,
            STATUS_CONFIRMED,
            STATUS_PACKED,
            STATUS_DISPATCHED,
            STATUS_DELIVERED,
            STATUS_CANCELLED
        )
    }
}
