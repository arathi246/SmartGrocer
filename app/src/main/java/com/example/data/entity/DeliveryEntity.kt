package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deliveries")
data class DeliveryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val orderNumber: String,
    val customerName: String,
    val deliveryAddress: String,
    val dispatchDateEpoch: Long = System.currentTimeMillis(),
    val status: String, // Pending, Dispatched, Delivered, Failed
    val driverName: String = "",
    val notes: String = "",
    val isDemo: Boolean = false
) {
    companion object {
        const val STATUS_PENDING = "Pending"
        const val STATUS_DISPATCHED = "Dispatched"
        const val STATUS_DELIVERED = "Delivered"
        const val STATUS_FAILED = "Failed"

        val ALL_STATUSES = listOf(
            STATUS_PENDING,
            STATUS_DISPATCHED,
            STATUS_DELIVERED,
            STATUS_FAILED
        )
    }
}
