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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.view.ui.theme.BUSMATETheme
import com.example.busmate.R
import com.example.busmate.ui.theme.BusMateYellow

data class DriverProfileData(
    val vehicleModel: String = "Bus Model ",
    val driverName: String = "Ram Bahadur Thapa",
    val rating: Double = 4.9,
    val licensePlate: String = "Bus No: Ba 4 Pa 9988",
    val driverLicense: String = "DL8459-0012",
    val joinedDate: String = "Joined May 2023",
    val location: String = "From Banepa",
    val phoneNumber: String = "+977-9845456765",
    val email: String = "driver@schoolname.com",
    val currentRoute: String = "Budhanilkantha"
)

class DriverProfileScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BUSMATETheme {
                DriverProfileScreenUI()
            }
        }
    }
}

@Composable
fun DriverProfileScreenUI(modifier: Modifier = Modifier) {
    val profile = remember { DriverProfileData() }

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

                    // Driver Info
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 32.dp)
                    ) {
                        Text(
                            text = profile.driverName,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = BusMateYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = profile.rating.toString(),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${profile.vehicleModel})",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 2. White Profile Card
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
                            .padding(top = 56.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Driver Avatar
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .shadow(8.dp, CircleShape) // Shadow for depth effect
                                .clip(CircleShape)
                                .background(Color.White) // Added white background here
                                .border(4.dp, Color.White, CircleShape), // White border to frame image
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.driver),
                                contentDescription = "Driver Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))



                        Spacer(modifier = Modifier.height(24.dp))


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "ABOUT DRIVER",
                                color = BusMateBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Divider(color = Color.LightGray, thickness = 1.dp)



                        Text(
                            text = profile.driverName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(top=16.dp,bottom = 4.dp)
                        )


                        Text(
                            text = profile.vehicleModel,
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
                                text = profile.licensePlate,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Existing items
                        ProfileItem(icon = Icons.Default.CalendarMonth, text = profile.joinedDate)
                        ProfileItem(icon = Icons.Default.LocationOn, text = profile.location)
                        // Driver License Number
                        ProfileItem(icon = Icons.Default.Badge, text = "License: ${profile.driverLicense}")

                        // Other contact items
                        ProfileItem(icon = Icons.Default.Phone, text = "Phone Number: ${profile.phoneNumber}")
                        ProfileItem(icon = Icons.Default.Email, text = "Email: ${profile.email}")
                        ProfileItem(icon = Icons.Default.ShareLocation, text = "Current Route: ${profile.currentRoute}")


                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    )
}

@Composable
fun ProfileItem(icon: ImageVector, text: String) {
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
fun DriverProfileScreenPreview() {

    // BUSMATETheme {
    DriverProfileScreenUI()
    // }
}