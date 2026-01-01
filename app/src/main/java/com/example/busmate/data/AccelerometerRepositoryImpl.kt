package com.example.busmate.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.busmate.model.AccelerometerModel
import com.example.busmate.util.NotificationHelper
import com.google.firebase.database.*
import kotlin.math.sqrt

class AccelerometerRepositoryImpl(private val context: Context) : AccelerometerRepository, SensorEventListener {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastSpeedAlertTime = 0L
    private val ALERT_COOLDOWN = 10000L // 10 seconds cooldown between alerts

    // Variables for Driver Mode (Sending)
    private var activeBusUid: String? = null
    private var isSensorRegistered = false
    private var lastUploadTime = 0L

    // Variables for Receiver Mode (Fetching)
    private var firebaseSpeedListener: ValueEventListener? = null

    private val _currentSpeedMps = MutableLiveData(0f)
    override val currentSpeedMps: LiveData<Float> = _currentSpeedMps

    private val _firebaseData = MutableLiveData<AccelerometerModel>()
    override val firebaseData: LiveData<AccelerometerModel> = _firebaseData

    private var gravity = floatArrayOf(0f, 0f, 0f)
    private val ALPHA = 0.8f
    private val SHAKE_THRESHOLD = 0.5f
    private val SCALING_FACTOR = 1.5f

    /* ---------- DRIVER LOGIC (SENSING & SENDING) ---------- */

    override fun startListening(driverUid: String) {
        database.getReference("buses")
            .orderByChild("driver/uid")
            .equalTo(driverUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val busNode = snapshot.children.first()
                        activeBusUid = busNode.key
                        registerSensors()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("AccelerometerRepo", "Query failed: ${error.message}")
                }
            })
    }

    override fun registerSensors() {
        if (accelerometer == null || isSensorRegistered) return
        gravity = floatArrayOf(0f, 0f, 0f)
        lastUploadTime = System.currentTimeMillis()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        isSensorRegistered = true
    }

    override fun stopListening() {
        if (isSensorRegistered) {
            sensorManager.unregisterListener(this)
            isSensorRegistered = false
        }
        sendDataToFirebase(0f, isFinal = true)
        activeBusUid = null
        _currentSpeedMps.postValue(0f)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]

        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * ax
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * ay
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * az

        val movementMagnitude = sqrt(
            Math.pow((ax - gravity[0]).toDouble(), 2.0) +
                    Math.pow((ay - gravity[1]).toDouble(), 2.0) +
                    Math.pow((az - gravity[2]).toDouble(), 2.0)
        ).toFloat()




        // Apply smoother scaling and cap the max "simulated" speed
        var finalValue = if (movementMagnitude < SHAKE_THRESHOLD) 0f else movementMagnitude * SCALING_FACTOR
        if (finalValue > 25f) finalValue = 25f // Cap at ~90km/h for safety

        _currentSpeedMps.postValue(finalValue)
        sendDataToFirebase(finalValue)


    }

    private fun sendDataToFirebase(value: Float, isFinal: Boolean = false) {
        val busId = activeBusUid ?: return
        val currentTime = System.currentTimeMillis()

        if (isFinal || currentTime - lastUploadTime >= 500L) {
            database.getReference("buses").child(busId).child("speed")
                .setValue(value.toDouble())
            if (!isFinal) lastUploadTime = currentTime
        }
    }

    /* ---------- NOTIFICATION TRIGGER LOGIC ---------- */

    override fun updateTripRunning(busId: String, isRunning: Boolean) {
        database.getReference("buses").child(busId).child("isTripRunning").setValue(isRunning)

        database.getReference("buses").child(busId).child("busNumber")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val busNo = snapshot.getValue(String::class.java) ?: "Unknown"
                    val statusText = if (isRunning) "Started" else "Ended"

                    val parentData = mapOf(
                        "title" to "Trip $statusText",
                        "message" to "Your child's bus ($busNo) has $statusText its journey.",
                        "timestamp" to ServerValue.TIMESTAMP
                    )

                    val adminData = mapOf(
                        "title" to "Fleet Update",
                        "message" to "Route $busNo has $statusText a trip.",
                        "timestamp" to ServerValue.TIMESTAMP
                    )

                    sendNotificationToAssociatedUsers(busId, parentData, adminData)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }



    override fun sendNotificationToAssociatedUsers(
        busId: String,
        parentData: Map<String, Any>,
        adminData: Map<String, Any>
    ) {
        // 1. Notify Admin (Works because 'admin' is a fixed path)
        database.getReference("notifications").child("admin").push().setValue(adminData)

        // 2. Notify Parents (Using the logic found in BusDetailsActivity)
        database.getReference("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Loop through all users (just like you did to find driver/parent details)
                for (userSnapshot in snapshot.children) {
                    val parentUid = userSnapshot.key ?: continue
                    val childrenNode = userSnapshot.child("children")

                    var isThisParentInvolved = false

                    // Loop through children under this specific user
                    for (childSnapshot in childrenNode.children) {
                        val childBusRouteId = childSnapshot.child("busRouteId").getValue(String::class.java)

                        // If this child belongs to the bus that triggered the alert
                        if (childBusRouteId == busId) {
                            isThisParentInvolved = true
                            break
                        }
                    }

                    // If a match was found, send notification to notifications/{parentUid}
                    if (isThisParentInvolved) {
                        database.getReference("notifications")
                            .child(parentUid)
                            .push()
                            .setValue(parentData)
                            .addOnSuccessListener {
                                Log.d("Notification", "Successfully sent to parent: $parentUid")
                            }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Notification", "Database error: ${error.message}")
            }
        })
    }
    /* ---------- RECEIVER LOGIC (FETCHING FOR BUS PROFILE) ---------- */

    override fun startSyncingFromFirebase(busUid: String) {
        stopSyncingFromFirebase()
        val speedRef = database.getReference("buses").child(busUid).child("speed")

        firebaseSpeedListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val speedValue = snapshot.getValue(Double::class.java) ?: 0.0
                _firebaseData.postValue(AccelerometerModel(
                    speedMps = speedValue.toFloat(),
                    isRunning = speedValue > 0.1
                ))
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("AccelerometerRepo", "Fetch error: ${error.message}")
            }
        }
        speedRef.addValueEventListener(firebaseSpeedListener!!)
    }

    override fun stopSyncingFromFirebase() {
        firebaseSpeedListener?.let { listener ->
            database.getReference("buses").removeEventListener(listener)
            firebaseSpeedListener = null
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    // In AccelerometerRepositoryImpl.kt
    override fun checkSpeedAlert(speedMps: Float, busId: String) {
        val speedKmh = speedMps * 3.6
        val currentTime = System.currentTimeMillis()

        if (speedKmh > 50 && (currentTime - lastSpeedAlertTime > ALERT_COOLDOWN)) {
            lastSpeedAlertTime = currentTime

            // 1. Trigger local notification for Driver
            NotificationHelper.showNotification(
                context,
                "Speed Warning!",
                "You are driving at ${speedKmh.toInt()} km/h. Please slow down."
            )

            // 2. Send notification to Admin via Firebase
            sendSpeedAlertToAdmin(busId, speedKmh.toInt())
        }
    }

    override fun sendSpeedAlertToAdmin(busId: String, speed: Int) {
        database.getReference("buses").child(busId).get().addOnSuccessListener { snapshot ->
            val driverName = snapshot.child("driverName").getValue(String::class.java) ?: "Driver"

            val adminNotification = mapOf(
                "title" to "Speed Violation",
                "message" to "$driverName is driving at $speed km/h",
                "timestamp" to ServerValue.TIMESTAMP,
                "type" to "speed_warning" // Use a type to distinguish it
            )

            // KEEP IT CLEAN: Use the existing notifications/admin node
            database.getReference("notifications").child("admin").push().setValue(adminNotification)
        }
    }
}