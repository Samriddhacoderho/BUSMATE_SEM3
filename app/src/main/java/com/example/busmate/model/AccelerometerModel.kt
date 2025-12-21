package com.example.busmate.model

data class AccelerometerModel (
    val speedMps: Float = 0f,

    // Field for UI state (must have default value for Firebase)
    val isRunning: Boolean = false,

    // NEW FIELD: Required for Firebase to log the time of the reading
    val timestamp: Long = 0
)