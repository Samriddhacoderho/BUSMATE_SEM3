package com.example.busmate.data

import com.example.busmate.model.BusModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

interface BusRepositoryInterface {

    fun registerBus(
        bus: BusModel,
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

    fun getBusStreamByRouteId(routeId: String): Flow<BusModel?>


}
