package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.EmergencyRepositoryImpl
import com.example.busmate.model.EmergencyModel
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.viewmodel.EmergencyViewModel
import java.text.SimpleDateFormat
import java.util.*

class EmergencyAlertActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Get the current user model to handle filtering
        val userModel = intent.getParcelableExtra<UserModel>("model")

        // 2. Initialize the architecture layers
        val repository = EmergencyRepositoryImpl()
        val viewModel = EmergencyViewModel(repository)

        setContent {
            BusMateTheme {
                // Observe the real-time SOS alerts from the ViewModel
                val allAlerts by viewModel.alerts.collectAsState()

                // 3. Filter Logic:
                // Admin sees everything.
                // Parent only sees alerts matching their bus route.
                val filteredAlerts = remember(allAlerts) {
                    if (userModel?.typeofUser == "Admin") {
                        allAlerts
                    } else {
                        allAlerts.filter { alert ->
                            userModel?.children?.values?.any { it.busRouteId == alert.busId } ?: false
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text("Emergency Alerts", fontWeight = FontWeight.Bold, color = Color.White)
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color(0xFFD32F2F) // High-visibility Emergency Red
                            )
                        )
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(Color(0xFFF9F9F9))
                    ) {
                        if (filteredAlerts.isEmpty()) {
                            EmptyAlertsView()
                        } else {
                            EmergencyList(filteredAlerts)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyList(alerts: List<EmergencyModel>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(alerts) { alert ->
            EmergencyAlertCard(alert)
        }
    }
}

@Composable
fun EmergencyAlertCard(alert: EmergencyModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "CRITICAL SOS",
                    color = Color.Red,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Text(
                    text = alert.message,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Driver: ${alert.driverName}",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "Bus Route ID: ${alert.busId}",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(alert.timestamp)),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EmptyAlertsView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No active emergency alerts.", color = Color.Gray)
    }
}