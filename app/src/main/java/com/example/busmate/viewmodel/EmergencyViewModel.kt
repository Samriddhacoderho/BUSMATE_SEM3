package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.busmate.data.EmergencyRepositoryInterface
import com.example.busmate.model.EmergencyModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EmergencyViewModel(private val repository: EmergencyRepositoryInterface) : ViewModel() {
    private val _alerts = MutableStateFlow<List<EmergencyModel>>(emptyList())
    val alerts: StateFlow<List<EmergencyModel>> = _alerts

    init {
        repository.observeAlerts { _alerts.value = it }
    }

    fun triggerSOS(driverName: String, busId: String, onSent: (Boolean) -> Unit) {
        val alert = EmergencyModel(
            driverName = driverName,
            busId = busId,
            message = "EMERGENCY: Driver $driverName has triggered an SOS!",
            timestamp = System.currentTimeMillis()
        )
        repository.sendSOS(alert, onSent)
    }
}