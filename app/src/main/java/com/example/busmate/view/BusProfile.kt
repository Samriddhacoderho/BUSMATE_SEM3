package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.R
import com.example.busmate.data.AdminActionsImpl
import com.example.busmate.model.BusModel
import com.example.busmate.ui.theme.BusMateBlue
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

    val busesState = remember { mutableStateOf<List<BusModel>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.getAllBus { success, buses ->
            if (success && buses != null) {
                busesState.value = buses
            }
        }
    }

    BusProfileScreenUI(busesState.value)
}

@Composable
fun BusProfileScreenUI(buses: List<BusModel>) {

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔵 Blue Top Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(BusMateBlue)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(id = R.drawable.schoolbus),
                    contentDescription = "Bus",
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "ABOUT THIS BUS",
                    color = BusMateBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                BusProfileItem(Icons.Default.Badge, "Bus Number: ${bus.busNumber}")
                BusProfileItem(Icons.Default.People, "Capacity: ${bus.capacity}")
                BusProfileItem(Icons.Default.Person, "Driver: ${bus.driver ?: "Not Assigned"}")
                BusProfileItem(Icons.Default.Info, "Maintenance: ${bus.maintenanceStatus}")
                BusProfileItem(Icons.Default.Route, "Route: ${bus.routeId}")
                BusProfileItem(Icons.Default.Speed, "Speed: ${bus.speed} km/h")

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun BusProfileItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
