package com.example.busmate.service

import android.util.Log
import com.example.busmate.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Triggered when FCM message arrives (Foreground or Data messages)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 1. Get Title and Body from the FCM payload
        val title = remoteMessage.notification?.title ?: "Bus Update"
        val message = remoteMessage.notification?.body ?: ""

        // 2. Call our helper to show it
        if (message.isNotEmpty()) {
            NotificationHelper.showNotification(applicationContext, title, message)
        }
    }

    // Called when a new token is generated (e.g. first app open or token refresh)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_SERVICE", "New token generated: $token")

        // Update the token in the database so we can send notifications to this device
        saveTokenToDatabase(token)
    }

    private fun saveTokenToDatabase(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(userId)

            // Store the token under the user's profile
            userRef.child("fcmToken").setValue(token)
                .addOnSuccessListener {
                    Log.d("FCM_SERVICE", "Token successfully saved to Firebase")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM_SERVICE", "Failed to save token: ${e.message}")
                }
        }
    }
}