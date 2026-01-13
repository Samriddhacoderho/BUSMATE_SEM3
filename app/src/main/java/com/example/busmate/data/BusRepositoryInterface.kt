package com.example.busmate.data

import android.content.Context
import android.net.Uri
import com.example.busmate.model.BusModel
import com.google.android.gms.maps.model.LatLng

interface BusRepositoryInterface {

    fun registerBus(
        bus: BusModel, // No change here, but BusModel must have the field
        callback: (String, Boolean) -> Unit
    )

    fun updateLocationByDriver(
        driverUid: String,
        latLng: LatLng
    )
    fun updateBusLocation(
        busUid: String,
        latLng: com.google.android.gms.maps.model.LatLng,
        callback: (Boolean) -> Unit
    )
     fun getLiveBusLocation(busId: String, callback: (String) -> Unit)

    fun checkBusRouteExists(
        busRouteId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getBusByRouteId(
        busRouteId: String,
        callback: (BusModel?) -> Unit
    )

    fun getBusByDriverUid(driverUid: String, callback: (BusModel?) -> Unit)

    fun getAllBusesLive(callback: (List<BusModel?>) -> Unit)

    fun getRoadSnappedRoute(
        origin: LatLng,
        destination: LatLng,
        apiKey: String,
        waypoints: List<LatLng> = emptyList(), // Default value goes here
        onSuccess: (List<LatLng>, Int) -> Unit,
        onFailure: (String) -> Unit
    )
    fun triggerSOS(driverUid: String, callback: (Boolean, String) -> Unit)
    fun uploadBusImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )
    fun observeAllBuses(callback: (List<BusModel>) -> Unit)
    fun updateBus(
        bus: BusModel,
        callback: (Boolean, String) -> Unit
    )

}