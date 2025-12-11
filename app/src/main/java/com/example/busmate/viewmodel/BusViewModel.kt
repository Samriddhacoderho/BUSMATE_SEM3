package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.data.BusRepositoryInterface
import com.example.busmate.model.BusModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class BusViewModel(
    private val repository: BusRepositoryInterface = BusRepositoryImpl()
) : ViewModel() {

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    fun registerBus(
        busNumber: String,
        licensePlate: String,
        routeId: String,
        capacity: Int
    ) {
        viewModelScope.launch {
            _message.value = "Loading"

            try {
                // 1. Construct the Final Bus Model
                val bus = BusModel(
                    busNumber = busNumber.trim(),
                    licensePlate = licensePlate.trim().uppercase(),
                    routeId = routeId.trim(),
                    capacity = capacity,
                    driver = null
                )

                // 2. Call the Repository
                repository.registerBus(bus) { responseMessage, success ->
                    _message.value = responseMessage
                }

            } catch (e: Exception) {
                _message.value = e.message ?: "An unexpected error occurred during bus registration."
            }
        }
    }
}