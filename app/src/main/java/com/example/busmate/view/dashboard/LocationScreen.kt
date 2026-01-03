package com.example.busmate.view.dashboard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    busId: String,
    selectedChildId: String? = null
) {
    val coordinates by viewModel.currentBusCoordinates.collectAsState()
    val childEtas by viewModel.childEtas.collectAsState()
    val children by childViewModel.children.collectAsState()
    val liveReading by accelViewModel.firebaseReading.observeAsState()
    val isTripRunning by viewModel.isTripRunning.collectAsState()

    // FIXED: Collect polylinePoints (matching ViewModel name)
    val polylinePoints by viewModel.polylinePoints.collectAsState()

    val allBuses by viewModel.allBuses.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity
    val model by remember {
        mutableStateOf(activity.intent.getParcelableExtra<UserModel>("model"))
    }

    // Retrieve API Key from Manifest
    val apiKey = remember {
        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData.getString("com.google.android.geo.API_KEY") ?: ""
    }

    LaunchedEffect(busId, model?.typeofUser) {
        if (model?.typeofUser == "Admin") {
            viewModel.trackAllBuses()
        } else {
            viewModel.startTracking(busId = busId)
            accelViewModel.startTrackingBus(busId)
            model?.uid?.let { parentUid ->
                childViewModel.observeChildren(parentUid)
            }
        }
    }

    /// 1. THIS BLOCK REQUESTS THE ROUTE
    LaunchedEffect(coordinates, children, selectedChildId) {
        if (model?.typeofUser == "Parent") {
            val busParts = coordinates.split(",")
            val busLat = busParts.getOrNull(0)?.toDoubleOrNull()
            val busLng = busParts.getOrNull(1)?.toDoubleOrNull()
            val child = children.find { it.studentId == selectedChildId }

            if (busLat != null && busLng != null && child != null && apiKey.isNotEmpty()) {
                viewModel.fetchRoadSnappedRoute(
                    origin = LatLng(busLat, busLng),
                    destination = LatLng(child.pickUpLat, child.pickUpLng),
                    apiKey = apiKey
                )
            }
        }
    }

// 2. THIS BLOCK UPDATES THE CARDS ONCE DATA ARRIVES
    LaunchedEffect(polylinePoints, liveReading?.speedMps) {
        if (model?.typeofUser == "Parent") {
            val filteredChildren = children.filter { it.studentId == selectedChildId }
            viewModel.updateChildEtas(
                children = filteredChildren,
                currentCoords = coordinates,
                rawSpeedMps = liveReading?.speedMps ?: 0f
            )
            Log.d("DEBUG_CODE",coordinates)
        }
    }

            // Calculate coordinates for Directions API
            val busParts = coordinates.split(",")
            val busLat = busParts.getOrNull(0)?.toDoubleOrNull()
            val busLng = busParts.getOrNull(1)?.toDoubleOrNull()
            val child = children.find { it.studentId == selectedChildId }

            if (busLat != null && busLng != null && child != null && apiKey.isNotEmpty()) {
                viewModel.fetchRoadSnappedRoute(
                    origin = LatLng(busLat, busLng),
                    destination = LatLng(child.pickUpLat, child.pickUpLng),
                    apiKey = apiKey
                )
            }


    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = if (model?.typeofUser == "Admin") "Fleet Live Overview" else "Live Location Tracking",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (!isTripRunning && model?.typeofUser == "Parent") {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = "Note: Bus is currently idle.",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Red,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            MapPrototype(
                modifier = Modifier.padding(horizontal = 16.dp),
                model = model,
                context = context,
                coordinates = coordinates,
                locationViewModel = viewModel,
                busId = busId,
                allBuses = (if (model?.typeofUser == "Admin") allBuses else emptyList()) as List<BusModel>,
                polylinePoints = polylinePoints, // Passed correctly now
                selectedChildId = selectedChildId,
                children = children
            )

            if (model?.typeofUser == "Parent") {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Your Children's Arrival Info", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    items(childEtas) { etaState ->
                        ETACard(childName = etaState.childName, eta = etaState.etaMinutes, cardColor = MaterialTheme.colorScheme.primaryContainer)
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
    allBuses: List<com.example.busmate.model.BusModel>,
    polylinePoints: List<LatLng>,
    selectedChildId: String?,
    children: List<com.example.busmate.model.ChildModel>
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
        CardMap(modifier, model, context, coordinates, locationViewModel, busId, allBuses, polylinePoints, selectedChildId, children)
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
    coordinates: String,
    locationViewModel: LocationViewModel,
    busId: String,
    allBuses: List<com.example.busmate.model.BusModel>,
    polylinePoints: List<LatLng>,
    selectedChildId: String?,
    children: List<com.example.busmate.model.ChildModel>
) {
    val defaultLatLng = LatLng(27.7172, 85.3240)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 12f)
    }

    var busIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    LaunchedEffect(Unit) { busIcon = createBusMarkerIcon(context) }

    LaunchedEffect(coordinates) {
        if (model?.typeofUser == "Parent") {
            val parts = coordinates.split(",")
            val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull() ?: return@LaunchedEffect
            val lng = parts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: return@LaunchedEffect
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(LatLng(lat, lng)))
        }
    }

    Card(modifier = modifier.fillMaxWidth().height(400.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(8.dp)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = model?.typeofUser == "Driver"),
            uiSettings = MapUiSettings(zoomControlsEnabled = true)
        ) {
            if (model?.typeofUser == "Admin" && busIcon != null) {
                allBuses.forEach { bus ->
                    val parts = bus.currentLocation.split(",")
                    val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull()
                    val lng = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
                    if (lat != null && lng != null) {
                        Marker(
                            state = rememberMarkerState(position = LatLng(lat, lng)),
                            title = "Bus ${bus.busNumber}",
                            snippet = "Speed: ${bus.speed} km/h",
                            icon = busIcon
                        )
                    }
                }
            }

            if (model?.typeofUser == "Parent" && busIcon != null) {
                // 1. Draw Road-Snapped Polyline
                if (polylinePoints.isNotEmpty()) {
                    Polyline(
                        points = polylinePoints,
                        color = Color(0xFFFF9800),
                        width = 12f,
                        jointType = JointType.ROUND,
                        startCap = RoundCap(),
                        endCap = RoundCap()
                    )
                }

                // 2. Bus Marker
                val parts = coordinates.split(",")
                val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull()
                val lng = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
                if (lat != null && lng != null) {
                    Marker(
                        state = rememberMarkerState(position = LatLng(lat, lng)),
                        title = "Your Child's Bus",
                        icon = busIcon
                    )
                }

                // 3. Child Pickup Marker
                val child = children.find { it.studentId == selectedChildId }
                child?.let {
                    Marker(
                        state = rememberMarkerState(position = LatLng(it.pickUpLat, it.pickUpLng)),
                        title = "Pickup Point",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
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