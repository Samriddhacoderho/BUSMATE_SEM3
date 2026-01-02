package com.example.busmate.view.dashboard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.busmate.R
import com.example.busmate.model.BusModel
import com.example.busmate.model.UserModel
import com.example.busmate.viewmodel.AccelRecieverViewModel
import com.example.busmate.viewmodel.ChildViewModel
import com.example.busmate.viewmodel.LocationViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// Helper function to create the bus icon
fun createBusMarkerIcon(context: Context): BitmapDescriptor {
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.schoolbus)
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
    return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
}

@Composable
fun LiveLocationScreen(
    viewModel: LocationViewModel,
    childViewModel: ChildViewModel,
    accelViewModel: AccelRecieverViewModel,
    busId: String, // ID passed from Parent click
    selectedChildId: String? = null
) {
    val routeChildren by viewModel.routeChildren.collectAsState()
    val coordinates by viewModel.currentBusCoordinates.collectAsState()
    val childEtas by viewModel.childEtas.collectAsState()
    val children by childViewModel.children.collectAsState()
    val liveReading by accelViewModel.firebaseReading.observeAsState()
    val isTripRunning by viewModel.isTripRunning.collectAsState()
    val allBuses by viewModel.allBuses.collectAsState()

    val context = LocalContext.current
    val activity = context as Activity

    val model by remember {
        mutableStateOf(activity.intent.getParcelableExtra<UserModel>("model"))
    }

    // UPDATED: Logic to separate Driver UID from Parent Route ID
    LaunchedEffect(model, busId) {
        val currentUser = model ?: return@LaunchedEffect

        when (currentUser.typeofUser) {
            "Driver" -> {
                // Driver uses UID to find their Route, which loads kids AND starts map tracking
                viewModel.loadChildrenByDriverId(currentUser.uid)
                accelViewModel.startTrackingBus(currentUser.schoolId)
            }
            "Parent" -> {
                if (busId.isNotEmpty()) {
                    viewModel.loadChildrenForRoute(busId)
                    viewModel.startTracking(busId) // RESTORES THE BUS MARKER
                    accelViewModel.startTrackingBus(busId)
                    currentUser.uid.let { childViewModel.observeChildren(it) }
                }
            }
            "Admin" -> {
                viewModel.trackAllBuses()
            }
        }
    }

    LaunchedEffect(coordinates, liveReading, children) {
        if (model?.typeofUser == "Parent") {
            val filteredChildren = children.filter { it.studentId == selectedChildId }
            viewModel.updateChildEtas(
                children = filteredChildren,
                currentCoords = coordinates,
                rawSpeedMps = liveReading?.speedMps ?: 0f
            )
        }
    }
    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        // Box is used to overlay the Driver's list ON TOP of the Map
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Background: Map and Title
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = if (model?.typeofUser == "Admin") "Fleet Live Overview" else "Live Location Tracking",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                MapPrototype(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    model = model,
                    context = context,
                    coordinates = coordinates,
                    locationViewModel = viewModel,
                    busId = if (model?.typeofUser == "Driver") model?.schoolId ?: "" else busId,
                    allBuses = if (model?.typeofUser == "Admin") allBuses.filterNotNull() else emptyList()
                )

                if (model?.typeofUser == "Parent") {
                    Text("Children Arrival Info", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                        items(childEtas) { etaState ->
                            ETACard(childName = etaState.childName, eta = etaState.etaMinutes, cardColor = MaterialTheme.colorScheme.primaryContainer)
                        }
                    }
                }
            }

            // Foreground: Driver's Student Pickup List
            if (model?.typeofUser == "Driver") {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Route Students", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)

                        // Corrected Material 3 Divider
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 1.dp,
                            color = Color.LightGray.copy(alpha = 0.4f)
                        )

                        if (routeChildren.isEmpty()) {
                            Text("No students found on this route.", color = Color.Gray, modifier = Modifier.padding(20.dp).align(Alignment.CenterHorizontally))
                        }

                        LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                            items(routeChildren) { child ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${child.firstName} ${child.lastName}", fontWeight = FontWeight.Bold)
                                        Text("Stop: ${child.pickUpLocation}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = Color(0xFF2567E8))
                                }
                                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapPrototype(
    modifier: Modifier,
    model: UserModel?,
    context: Context,
    coordinates: String,
    locationViewModel: LocationViewModel,
    busId: String,
    allBuses: List<com.example.busmate.model.BusModel> // Added for Admin
) {
    var permissionGranted by remember {
        mutableStateOf(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        permissionGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(model?.typeofUser) {
        if (!permissionGranted) {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    if (permissionGranted) {
        CardMap(modifier, model, context, coordinates, locationViewModel, busId, allBuses)
    } else {
        Box(modifier = modifier.fillMaxWidth().height(260.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Text("Location permission required")
        }
    }
}

@Composable
fun CardMap(
    modifier: Modifier,
    model: UserModel?,
    context: Context,
    coordinates: String, // This is the flow from currentBusCoordinates
    locationViewModel: LocationViewModel,
    busId: String,
    allBuses: List<com.example.busmate.model.BusModel>
) {
    val currentLocation by locationViewModel.location.collectAsState()
    val roadPoints by locationViewModel.roadPathPoints.collectAsState()
    val liveBusSpeed by locationViewModel.currentBusSpeed.collectAsState()

    val staticPickupPoints = remember {
        listOf(
            LatLng(27.7781, 85.3524), LatLng(27.7533, 85.3436),
            LatLng(27.7390, 85.3340), LatLng(27.7320, 85.3090),
            LatLng(27.6930, 85.3175)
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(27.7172, 85.3240), 12f)
    }

    var busIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    LaunchedEffect(Unit) { busIcon = createBusMarkerIcon(context) }

    // Logic to update route (Driver only) and Camera (Parent only)
    LaunchedEffect(coordinates, currentLocation) {
        if (model?.typeofUser == "Driver" && currentLocation != null) {
            locationViewModel.fetchRoadRoute(currentLocation!!, staticPickupPoints)
        }

        if (model?.typeofUser == "Parent") {
            val parts = coordinates.split(",")
            val lat = parts.getOrNull(0)?.toDoubleOrNull()
            val lng = parts.getOrNull(1)?.toDoubleOrNull()
            if (lat != null && lng != null) {
                cameraPositionState.animate(CameraUpdateFactory.newLatLng(LatLng(lat, lng)))
            }
        }
    }

    Card(modifier = modifier.fillMaxWidth().height(400.dp), shape = RoundedCornerShape(16.dp)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = model?.typeofUser == "Driver")
        ) {

            // --- 1. ROUTE & STOPS (ONLY FOR DRIVER) ---
            if (model?.typeofUser == "Driver") {
                if (roadPoints.isNotEmpty()) {
                    Polyline(points = roadPoints, color = Color(0xFF1A73E8), width = 12f, jointType = JointType.ROUND)
                }
                staticPickupPoints.forEachIndexed { index, point ->
                    Marker(
                        state = rememberMarkerState(position = point),
                        title = if (index == staticPickupPoints.lastIndex) "School" else "Stop ${index + 1}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    )
                }
            }

            // --- 2. BUS MARKERS (VISIBLE TO ALL) ---
            if (busIcon != null) {
                if (model?.typeofUser == "Admin") {
                    allBuses.forEach { bus ->
                        val parts = bus.currentLocation.split(",")
                        val lat = parts.getOrNull(0)?.toDoubleOrNull()
                        val lng = parts.getOrNull(1)?.toDoubleOrNull()
                        if (lat != null && lng != null) {
                            Marker(
                                state = rememberMarkerState(position = LatLng(lat, lng)),
                                title = "Bus ${bus.busNumber}",
                                snippet = "Speed: ${"%.1f".format(bus.speed * 3.6)} KM/H",
                                icon = busIcon
                            )
                        }
                    }
                } else {
                    // This section handles both Driver and Parent marker display
                    val displayCoords = if (model?.typeofUser == "Driver") {
                        currentLocation
                    } else {
                        // Parent uses the 'coordinates' string passed from the screen
                        val parts = coordinates.split(",")
                        val lat = parts.getOrNull(0)?.toDoubleOrNull()
                        val lng = parts.getOrNull(1)?.toDoubleOrNull()
                        if (lat != null && lng != null) LatLng(lat, lng) else null
                    }

                    displayCoords?.let {
                        Marker(
                            state = rememberMarkerState(position = it),
                            title = if (model?.typeofUser == "Driver") "My Bus" else "Child's Bus",
                            // This snippet ensures speed is visible when tapping the icon
                            snippet = "Live Speed: ${"%.1f".format(liveBusSpeed * 3.6)} KM/H",
                            icon = busIcon
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ETACard(childName: String, eta: Int, cardColor: Color) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardColor), modifier = Modifier.width(260.dp).height(100.dp)) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(50)).background(Color.White), contentAlignment = Alignment.Center) { Text("👶", fontSize = 24.sp) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = if (eta <= 1) "Arriving Now" else "$eta mins to arrival", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(text = "For $childName", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            }
            Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f))
        }
    }
}