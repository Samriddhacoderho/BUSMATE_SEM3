package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busmate.viewmodel.AccelRecieverViewModel
import com.example.busmate.model.AccelerometerModel

class ChildTripDetails : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 1. Get child details and the CRITICAL Bus UID from the intent
            val childName = intent.getStringExtra("CHILD_NAME") ?: "Student"
            val busUid = intent.getStringExtra("BUS_UID") ?: ""

            ChildTripDetailsScreen(childName = childName, busUid = busUid)
        }
    }
}

@Composable
fun ChildTripDetailsScreen(
    childName: String,
    busUid: String,
    viewModel: AccelRecieverViewModel = viewModel()
) {
    // 2. Trigger the sync as soon as the screen opens
    LaunchedEffect(busUid) {
        viewModel.startTrackingBus(busUid)
    }

    // Observe live accelerometer data
    val remoteData by viewModel.firebaseReading.observeAsState(AccelerometerModel())

    val bluePurpleGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF2196F3), Color(0xFF673AB7))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 15.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color(0xFF673AB7),
                    spotColor = Color(0xFF2196F3)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .background(bluePurpleGradient)
                    .padding(24.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Real-time Trip Details",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    TripDetailRow(label = "Student Name", value = childName)

                    // Displays the live speed from Firebase
                    TripDetailRow(
                        label = "Current Speed",
                        value = "${"%.1f".format(remoteData.speedMps)} KM/H"
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f))

                    TripDetailRow(label = "Bus ID", value = busUid.take(8)) // showing part of ID for debug
                    TripDetailRow(label = "Status", value = "Live")

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Last Updated: ${remoteData.timestamp}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Tracking Bus: $busUid",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun TripDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp)
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}