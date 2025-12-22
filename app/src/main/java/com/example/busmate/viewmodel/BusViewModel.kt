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
    private val _busStatus = MutableStateFlow<BusModel?>(null)
    val busStatus: StateFlow<BusModel?> = _busStatus

    fun registerBus(
        busNumber: String,
        licensePlate: String,
        routeId: String,
        capacity: Int
    ) {
        _message.value = "Loading"

        try {
            val bus = BusModel(
                busNumber = busNumber.trim(),
                licensePlate = licensePlate.trim().uppercase(),
                routeId = routeId.trim(),
                capacity = capacity,
                driver = null
            )

            repository.registerBus(bus) { responseMessage, _ ->
                _message.value = responseMessage
            }

        } catch (e: Exception) {
            _message.value =
                e.message ?: "An unexpected error occurred during bus registration."
        }
    }

    fun checkBusRouteExists(busrouteId: String,callback:(Boolean, String)-> Unit){
        repository.checkBusRouteExists(busrouteId,callback)
    }

    fun getBusByRouteId(
        routeId: String,
        callback: (BusModel?) -> Unit
    ) {
        repository.getBusByRouteId(routeId, callback)
    }

    fun observeBusByRoute(routeId: String) {
        viewModelScope.launch {
            repository.getBusStreamByRouteId(routeId).collect { bus ->
                _busStatus.value = bus
            }
        }
    }



}
