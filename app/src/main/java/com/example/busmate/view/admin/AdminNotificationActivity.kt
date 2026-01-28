package com.example.busmate.view.admin

import android.content.Context
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
import com.example.busmate.ui.theme.isDarkMode
import com.example.busmate.viewmodel.AdminActionsViewModel

class AdminNotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = AdminActionsViewModel(AdminActionsImpl(), UserRepositoryImpl())

        setContent {
            val context = LocalContext.current

            // --- DARK MODE REFRESH LOGIC ---
            val sharedPrefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
            var themeUpdateTrigger by remember { mutableIntStateOf(0) }

            DisposableEffect(Unit) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "dark_mode_pref") {
                        themeUpdateTrigger++
                    }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            key(themeUpdateTrigger) {
                BusMateTheme(darkTheme = isDarkMode()) {
                    AdminNotificationScreen(
                        viewModel = viewModel,
                        onBack = { finish() }
                    )
                }
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
                // UPDATED: Adaptive background
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = busMateBlue
            )
            Text(
                "Broadcast to All Users",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface // UPDATED: Adaptive Text
            )
            Text(
                "Parents and Drivers will see this instantly",
                color = MaterialTheme.colorScheme.onSurfaceVariant, // UPDATED: Adaptive Text
                fontSize = 12.sp
            )

            Spacer(Modifier.height(30.dp))

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Announcement Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                // UPDATED: Input colors for Dark Mode
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = busMateBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = busMateBlue,
                    cursorColor = busMateBlue
                )
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
                shape = RoundedCornerShape(12.dp),
                // UPDATED: Input colors for Dark Mode
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = busMateBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = busMateBlue,
                    cursorColor = busMateBlue
                )
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (title.isBlank() || message.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSending = true

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
                    Text("SEND TO ALL USERS", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}