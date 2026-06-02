package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnlockRepository(private val unlockDao: UnlockDao) {

    fun getUnlocksToday(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return unlockDao.getUnlocksToday(calendar.timeInMillis)
    }

    fun getTotalUnlocks(): Flow<Int> = unlockDao.getTotalUnlocks()

    fun getAllEvents(): Flow<List<UnlockEvent>> = unlockDao.getAllEvents()

    fun getTotalAmountSent(): Flow<Double> = unlockDao.getTotalAmountSent()
    
    fun getUnpaidBalance(): Flow<Double> = unlockDao.getUnpaidBalance()

    suspend fun getUnpaidBalanceSync(): Double = withContext(Dispatchers.IO) {
        unlockDao.getUnpaidBalanceSync()
    }

    suspend fun logUnlock(
        amount: Double,
        recipientName: String,
        cashtag: String
    ): UnlockEvent = withContext(Dispatchers.IO) {
        val event = UnlockEvent(
            amount = amount,
            recipientName = recipientName,
            recipientContact = cashtag,
            status = "UNPAID"
        )
        unlockDao.insert(event)
        event
    }
    
    suspend fun markAllAsPaid() = withContext(Dispatchers.IO) {
        unlockDao.markAllAsPaid()
    }
}
