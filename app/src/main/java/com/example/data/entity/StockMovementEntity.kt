package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val changeAmount: Double, // positive for addition, negative for deduction
    val remainingQuantity: Double,
    val reason: String, // "Purchase Received", "Order Dispatched", "Order Cancelled", "Damaged/Spoiled", "Correction"
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
