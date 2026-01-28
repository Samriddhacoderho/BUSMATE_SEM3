package com.example.busmate.data

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.example.busmate.model.ChildModel
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// We need Context now to convert Coordinates -> Address
class ChatRepository(private val context: Context) {

    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash") // Flash is faster for real-time chat

    private val db = FirebaseDatabase.getInstance()

    suspend fun generateResponse(userQuestion: String, children: List<ChildModel>): String {
        try {
            // 1. Fetch Real-time Data (Context)
            val contextData = getParentContext(children)

            // 2. Create the Prompt
            // We give the AI a "Persona" and strict rules based on the data we fetched.
            val prompt = """
                You are BusMate Assistant, a helpful AI for parents.
                
                REAL-TIME DATA:
                $contextData
                
                USER QUESTION: "$userQuestion"
                
                INSTRUCTIONS:
                1. Answer the user's question accurately using ONLY the REAL-TIME DATA above.
                2. IF the data says "Trip Status: NOT STARTED", you MUST explicitly say the bus has not started yet. Do not guess a location.
                3. IF the data says "Trip Status: RUNNING", tell them the "Current Location" address provided in the data.
                4. Always mention the child's name and their specific attendance status if asked.
                5. Keep the response friendly, reassuring, and concise.
            """.trimIndent()

            // 3. Generate Content
            val response = model.generateContent(prompt)
            return response.text ?: "I'm having trouble connecting to the AI right now."

        } catch (e: Exception) {
            return "I encountered an error checking the status: ${e.localizedMessage}"
        }
    }

    private suspend fun getParentContext(children: List<ChildModel>): String {
        val sb = StringBuilder()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        sb.append("Today's Date: $today\n")

        for (child in children) {
            sb.append("\n----------------\n")
            sb.append("Child: ${child.firstName} ${child.lastName}\n")

            // --- A. Check Attendance ---
            // Matches JSON: attendance -> DATE -> RouteID -> StudentID
            try {
                val attRef = db.getReference("attendance").child(today)
                    .child(child.busRouteId).child(child.studentId)
                val attSnap = attRef.get().await()

                if (attSnap.exists()) {
                    val status = attSnap.child("status").getValue(String::class.java)
                    // Convert timestamp to readable time
                    val ts = attSnap.child("timestamp").getValue(Long::class.java) ?: 0L
                    val timeString = if (ts > 0) SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(ts)) else ""

                    sb.append("Attendance: $status (Marked at $timeString)\n")
                } else {
                    sb.append("Attendance: Not marked yet (Child may not have boarded).\n")
                }
            } catch (e: Exception) {
                sb.append("Attendance: Info unavailable.\n")
            }

            // --- B. Check Bus Status & Location ---
            // JSON Logic Fix: buses are stored by UUID, but contain "routeId".
            // We must query for the bus where "routeId" == child.busRouteId
            if (child.busRouteId.isNotEmpty()) {
                try {
                    val busQuery = db.getReference("buses")
                        .orderByChild("routeId")
                        .equalTo(child.busRouteId)

                    val querySnap = busQuery.get().await()

                    if (querySnap.exists()) {
                        // The query might return multiple (should be 1), take the first match
                        val busSnap = querySnap.children.first()

                        val isRunning = busSnap.child("isTripRunning").getValue(Boolean::class.java) ?: false

                        if (isRunning) {
                            sb.append("Trip Status: RUNNING\n")

                            // Get Coords and Convert to Address
                            val locationStr = busSnap.child("currentLocation").getValue(String::class.java) ?: ""
                            val address = convertCoordsToAddress(locationStr)
                            sb.append("Current Location: $address\n")

                        } else {
                            sb.append("Trip Status: NOT STARTED\n")
                            sb.append("Current Location: Bus is at the garage/school (Not moving).\n")
                        }
                    } else {
                        sb.append("Bus Info: No bus found for route ${child.busRouteId}.\n")
                    }
                } catch (e: Exception) {
                    sb.append("Bus Status: Unavailable at the moment.\n")
                }
            }
        }
        return sb.toString()
    }

    // Helper: Converts "27.72,85.33" -> "Lazimpat, Kathmandu"
    private fun convertCoordsToAddress(latLngStr: String): String {
        if (latLngStr.isEmpty() || !latLngStr.contains(",")) return "Unknown Location"

        try {
            val parts = latLngStr.split(",")
            val lat = parts[0].trim().toDouble()
            val lng = parts[1].trim().toDouble()

            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)

            return if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                // Try to get a familiar name (feature name) or street, otherwise locality
                val street = addr.thoroughfare ?: addr.featureName
                val area = addr.subLocality ?: addr.locality
                "$street, $area"
            } else {
                "Coordinates: $latLngStr"
            }
        } catch (e: Exception) {
            return "Unknown Location (Map unavailable)"
        }
    }
}