package com.example.busmate.view.driver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.data.LocationImpl
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.BusMateOrange
import com.example.busmate.viewmodel.AccelerometerViewModel
import com.example.busmate.viewmodel.LocationViewModel

class TripActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notifications are required for trip alerts", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        askNotificationPermission()

        val driverUid = intent.getStringExtra("EXTRA_DRIVER_UID") ?: ""
        val busId = intent.getStringExtra("EXTRA_BUS_ID") ?: ""

        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                TripScreen(driverUid = driverUid, busId = busId)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun TripScreen(
    driverUid: String,
    busId: String,
    accelerometerViewModel: AccelerometerViewModel = viewModel()
) {
    val state by accelerometerViewModel.state
    val context = LocalContext.current
    var selectedTripType by remember { mutableStateOf("Pickup") } // Global choice

    val locationViewModel = remember {
        LocationViewModel(
            repo = LocationImpl(context),
            busRepo = BusRepositoryImpl()
        )
    }

    // Permission Launcher for GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // ✅ Pass the selected trip type
            accelerometerViewModel.startMeasurement(
                driverUid = driverUid,
                busRouteId = busId,
                tripType = selectedTripType
            )
        } else {
            Toast.makeText(context, "GPS Permission is required to track speed.", Toast.LENGTH_LONG)
                .show()
        }
    }

    LaunchedEffect(state.isRunning) {
        if (state.isRunning) {
            locationViewModel.startTracking(busId = busId, driverUid = driverUid)
        } else {
            locationViewModel.stopLocationUpdates()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (state.isRunning) "TRIP ACTIVE" else "READY TO START",
            fontWeight = FontWeight.Bold,
            color = if (state.isRunning) Color(0xFF4CAF50) else Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Large Speed Display (Now shows REAL GPS speed)
        Text(
            text = "%.1f".format(state.speedMps * 3.6),
            fontSize = 100.sp,
            fontWeight = FontWeight.Black
        )
        Text(text = "KM/H", fontSize = 24.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(60.dp))

        // Trip Type Toggle (Only enabled when trip is not running)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Type: ", fontWeight = FontWeight.Bold)
            FilterChip(
                selected = selectedTripType == "Pickup",
                onClick = {
                    if (!state.isRunning) {
                        selectedTripType = "Pickup"
                    }
                },
                label = { Text("Pickup") },
                enabled = !state.isRunning
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = selectedTripType == "Drop-off",
                onClick = {
                    if (!state.isRunning) {
                        selectedTripType = "Drop-off"
                    }
                },
                label = { Text("Drop-off") },
                enabled = !state.isRunning
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (state.isRunning) {
                    // Stop Trip
                    accelerometerViewModel.stopMeasurement(busId)
                } else {
                    // Start Trip - Check permission first
                    val permissionCheck = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        // ✅ Pass the selected trip type to ViewModel
                        accelerometerViewModel.startMeasurement(
                            driverUid = driverUid,
                            busRouteId = busId,
                            tripType = selectedTripType
                        )
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isRunning) BusMateOrange else BusMateBlue
            )
        ) {
            Text(
                text = if (state.isRunning) "STOP TRIP" else "START TRIP",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}