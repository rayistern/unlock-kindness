package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesManager
import com.example.data.UnlockRepository
import com.example.data.UnlockEvent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PennyDropViewModel(
    private val repository: UnlockRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // Preferences
    val isAutomationActive: StateFlow<Boolean> = preferencesManager.isAutomationActive
    val recipientName: StateFlow<String> = preferencesManager.recipientName
    val cashtag: StateFlow<String> = preferencesManager.recipientContact
    val amountPerUnlock: StateFlow<Float> = preferencesManager.amountPerUnlock

    // Stats and History
    val unlocksToday: StateFlow<Int> = repository.getUnlocksToday()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalSent: StateFlow<Double> = repository.getTotalAmountSent()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val unpaidBalance: StateFlow<Double> = repository.getUnpaidBalance()
        .stateIn(
             scope = viewModelScope,
             started = SharingStarted.WhileSubscribed(5000),
             initialValue = 0.0
        )

    val allEvents: StateFlow<List<UnlockEvent>> = repository.getAllEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Setters
    fun toggleAutomation(active: Boolean) {
        preferencesManager.setAutomationActive(active)
    }

    fun updateAmountPerUnlock(amount: Float) {
        preferencesManager.setAmountPerUnlock(amount)
    }
    
    fun markAllAsPaid() {
        viewModelScope.launch {
            repository.markAllAsPaid()
        }
    }
}
