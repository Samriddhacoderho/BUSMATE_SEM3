package com.example.busmate.view.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.busmate.R
import com.example.busmate.data.AdminActionsImpl
import com.example.busmate.data.UserRepositoryImpl
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

    val viewModel = remember {
        AdminActionsViewModel(AdminActionsImpl(), UserRepositoryImpl())
    }

    val buses = remember { mutableStateListOf<BusModel>() }

    LaunchedEffect(Unit) {
        viewModel.getAllBus { success, list ->
            if (success && list != null) buses.addAll(list)
        }
    }

    BusProfileScreenUI(buses)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusProfileScreenUI(buses: List<BusModel>) {

    val context = LocalContext.current
    val activity = context as? Activity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bus Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BusMateBlue
                )
            )
        }
    ) { padding ->

        if (buses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BusMateBlue)
            }
            return@Scaffold
        }

        val pagerState = rememberPagerState(pageCount = { buses.size })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Text(
                text = "Swipe to view buses (${pagerState.currentPage + 1}/${buses.size})",
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .align(Alignment.CenterHorizontally),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                SingleBusProfile(bus = buses[page])
            }
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
                else "Not Assigned"
            } ?: "Not Assigned"
        )
    }

    val viewModel = remember {
        AdminActionsViewModel(AdminActionsImpl(), UserRepositoryImpl())
    }

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
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BusMateBlue,
                            BusMateBlue.copy(alpha = 0.85f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Bus ${bus.busNumber}",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Route ${bus.routeId}",
                    color = Color.White.copy(0.9f),
                    fontSize = 15.sp
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-60).dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (bus.busImage.isNotEmpty()) {
                        AsyncImage(
                            model = bus.busImage,
                            contentDescription = "Bus Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.schoolbus),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(90.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Bus Details",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = BusMateBlue
                )

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                BusProfileItem(Icons.Default.Badge, "Bus Number: ${bus.busNumber}")
                BusProfileItem(Icons.Default.People, "Capacity: ${bus.capacity}")

                BusProfileItem(
                    icon = Icons.Default.Person,
                    text = "Driver: $currentDriver",
                    clickable = true,
                    trailing = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                ) {
                    launcher.launch(
                        Intent(context, DriverProfileScreen::class.java)
                            .putExtra("select_mode", true)
                    )
                }

                BusProfileItem(Icons.Default.Info, "Maintenance: ${bus.maintenanceStatus}")
                BusProfileItem(Icons.Default.Route, "Route: ${bus.routeId}")

                val displaySpeed = liveReading?.speedMps ?: bus.speed.toFloat()

                BusProfileItem(
                    icon = Icons.Default.Speed,
                    text = "Speed",
                    trailing = {
                        Surface(
                            color = BusMateBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "${"%.1f".format(displaySpeed)} km/h",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                color = BusMateBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun BusProfileItem(
    icon: ImageVector,
    text: String,
    clickable: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(if (clickable) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BusMateBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = BusMateBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        trailing?.invoke()
    }
}
