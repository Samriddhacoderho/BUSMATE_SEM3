package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.view.ui.theme.BUSMATETheme
import com.example.busmate.R
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.BusMateYellow


// Placeholder data for the bus profile
data class BusProfileData(
    val busName: String = "School Bus ABC",
    val model: String = "Vehicle Model Name",
    val year: String = "2020",
    val licensePlate: String = "Ba 4 Pa 9988",
    val capacity: Int = 45,
    val fuelType: String = "Diesel",
    val maintenanceStatus: String = "Good (Last Service: Jan 2025)",
    val averageSpeed: String = "35 km/h",

    val driverName: String = "Ram Bahadur Thapa",
    val assignedRoute: String = "Route 03 - Budhanilkantha",
)

class BusProfileScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BUSMATETheme {
                BusProfileScreenUI()
            }
        }
    }
}

@Composable
fun BusProfileScreenUI(modifier: Modifier = Modifier) {
    val profile = remember { BusProfileData() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { innerPadding ->
            // Main screen content (Blue background and White Card)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top Blue Background Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(BusMateBlue)
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    // Back button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.clickable { /* UX: navigate back */ }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }


                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 32.dp)
                    ) {
                        Text(
                            text = profile.busName,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {


                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${profile.model})",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // White Profile Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-150).dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Bus Photo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f) // Wider for bus image
                                .height(150.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.LightGray)
                                .border(4.dp, Color.White, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {

                            Image(
                                painter = painterResource(id = R.drawable.schoolbus),
                                contentDescription = "Bus Photo",

                                )
                        }

                        Spacer(modifier = Modifier.height(24.dp))


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "ABOUT BUS",
                                color = BusMateBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Divider(color = Color.LightGray, thickness = 1.dp)


                        // Bus Name
                        Text(
                            text = profile.busName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(top=16.dp,bottom = 4.dp)
                        )

                        Text(
                            text = "${profile.model} (${profile.year})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .background(
                                    color = BusMateYellow,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "License Plate: ${profile.licensePlate}",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bus-specific items
                        BusProfileItem(icon = Icons.Default.People, text = "Capacity: ${profile.capacity} Passengers")
                        BusProfileItem(icon = Icons.Default.LocalGasStation, text = "Fuel Type: ${profile.fuelType}")
                        BusProfileItem(icon = Icons.Default.Construction, text = "Maint. Status: ${profile.maintenanceStatus}")
                        BusProfileItem(icon = Icons.Default.Speed, text = "Avg. Speed: ${profile.averageSpeed}")
                        // Displaying Driver Name instead of Last Checkup
                        BusProfileItem(icon = Icons.Default.Person, text = "Driver: ${profile.driverName}")
                        BusProfileItem(icon = Icons.Default.Route, text = "Assigned Route: ${profile.assignedRoute}")


                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    )
}

@Composable
fun BusProfileItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.DarkGray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

// Preview Composable
@Preview(showBackground = true)
@Composable
fun BusProfileScreenPreview() {
    BUSMATETheme {
        BusProfileScreenUI()
    }
}