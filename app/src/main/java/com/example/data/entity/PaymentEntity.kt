package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val orderId: Long? = null,
    val amount: Double,
    val paymentDateEpoch: Long = System.currentTimeMillis(),
    val paymentMethod: String, // Cash, Bank Transfer, UPI, Cheque, Other
    val referenceNumber: String = "",
    val notes: String = "",
    val isDemo: Boolean = false
) {
    companion object {
        const val METHOD_CASH = "Cash"
        const val METHOD_UPI = "UPI"
        const val METHOD_BANK_TRANSFER = "Bank Transfer"
        const val METHOD_CHEQUE = "Cheque"
        const val METHOD_OTHER = "Other"

        val ALL_METHODS = listOf(
            METHOD_CASH,
            METHOD_UPI,
            METHOD_BANK_TRANSFER,
            METHOD_CHEQUE,
            METHOD_OTHER
        )
    }
}
