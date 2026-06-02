package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockDao {
    @Insert
    suspend fun insert(event: UnlockEvent)

    @Query("SELECT COUNT(*) FROM unlock_events WHERE timestamp >= :startOfDay")
    fun getUnlocksToday(startOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM unlock_events")
    fun getTotalUnlocks(): Flow<Int>

    @Query("SELECT * FROM unlock_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<UnlockEvent>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM unlock_events WHERE status = 'PAID'")
    fun getTotalAmountSent(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM unlock_events WHERE status = 'UNPAID'")
    fun getUnpaidBalance(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM unlock_events WHERE status = 'UNPAID'")
    suspend fun getUnpaidBalanceSync(): Double

    @Query("UPDATE unlock_events SET status = 'PAID' WHERE status = 'UNPAID'")
    suspend fun markAllAsPaid()
}
