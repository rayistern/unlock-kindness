package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pennydrop_prefs", Context.MODE_PRIVATE)

    // Automation Active
    private val _isAutomationActive = MutableStateFlow(prefs.getBoolean("automation_active", true))
    val isAutomationActive: StateFlow<Boolean> = _isAutomationActive.asStateFlow()

    // Recipient Name
    val recipientName: StateFlow<String> = MutableStateFlow("Merkos 302")

    // Recipient Contact (CashApp $Cashtag)
    val recipientContact: StateFlow<String> = MutableStateFlow("\$merkos302")

    // Custom Amount Per Unlock
    private val _amountPerUnlock = MutableStateFlow(prefs.getFloat("amount_per_unlock", 0.02f))
    val amountPerUnlock: StateFlow<Float> = _amountPerUnlock.asStateFlow()

    fun setAutomationActive(active: Boolean) {
        prefs.edit().putBoolean("automation_active", active).apply()
        _isAutomationActive.value = active
    }

    fun setAmountPerUnlock(amount: Float) {
        prefs.edit().putFloat("amount_per_unlock", amount).apply()
        _amountPerUnlock.value = amount
    }
}
