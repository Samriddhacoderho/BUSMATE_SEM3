package com.example.busmate.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busmate.R
import com.example.busmate.data.AdminActionsImpl
import com.example.busmate.model.BusModel
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.viewmodel.AccelRecieverViewModel
import com.example.busmate.viewmodel.AdminActionsViewModel

class BusProfileScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusProfileMainScreen()
        }
    }
}

@Composable
fun BusProfileMainScreen() {

    val viewModel = remember { AdminActionsViewModel(AdminActionsImpl()) }
    val buses = remember { mutableStateListOf<BusModel>() }

    LaunchedEffect(Unit) {
        viewModel.getAllBus { success, list ->
            if (success && list != null) buses.addAll(list)
        }
    }



    BusProfileScreenUI(buses)
}

@Composable
fun BusProfileScreenUI(buses: List<BusModel>) {

    Scaffold { padding ->

        if (buses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading buses...", fontSize = 20.sp)
            }
            return@Scaffold
        }

        val pagerState = rememberPagerState(pageCount = { buses.size })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            SingleBusProfile(bus = buses[page])
        }
    }
}

@Composable
fun SingleBusProfile(bus: BusModel) {

    val context = LocalContext.current

    val accelViewModel: AccelRecieverViewModel = viewModel()
    val liveReading by accelViewModel.firebaseReading.observeAsState()
    LaunchedEffect(bus.uid) {
        accelViewModel.startTrackingBus(bus.uid)
    }

    var currentDriver by remember {
        mutableStateOf(
            bus.driver?.let {
                val first = it.firstName.orEmpty()
                val last = it.lastName.orEmpty()
                if (first.isNotBlank() || last.isNotBlank())
                    "$first $last"
                else
                    "Not Assigned"
            } ?: "Not Assigned"
        )
    }

    val viewModel = remember { AdminActionsViewModel(AdminActionsImpl()) }

    // ✅ FIX: Compose-safe activity result launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@rememberLauncherForActivityResult
            val driverId = data.getStringExtra("driverId") ?: return@rememberLauncherForActivityResult
            val driverName = data.getStringExtra("driverName") ?: return@rememberLauncherForActivityResult

            currentDriver = driverName

            viewModel.assignBusToDriver(bus.uid, driverId) { _, message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔵 BLUE AREA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(BusMateBlue),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))


            Text(
                text = "Bus ${bus.busNumber}",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Route: ${bus.routeId}",
                color = Color.White.copy(0.8f),
                fontSize = 16.sp
            )
        }

        // 🔳 WHITE CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .offset(y = (-60).dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F3F5)),
                    contentAlignment = Alignment.Center
                ) {
                    if (bus.busImage.isNotEmpty()) {
                        // ✅ Load the actual image registered by the admin
                        coil3.compose.AsyncImage(
                            model = bus.busImage,
                            contentDescription = "Bus Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        // Fallback to logo if no image exists
                        Image(
                            painter = painterResource(id = R.drawable.schoolbus),
                            contentDescription = "Default Bus",
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))

                BusProfileItem(Icons.Default.Badge, "Bus Number: ${bus.busNumber}")
                BusProfileItem(Icons.Default.People, "Capacity: ${bus.capacity}")

                // 🔥 CLICKABLE DRIVER ROW (FIXED)
                BusProfileItem(
                    icon = Icons.Default.Person,
                    text = "Driver: $currentDriver",
                    clickable = true
                ) {
                    launcher.launch(
                        Intent(context, DriverProfileScreen::class.java)
                            .putExtra("select_mode", true)
                    )
                }

                BusProfileItem(Icons.Default.Info, "Maintenance: ${bus.maintenanceStatus}")
                BusProfileItem(Icons.Default.Route, "Route: ${bus.routeId}")
//               BusProfileItem(Icons.Default.Speed, "Speed: ${bus.speed} km/h")
                val displaySpeed = liveReading?.speedMps ?: bus.speed.toFloat()
                BusProfileItem(
                    icon = Icons.Default.Speed,
                    text = "Speed: ${"%.1f".format(displaySpeed)} km/h"
                )
            }
        }
    }
}

@Composable
fun BusProfileItem(
    icon: ImageVector,
    text: String,
    clickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .then(
                if (clickable) Modifier.clickable { onClick() }
                else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (clickable) BusMateBlue else Color.Gray,
            modifier = Modifier.size(22.dp)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text,
            fontSize = 16.sp,
            color = if (clickable) BusMateBlue else Color.Black,
            fontWeight = if (clickable) FontWeight.Bold else FontWeight.Normal
        )
    }
}

