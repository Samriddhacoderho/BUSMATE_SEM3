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

    // Store the active bus ID here for speed alert logic
    private var currentBusId: String = ""

    private val _state = mutableStateOf(AccelerometerModel())
    val state: State<AccelerometerModel> = _state

    // Observer to bridge Repository data to Compose State
    private val speedObserver = Observer<Float> { newSpeed ->
        _state.value = _state.value.copy(speedMps = newSpeed)

        // Trigger the speed check (Now using accurate GPS speed)
        if (currentBusId.isNotEmpty()) {
            repository.checkSpeedAlert(newSpeed, currentBusId)
        }
    }

    init {
        // Start observing the Repository's speed source
        repository.currentSpeedMps.observeForever(speedObserver)
    }

    /**
     * Called by TripActivity when "START TRIP" is clicked.
     */
    fun startMeasurement(driverUid: String, busRouteId: String) {
        currentBusId = busRouteId
        // Update local UI state
        _state.value = _state.value.copy(isRunning = true)

        // 1. Update Firebase 'isTripRunning: true'
        if (busRouteId.isNotEmpty()) {
            repository.updateTripRunning(busRouteId, true)
        }

        // 2. Start GPS Listening
        repository.startListening(driverUid)
    }

    /**
     * Called by TripActivity when "STOP TRIP" is clicked.
     */
    fun stopMeasurement(busRouteId: String) {
        // 1. Stop GPS updates
        repository.stopListening()

        // 2. Update Firebase 'isTripRunning: false'
        if (busRouteId.isNotEmpty()) {
            repository.updateTripRunning(busRouteId, false)
        }

        // 3. Reset local state
        currentBusId = ""
        _state.value = _state.value.copy(isRunning = false, speedMps = 0f)
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopListening()
        // Prevent memory leaks
        repository.currentSpeedMps.removeObserver(speedObserver)
    }
}