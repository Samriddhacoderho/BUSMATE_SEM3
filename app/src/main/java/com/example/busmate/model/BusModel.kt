package com.example.busmate.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BusModel(
    val uid: String="",
    val busNumber: String = "", // Internal Bus ID (e.g., "Bus 15")
    val licensePlate: String = "",

    val routeId: String = "",
    val capacity: Int = 0,
    val maintenanceStatus: String = "Good",
    val currentLocation: String = "Depot",
    val speed: Double = 0.0,
    val driver:DriverModel? = null,


    ) : Parcelable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "busNumber" to busNumber,
            "licensePlate" to licensePlate,
            "routeId" to routeId,
            "capacity" to capacity,
            "maintenanceStatus" to maintenanceStatus,
            "currentLocation" to currentLocation,
            "speed" to speed,
            "driver" to driver?.toMap() // nullable OK
        )
    }
}