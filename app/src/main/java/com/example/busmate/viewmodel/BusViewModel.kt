package com.example.busmate.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.data.BusRepositoryInterface
import com.example.busmate.model.BusModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BusViewModel(
    private val repository: BusRepositoryInterface = BusRepositoryImpl()
) : ViewModel() {

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    private val _buses = MutableStateFlow<List<BusModel>>(emptyList())
    val buses: StateFlow<List<BusModel>> = _buses

    fun registerBus(
        busNumber: String,
        licensePlate: String,
        routeId: String,
        capacity: Int,
        busImage: String
    ) {
        _message.value = "Loading"

        try {
            val bus = BusModel(
                busNumber = busNumber.trim(),
                licensePlate = licensePlate.trim().uppercase(),
                routeId = routeId.trim(),
                capacity = capacity,
                busImage = busImage,
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

    fun getBusByDriverUid(driverUid: String, callback: (BusModel?) -> Unit) {
        repository.getBusByDriverUid(driverUid, callback)
    }
    fun triggerSOS(driverUid: String) {
        repository.triggerSOS(driverUid) { success, msg ->
            _message.value = msg
        }
    }
    fun observeAllBuses() {
        repository.observeAllBuses {
            _buses.value = it
        }
    }
    fun updateBus(bus: BusModel) {
        _message.value = "Saving..."
        repository.updateBus(bus) { _, msg ->
            _message.value = msg
        }
    }
    fun uploadBusImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        repository.uploadBusImage(context, imageUri, callback)
    }
}
//testing