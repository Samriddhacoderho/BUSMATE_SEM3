package com.example.busmate.data

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
        onSuccess: (List<LatLng>) -> Unit,
        onFailure: (String) -> Unit
    )


}
