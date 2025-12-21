package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.data.LocationImpl
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.BusMateOrange
import com.example.busmate.viewmodel.AccelerometerViewModel
import com.example.busmate.viewmodel.LocationViewModel

class TripActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Retrieve the Driver UID passed from HomeScreen
        val driverUid = intent.getStringExtra("EXTRA_DRIVER_UID") ?: ""

        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                TripScreen(driverUid = driverUid)
            }
        }
    }
}

@Composable
fun TripScreen(driverUid: String, accelerometerViewModel: AccelerometerViewModel = viewModel()) {
    val state by accelerometerViewModel.state
    val context = LocalContext.current

    // Initialize LocationViewModel manually to follow MVVM/Repo pattern without a factory
    val locationViewModel = remember {
        LocationViewModel(
            repo = LocationImpl(context),
            busRepo = BusRepositoryImpl()
        )
    }

    //
    // Effect to start/stop live location tracking based on the trip's running state
    LaunchedEffect(state.isRunning) {
        if (state.isRunning) {
            // This triggers the GPS loop and starts updating the BusModel in Firebase
            locationViewModel.startTracking(driverUid = driverUid)
        } else {
            // Stops GPS and synchronization to save battery
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

        // Live speed from Accelerometer
        Text(
            text = "%.1f".format(state.speedMps),
            fontSize = 100.sp,
            fontWeight = FontWeight.Black
        )
        Text(text = "KM/H", fontSize = 24.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(60.dp))

        Button(
            onClick = {
                if (state.isRunning) {
                    accelerometerViewModel.stopMeasurement()
                } else {
                    accelerometerViewModel.startMeasurement(driverUid)
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