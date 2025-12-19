package com.example.busmate.data

import com.example.busmate.model.BusModel
import com.google.android.gms.maps.model.LatLng

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
}
