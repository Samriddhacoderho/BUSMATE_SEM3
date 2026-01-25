package com.example.busmate.data

import com.example.busmate.model.ChildModel
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatRepository {

    // Initialize using the Firebase AI SDK as requested.
    // NOTE: For the Developer API (googleAI), if it doesn't pick up the key automatically,
    // you may need to pass it like: GenerativeBackend.googleAI(apiKey = "YOUR_KEY")
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")
    // using 1.5-flash as 2.5 is not fully public/stable in all regions yet,
    // but you can swap this string to "gemini-2.5-flash" if you have access.

    private val db = FirebaseDatabase.getInstance()

    suspend fun generateResponse(userQuestion: String, children: List<ChildModel>): String {
        try {
            // 1. Fetch Real-time Data (Context)
            val contextData = getParentContext(children)

            // 2. Create the Prompt
            val prompt = """
                You are BusMate AI. Use the following real-time data to answer the parent's question.
                
                DATA CONTEXT:
                $contextData
                
                USER QUESTION: "$userQuestion"
                
                INSTRUCTIONS:
                - Answer based ONLY on the Data Context.
                - If "isTripRunning" is false, say the bus has not started.
                - Be concise and polite.
            """.trimIndent()

            // 3. Generate Content
            val response = model.generateContent(prompt)
            return response.text ?: "I could not generate a response."

        } catch (e: Exception) {
            return "Error: ${e.localizedMessage}. (Make sure your API Key is valid)."
        }
    }

    private suspend fun getParentContext(children: List<ChildModel>): String {
        val sb = StringBuilder()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        sb.append("Date: $today\n")

        for (child in children) {
            sb.append("\nStudent: ${child.firstName} ${child.lastName}\n")

            // A. Check Attendance
            try {
                val attRef = db.getReference("attendance").child(today)
                    .child(child.busRouteId).child(child.studentId)
                val attSnap = attRef.get().await()

                if (attSnap.exists()) {
                    val status = attSnap.child("status").getValue(String::class.java)
                    val time = attSnap.child("timestamp").getValue(Long::class.java)
                    sb.append(" - Attendance: $status at $time\n")
                } else {
                    sb.append(" - Attendance: Not marked yet.\n")
                }
            } catch (e: Exception) { sb.append(" - Attendance info unavailable.\n") }

            // B. Check Bus Status
            if (child.busRouteId.isNotEmpty()) {
                try {
                    val busSnap = db.getReference("buses").child(child.busRouteId).get().await()
                    if (busSnap.exists()) {
                        val isRunning = busSnap.child("isTripRunning").getValue(Boolean::class.java) ?: false
                        val location = busSnap.child("currentLocation").getValue(String::class.java) ?: "Unknown"
                        sb.append(" - Bus Status: Trip Running = $isRunning. Current Loc: $location\n")
                    }
                } catch (e: Exception) { sb.append(" - Bus info unavailable.\n") }
            }
        }
        return sb.toString()
    }
}

// Data model for the chat list
data class ChatMessage(val text: String, val isUser: Boolean)