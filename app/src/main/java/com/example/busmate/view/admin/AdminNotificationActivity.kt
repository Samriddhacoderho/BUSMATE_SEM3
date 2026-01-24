package com.example.busmate.view.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.AdminActionsImpl
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.viewmodel.AdminActionsViewModel
import com.google.firebase.messaging.FirebaseMessaging

class AdminNotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel with necessary repositories
        val viewModel = AdminActionsViewModel(AdminActionsImpl(), UserRepositoryImpl())

        // Optional: Ensure this admin device is also subscribed to the topic for testing
//        FirebaseMessaging.getInstance().subscribeToTopic("all_users")

        setContent {
            BusMateTheme {
                AdminNotificationScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationScreen(viewModel: AdminActionsViewModel, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val busMateBlue = Color(0xFF2567E8)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Announcement", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = busMateBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = busMateBlue
            )
            Text("Broadcast to All Users", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Parents and Drivers will see this instantly", color = Color.Gray, fontSize = 12.sp)

            Spacer(Modifier.height(30.dp))

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Announcement Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Message Input
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (title.isBlank() || message.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSending = true

                    // Use the ViewModel function we created earlier
                    viewModel.sendBroadcast(title, message) { success, resultMessage ->
                        isSending = false
                        if (success) {
                            Toast.makeText(context, "Notification Sent!", Toast.LENGTH_SHORT).show()
                            onBack()
                        } else {
                            Toast.makeText(context, "Failed: $resultMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = busMateBlue),
                enabled = !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("SEND TO ALL USERS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}