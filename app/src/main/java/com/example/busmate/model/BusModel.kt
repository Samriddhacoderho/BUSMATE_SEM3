package com.example.busmate.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BusModel(
    val busNumber: String = "", // Internal Bus ID (e.g., "Bus 15")
    val licensePlate: String = "",

    // 2. CORE ASSIGNMENTS
    val schoolId: String = "",
    val routeId: String = "",
    val driverId: String = "",
    val capacity: Int = 0,
    val maintenanceStatus: String = "Good",
    val driverName: String = "",
    val currentLocation: String = "Depot",
    val speed: Double = 0.0,

    ) : Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "busNumber" to busNumber,
            "licensePlate" to licensePlate,
            "schoolId" to schoolId,
            "routeId" to routeId,
            "driverId" to driverId,
            "capacity" to capacity,
            "maintenanceStatus" to maintenanceStatus,
            "driverName" to driverName,
            "currentLocation" to currentLocation,
            "speed" to speed,
        )
    }
}