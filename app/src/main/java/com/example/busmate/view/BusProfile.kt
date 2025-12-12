package com.example.busmate.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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

    // Shared variable for passing driver result back to Composables
    companion object {
        var onDriverSelectedCallback: ((String, String) -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusProfileMainScreen()
        }
    }

    // Handle driver selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            val driverId = data.getStringExtra("driverId") ?: return
            val driverName = data.getStringExtra("driverName") ?: return

            // Call Composable callback
            onDriverSelectedCallback?.invoke(driverId, driverName)
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
    var currentDriver by remember {
        mutableStateOf(
            bus.driver?.let {
                if (it.firstName.isNotBlank() || it.lastName.isNotBlank())
                    "${it.firstName} ${it.lastName}"
                else
                    "Not Assigned"
            } ?: "Not Assigned"
        )
    }
    val viewModel = remember { AdminActionsViewModel(AdminActionsImpl()) }

    // Register callback for selection result
    BusProfileScreen.onDriverSelectedCallback = { driverId, driverName ->
        currentDriver = driverName

        viewModel.assignBusToDriver(bus.uid,driverId){success,message->
            if (success){
                Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
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
                .height(260.dp)
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
                .offset(y = (-120).dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(R.drawable.schoolbus),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(140.dp)
                )

                Spacer(Modifier.height(24.dp))

                BusProfileItem(Icons.Default.Badge, "Bus Number: ${bus.busNumber}")
                BusProfileItem(Icons.Default.People, "Capacity: ${bus.capacity}")

                // 🔥 CLICKABLE DRIVER ROW
                BusProfileItem(
                    icon = Icons.Default.Person,
                    text = "Driver: $currentDriver",
                    clickable = true
                ) {
                    val intent = Intent(context, DriverProfileScreen::class.java)
                    intent.putExtra("select_mode", true)
                    (context as Activity).startActivityForResult(intent, 1001)
                }

                BusProfileItem(Icons.Default.Info, "Maintenance: ${bus.maintenanceStatus}")
                BusProfileItem(Icons.Default.Route, "Route: ${bus.routeId}")
                BusProfileItem(Icons.Default.Speed, "Speed: ${bus.speed} km/h")
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
