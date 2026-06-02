package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.data.AppDatabase
import com.example.data.PreferencesManager
import com.example.data.UnlockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class UnlockService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var repository: UnlockRepository
    private lateinit var preferencesManager: PreferencesManager
    private var lastReminderTime = 0L

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_USER_PRESENT) {
                val amount = preferencesManager.amountPerUnlock.value.toDouble()
                val recipientName = preferencesManager.recipientName.value
                val cashtag = preferencesManager.recipientContact.value
                
                serviceScope.launch {
                    repository.logUnlock(
                        amount = amount,
                        recipientName = recipientName,
                        cashtag = cashtag
                    )
                    
                    val balance = repository.getUnpaidBalanceSync()
                    showUnlockNotification(amount)
                    
                    if (balance >= 5.0) {
                        showBalanceReminderNotification(balance)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        repository = UnlockRepository(database.unlockDao())
        preferencesManager = PreferencesManager(this)
        
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        ContextCompat.registerReceiver(this, unlockReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, createNotification())
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(unlockReceiver)
        } catch (e: Exception) {}
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pennydrop_service",
                "PennyDrop Automation",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "pennydrop_service")
            .setContentTitle("Merkos 302 Active")
            .setContentText("Listening for phone unlocks.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun showUnlockNotification(amount: Double) {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("pennydrop_alerts", "Donation Alerts", NotificationManager.IMPORTANCE_DEFAULT)
            manager?.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, "pennydrop_alerts")
            .setContentTitle("Merkos 302")
            .setContentText("You just donated $${String.format("%.2f", amount)}!")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager?.notify(3, notification)
    }

    private fun showBalanceReminderNotification(balance: Double) {
        val now = System.currentTimeMillis()
        if (now - lastReminderTime < 86_400_000) return
        lastReminderTime = now
        
        val manager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, "pennydrop_alerts")
            .setContentTitle("Time to Send!")
            .setContentText("You hit $${String.format("%.2f", balance)}! Send to Merkos 302 via CashApp.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        manager?.notify(2, notification)
    }
}
