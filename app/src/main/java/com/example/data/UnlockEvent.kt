package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlock_events")
data class UnlockEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val amount: Double = 0.02,
    val recipientName: String = "Jordan Miller",
    val recipientContact: String = "\$jordanmiller",
    val status: String = "UNPAID", // "UNPAID", "PAID"
    val errorMessage: String? = null
)
