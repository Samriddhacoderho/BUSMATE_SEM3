package com.example.busmate.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmergencyModel(
    val id: String = "",
    val driverName: String = "",
    val busId: String = "", // Matches the Parent's Child busRouteId
    val message: String = "",
    val timestamp: Long = 0
) : Parcelable{
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "driverName" to driverName,
            "busId" to busId,
            "message" to message,
            "timestamp" to timestamp
        )
    }
}