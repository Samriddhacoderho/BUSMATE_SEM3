package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.busmate.R
import com.example.busmate.data.AdminActionsImpl
import com.example.busmate.model.DriverModel
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.BusMateYellow
import com.example.busmate.view.ui.theme.BUSMATETheme
import com.example.busmate.viewmodel.AdminActionsViewModel
import com.google.firebase.firestore.auth.User

// ------------------------------------------------------------------------------------------------
// MAIN ACTIVITY
// ------------------------------------------------------------------------------------------------

class DriverProfileScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BUSMATETheme {
                DriverProfileMainScreen()
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// FETCH DRIVERS + PAGER
// ------------------------------------------------------------------------------------------------

@Composable
fun DriverProfileMainScreen() {
    val viewModel = remember { AdminActionsViewModel(AdminActionsImpl()) }

    val driversState = remember { mutableStateOf<List<UserModel>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.getAllDrivers { success, drivers ->
            if (success && drivers != null) {
                driversState.value = drivers
            }
        }
    }

    DriverProfileScreenUI(driversState.value)
}

@Composable
fun DriverProfileScreenUI(drivers: List<UserModel>) {

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->

        if (drivers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading drivers...", fontSize = 20.sp)
            }
            return@Scaffold
        }

        val pagerState = rememberPagerState(pageCount = { drivers.size })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) { page ->
            SingleDriverProfile(driver = drivers[page])
        }
    }
}

// ------------------------------------------------------------------------------------------------
// SINGLE DRIVER PAGE (MATCHED WITH BUS STYLE)
// ------------------------------------------------------------------------------------------------

@Composable
fun SingleDriverProfile(driver: UserModel) {

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔵 Top Blue Area (same as Bus UI style)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(BusMateBlue)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Driver: ${driver.firstName} ${driver.lastName}",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "School ID: ${driver.schoolId}",
                color = Color.White.copy(0.8f),
                fontSize = 16.sp
            )
        }

        // 🔳 White Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-130).dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Driver Avatar
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(4.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.driver),
                        contentDescription = "Driver Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "ABOUT DRIVER",
                    color = BusMateBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // ✔ ONLY YOUR REAL FIELDS:
                DriverProfileItem(Icons.Default.Person, "Full Name: ${driver.firstName} ${driver.lastName}")
                DriverProfileItem(Icons.Default.Email, "Email: ${driver.email}")
                DriverProfileItem(Icons.Default.Phone, "Phone: ${driver.phone}")
                DriverProfileItem(Icons.Default.LocationOn, "School ID: ${driver.schoolId}")


                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// REUSABLE ROW ITEM
// ------------------------------------------------------------------------------------------------

@Composable
fun DriverProfileItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

