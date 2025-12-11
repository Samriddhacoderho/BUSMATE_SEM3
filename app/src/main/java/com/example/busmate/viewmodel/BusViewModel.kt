package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.data.BusRepositoryInterface
import com.example.busmate.model.BusModel
import com.example.busmate.model.DriverModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BusViewModel(
    private val repository: BusRepositoryInterface = BusRepositoryImpl()
) : ViewModel() {

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    fun registerBus(
        schoolId: String,
        busNumber: String,
        licensePlate: String,
        routeId: String,
        capacity: Int,
        driverId: String,
        driverFirstName: String,
        driverLastName: String,
        driverPhone: String,
        driverLicenseNumber: String
    ) {
        viewModelScope.launch {
            _message.value = "Loading"

            try {
                // 1. Construct Nested Driver Model
                val driverProfile = DriverModel(
                    driverId = driverId.trim(),
                    firstName = driverFirstName.trim(),
                    lastName = driverLastName.trim(),
                    phone = driverPhone.trim(),
                    licenseNumber = driverLicenseNumber.trim()
                )

                // 2. Construct the Final Bus Model
                val bus = BusModel(
                    busNumber = busNumber.trim(),
                    licensePlate = licensePlate.trim().uppercase(),
                    schoolId = schoolId,
                    routeId = routeId.trim(),
                    capacity = capacity,
                    driver = driverProfile
                )

                // 3. Call the Repository
                repository.registerBus(bus) { responseMessage, success ->
                    _message.value = responseMessage
                }

            } catch (e: Exception) {
                _message.value = e.message ?: "An unexpected error occurred."
            }
        }
    }
}