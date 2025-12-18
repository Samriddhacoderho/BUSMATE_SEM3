package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.busmate.model.AccelRecieverModel
import com.example.busmate.model.AccelerometerModel

class ChildTripDetails : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Get child name from intent or use default
            val childName = intent.getStringExtra("CHILD_NAME") ?: "Swikrit"
            ChildTripDetailsScreen(childName = childName)
        }
    }
}

@Composable
fun ChildTripDetailsScreen(
    childName: String,
    viewModel: AccelRecieverModel = viewModel()
) {
    // Observe live accelerometer data
    val remoteData by viewModel.firebaseReading.observeAsState(AccelerometerModel())

    // Modern Blue to Purple Gradient
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
        // Main Information Card
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
                    // Header
                    Text(
                        text = "Real-time Trip Details",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Unified Data Rows
                    TripDetailRow(label = "Student Name", value = childName)

                    // Speed Data (Now same size as other fields)
                    TripDetailRow(
                        label = "Current Speed",
                        value = "${"%.1f".format(remoteData.speedMps)} KM/H"
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f))

                    // Static Placeholder Data
                    TripDetailRow(label = "Bus Plate", value = "BA PAA 1234")
                    TripDetailRow(label = "Driver", value = "Ram Bahadur")

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

        // Text below the card
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Map Card below",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
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
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}