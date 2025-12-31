package com.example.busmate.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import com.example.busmate.data.AccelerometerRepository
import com.example.busmate.data.AccelerometerRepositoryImpl
import com.example.busmate.model.AccelerometerModel

class AccelerometerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AccelerometerRepository = AccelerometerRepositoryImpl(application.applicationContext)

    // Store the active bus ID here
    private var currentBusId: String = ""

    private val _state = mutableStateOf(AccelerometerModel())
    val state: State<AccelerometerModel> = _state

    // Combine your observers into this one
    private val speedObserver = Observer<Float> { newSpeed ->
        _state.value = _state.value.copy(speedMps = newSpeed)

        // Trigger the speed check if we have a valid bus ID
        if (currentBusId.isNotEmpty()) {
            repository.checkSpeedAlert(newSpeed, currentBusId)
        }
    }

    init {
        repository.currentSpeedMps.observeForever(speedObserver)
    }

    fun startMeasurement(driverUid: String, busRouteId: String) {
        currentBusId = busRouteId // Save the bus ID for the speed checker
        _state.value = _state.value.copy(isRunning = true)
        repository.startListening(driverUid)
        if (busRouteId.isNotEmpty()) {
            repository.updateTripRunning(busRouteId, true)
        }
    }

    fun stopMeasurement(busRouteId: String) {
        repository.stopListening()
        currentBusId = "" // Clear the bus ID
        _state.value = _state.value.copy(isRunning = false, speedMps = 0f)
        if (busRouteId.isNotEmpty()) {
            repository.updateTripRunning(busRouteId, false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopListening()
        repository.currentSpeedMps.removeObserver(speedObserver)
    }
}