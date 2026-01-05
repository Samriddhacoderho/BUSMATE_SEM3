package com.example.busmate.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.busmate.model.AccelerometerModel
import com.example.busmate.util.NotificationHelper
import com.google.android.gms.location.*
import com.google.firebase.database.*

// Note: SensorEventListener is kept in the signature to avoid breaking the interface,
// but we now use FusedLocationProvider for the logic.
class AccelerometerRepositoryImpl(private val context: Context) : AccelerometerRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    // GPS Client
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private var lastSpeedAlertTime = 0L
    private val ALERT_COOLDOWN = 10000L

    // Variables for Driver Mode (Sending)
    private var activeBusUid: String? = null
    private var lastUploadTime = 0L
    private val UPLOAD_INTERVAL_MS = 1000L

    // Variables for Receiver Mode (Fetching)
    private var firebaseSpeedListener: ValueEventListener? = null

    private val _currentSpeedMps = MutableLiveData(0f)
    override val currentSpeedMps: LiveData<Float> = _currentSpeedMps

    private val _firebaseData = MutableLiveData<AccelerometerModel>()
    override val firebaseData: LiveData<AccelerometerModel> = _firebaseData

    // GPS Settings
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
        .setMinUpdateIntervalMillis(500L)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation ?: return

            // GPS provides speed in meters/second.
            val speedMps = location.speed

            _currentSpeedMps.postValue(speedMps)

            // Trigger alerts if speed > 50km/h
            activeBusUid?.let { checkSpeedAlert(speedMps, it) }

            // Sync to Firebase at existing path: buses/busId/speed
            sendDataToFirebase(speedMps)
        }
    }

    /* ---------- DRIVER LOGIC (GPS SENSING & SENDING) ---------- */

    override fun startListening(driverUid: String) {
        // Preserved: Find the bus associated with this driver
        database.getReference("buses")
            .orderByChild("driver/uid")
            .equalTo(driverUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val busNode = snapshot.children.first()
                        activeBusUid = busNode.key
                        registerSensors() // Starts GPS
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("AccelerometerRepo", "Query failed: ${error.message}")
                }
            })
    }

    @SuppressLint("MissingPermission")
    override fun registerSensors() {
        // This now registers GPS updates instead of Accelerometer
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        lastUploadTime = System.currentTimeMillis()
    }

    override fun stopListening() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sendDataToFirebase(0f, isFinal = true)
        activeBusUid = null
        _currentSpeedMps.postValue(0f)
    }

    // --- UPDATED SENDING LOGIC (Driver Side) ---
    private fun sendDataToFirebase(value: Float, isFinal: Boolean = false) {
        val busId = activeBusUid ?: return
        val currentTime = System.currentTimeMillis()

        // Sync to Firebase every 1 second or on stop
        if (isFinal || currentTime - lastUploadTime >= UPLOAD_INTERVAL_MS) {
            val busRef = database.getReference("buses").child(busId)

            // 1. Update the flat 'speed' field (as a Double for compatibility)
            busRef.child("speed").setValue(value.toDouble())

            // 2. Note: 'isTripRunning' is already handled by updateTripRunning()

            if (!isFinal) lastUploadTime = currentTime
        }
    }
    /* ---------- NOTIFICATION TRIGGER LOGIC (UNTOUCHED) ---------- */

    override fun updateTripRunning(busId: String, isRunning: Boolean) {
        database.getReference("buses").child(busId).child("isTripRunning").setValue(isRunning)
        database.getReference("buses").child(busId).child("busNumber")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val busNo = snapshot.getValue(String::class.java) ?: "Unknown"
                    val statusText = if (isRunning) "Started" else "Ended"
                    val adminData = mapOf("title" to "Fleet Update", "message" to "Bus $busNo has $statusText a trip.", "timestamp" to ServerValue.TIMESTAMP, "type" to "trip_status")
                    val parentData = mapOf("title" to "Bus $statusText", "message" to "Bus route $busNo has $statusText its journey.", "timestamp" to ServerValue.TIMESTAMP, "type" to "trip_status")
                    sendNotificationToAssociatedUsers(busId, parentData, adminData)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun sendNotificationToAssociatedUsers(busId: String, parentData: Map<String, Any>, adminData: Map<String, Any>) {
        database.getReference("notifications").child("admin").push().setValue(adminData)
        database.getReference("buses").child(busId).get().addOnSuccessListener { busSnapshot ->
            val busRouteNumber = busSnapshot.child("routeId").getValue(String::class.java)
            database.getReference("users").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        if (userSnapshot.child("typeofUser").getValue(String::class.java) == "Parent") {
                            val parentUid = userSnapshot.key ?: continue
                            val childrenNode = userSnapshot.child("children")
                            var shouldNotify = false
                            for (childSnapshot in childrenNode.children) {
                                val childBusId = childSnapshot.child("busRouteId").getValue(String::class.java)
                                if (childBusId == busRouteNumber || childBusId == busId) { shouldNotify = true; break }
                            }
                            if (shouldNotify) database.getReference("notifications").child(parentUid).push().setValue(parentData)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    /* ---------- RECEIVER LOGIC (FETCHING) ---------- */

    // --- UPDATED RECEIVING LOGIC (Parent/Admin Side) ---
    override fun startSyncingFromFirebase(busUid: String) {
        stopSyncingFromFirebase()
        // Listen to the whole bus node to get both speed and trip status
        val busRef = database.getReference("buses").child(busUid)

        firebaseSpeedListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Read the flat 'speed' field
                val speedValue = snapshot.child("speed").getValue(Double::class.java) ?: 0.0
                // Read the flat 'isTripRunning' field
                val runningStatus = snapshot.child("isTripRunning").getValue(Boolean::class.java) ?: false

                // Map them back to your Model so your UI code doesn't have to change
                _firebaseData.postValue(AccelerometerModel(
                    speedMps = speedValue.toFloat(),
                    isRunning = runningStatus,
                    timestamp = System.currentTimeMillis()
                ))
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("AccelerometerRepo", "Fetch error: ${error.message}")
            }
        }
        busRef.addValueEventListener(firebaseSpeedListener!!)
    }

    override fun stopSyncingFromFirebase() {
        firebaseSpeedListener?.let { listener ->
            // Use specific path to remove listener
            database.getReference("buses").removeEventListener(listener)
            firebaseSpeedListener = null
        }
    }

    /* ---------- SPEED ALERT LOGIC ---------- */

    override fun checkSpeedAlert(speedMps: Float, busId: String) {
        val speedKmh = speedMps * 3.6
        val currentTime = System.currentTimeMillis()
        if (speedKmh > 50 && (currentTime - lastSpeedAlertTime > ALERT_COOLDOWN)) {
            lastSpeedAlertTime = currentTime
            NotificationHelper.showNotification(context, "Speed Warning!", "You are driving at ${speedKmh.toInt()} km/h. Please slow down.")
            sendSpeedAlertToAdmin(busId, speedKmh.toInt())
        }
    }

    override fun sendSpeedAlertToAdmin(busId: String, speed: Int) {
        database.getReference("buses").child(busId).get().addOnSuccessListener { snapshot ->
            val driverName = snapshot.child("driverName").getValue(String::class.java) ?: "Driver"
            val adminNotification = mapOf("title" to "Speed Violation", "message" to "$driverName is driving at $speed km/h", "timestamp" to ServerValue.TIMESTAMP, "type" to "speed_warning")
            database.getReference("notifications").child("admin").push().setValue(adminNotification)
        }
    }
}