package com.example.busmate.data

import com.example.busmate.model.EmergencyModel

interface EmergencyRepositoryInterface {
    fun sendSOS(alert: EmergencyModel, onComplete: (Boolean) -> Unit)
    fun observeAlerts(callback: (List<EmergencyModel>) -> Unit)
}