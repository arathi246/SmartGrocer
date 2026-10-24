package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val lowStockThreshold: Double = 10.0,
    val expiryDateEpoch: Long? = null,
    val supplierName: String = "",
    val batchNumber: String = "",
    val isDemo: Boolean = false
) {
    val isLowStock: Boolean
        get() = quantity <= lowStockThreshold

    fun daysUntilExpiry(currentEpochMs: Long = System.currentTimeMillis()): Long? {
        val exp = expiryDateEpoch ?: return null
        return (exp - currentEpochMs) / (1000L * 60 * 60 * 24)
    }

    fun isExpired(currentEpochMs: Long = System.currentTimeMillis()): Boolean {
        val days = daysUntilExpiry(currentEpochMs) ?: return false
        return days < 0
    }

    fun isNearingExpiry(warningDays: Long = 30, currentEpochMs: Long = System.currentTimeMillis()): Boolean {
        val days = daysUntilExpiry(currentEpochMs) ?: return false
        return days in 0..warningDays
    }
}
