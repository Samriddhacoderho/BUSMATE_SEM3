package com.example.busmate.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.busmate.model.AccelerometerModel
import com.google.firebase.database.*
import kotlin.math.sqrt

class AccelerometerRepositoryImpl(context: Context) : AccelerometerRepository, SensorEventListener {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

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
    private val SCALING_FACTOR = 10.0f


    // DRIVER LOGIC (SENSING & SENDING)

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

    private fun registerSensors() {
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

        val finalValue = if (movementMagnitude < SHAKE_THRESHOLD) 0f else movementMagnitude * SCALING_FACTOR
        _currentSpeedMps.postValue(finalValue)
        sendDataToFirebase(finalValue)
    }

    private fun sendDataToFirebase(value: Float, isFinal: Boolean = false) {
        val busId = activeBusUid ?: return
        val currentTime = System.currentTimeMillis()

        if (isFinal || currentTime - lastUploadTime >= 500L) {
            database.getReference("buses").child(busId).child("speed")
                .setValue(value.toDouble()) // Store as double for precision
            if (!isFinal) lastUploadTime = currentTime
        }
    }


    // RECEIVER LOGIC (FETCHING FOR BUS PROFILE)


    override fun startSyncingFromFirebase(busUid: String) {
        // Stop previous listener if switching buses in HorizontalPager
        stopSyncingFromFirebase()

        val speedRef = database.getReference("buses").child(busUid).child("speed")

        firebaseSpeedListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Ensure data is read as Double (matching how it's sent)
                val speedValue = snapshot.getValue(Double::class.java) ?: 0.0

                // Update the LiveData which AccelRecieverViewModel observes
                _firebaseData.postValue(AccelerometerModel(
                    speedMps = speedValue.toFloat(),
                    isRunning = speedValue > 0.1
                ))
                Log.d("AccelerometerRepo", "Fetched Speed: $speedValue for Bus: $busUid")
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
            Log.d("AccelerometerRepo", "Stopped syncing speed data.")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun updateTripRunning(routeId: String, isRunning: Boolean) {
        database.getReference("buses")
            .child(routeId)
            .child("isTripRunning")
            .setValue(isRunning)
    }

}