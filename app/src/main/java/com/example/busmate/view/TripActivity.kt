package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.BusMateOrange
import com.example.busmate.viewmodel.AccelerometerViewModel

class TripActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Retrieve the Driver UID (Shyam's UID) passed from HomeScreen
        val driverUid = intent.getStringExtra("EXTRA_DRIVER_UID") ?: ""

        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                TripScreen(driverUid = driverUid)
            }
        }
    }
}

@Composable
fun TripScreen(driverUid: String, viewModel: AccelerometerViewModel = viewModel()) {
    val state by viewModel.state

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (state.isRunning) "TRIP ACTIVE" else "READY TO START",
            fontWeight = FontWeight.Bold,
            color = if (state.isRunning) Color(0xFF4CAF50) else Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "%.1f".format(state.speedMps),
            fontSize = 100.sp,
            fontWeight = FontWeight.Black
        )
        Text(text = "KM/H", fontSize = 24.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(60.dp))

        Button(
            onClick = {
                if (state.isRunning) viewModel.stopMeasurement()
                else viewModel.startMeasurement(driverUid)
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
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