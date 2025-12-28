package com.example.busmate.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

    // Register the permission launcher for Android 13+ FCM requirements
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

        // FCM Requirement: Request Notification Permission for Android 13+
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

    val locationViewModel = remember {
        LocationViewModel(
            repo = LocationImpl(context),
            busRepo = BusRepositoryImpl()
        )
    }

    // Effect to handle tracking based on trip status
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

        // Large Speed Display
        Text(
            text = "%.1f".format(state.speedMps * 3.6), // Convert MPS to KM/H
            fontSize = 100.sp,
            fontWeight = FontWeight.Black
        )
        Text(text = "KM/H", fontSize = 24.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(60.dp))

        Button(
            onClick = {
                if (state.isRunning) {
                    // Trigger: Updates 'isTripRunning' to false (Parent app hears this)
                    accelerometerViewModel.stopMeasurement(busId)
                } else {
                    // Trigger: Updates 'isTripRunning' to true (Parent app hears this)
                    accelerometerViewModel.startMeasurement(driverUid, busId)
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