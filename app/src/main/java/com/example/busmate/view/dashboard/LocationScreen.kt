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
import com.example.busmate.model.UserModel
import com.example.busmate.viewmodel.AccelRecieverViewModel
import com.example.busmate.viewmodel.ChildViewModel
import com.example.busmate.viewmodel.LocationViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*

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
    busId: String
) {
    val coordinates by viewModel.currentBusCoordinates.collectAsState()
    val childEtas by viewModel.childEtas.collectAsState()
    val children by childViewModel.children.collectAsState()
    val liveReading by accelViewModel.firebaseReading.observeAsState()
    val isTripRunning by viewModel.isTripRunning.collectAsState()

    val context = LocalContext.current
    val activity = context as Activity
    val model by remember {
        mutableStateOf(activity.intent.getParcelableExtra<UserModel>("model"))
    }

    LaunchedEffect(busId) {
        viewModel.startTracking(busId = busId)
        accelViewModel.startTrackingBus(busId)
        model?.uid?.let { parentUid ->
            childViewModel.observeChildren(parentUid)
        }
    }

    LaunchedEffect(coordinates, liveReading, children) {
        viewModel.updateChildEtas(
            children = children,
            currentCoords = coordinates,
            rawSpeedMps = liveReading?.speedMps ?: 0f
        )
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Live Location Tracking",
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

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Text("Current LatLng: ", fontWeight = FontWeight.Bold)
                    Text(coordinates, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MapPrototype(
                modifier = Modifier.padding(horizontal = 16.dp),
                model = model,
                context = context,
                coordinates = coordinates,
                locationViewModel = viewModel,
                busId = busId // Pass busId down
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
    busId: String // Added parameter
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
        CardMap(modifier, model, context, coordinates, locationViewModel, busId)
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
    busId: String // Added parameter
) {
    val currentLocation by locationViewModel.location.collectAsState()
    val busLatLng = remember(coordinates) {
        val parts = coordinates.split(",")
        val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull() ?: 27.7172
        val lng = parts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 85.3240
        LatLng(lat, lng)
    }

    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(busLatLng, 15f) }
    val markerState = rememberMarkerState(position = busLatLng)
    var busIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    LaunchedEffect(Unit) { busIcon = createBusMarkerIcon(context) }

    LaunchedEffect(busLatLng) {
        if (model?.typeofUser == "Parent") {
            markerState.position = busLatLng
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(busLatLng))
        }
    }

    LaunchedEffect(currentLocation) {
        if (model?.typeofUser == "Driver" && currentLocation != null) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(currentLocation!!))
        }
    }

    // CORRECTED: Added the missing busId parameter here
    LaunchedEffect(Unit) {
        if (model?.typeofUser == "Driver") {
            locationViewModel.startTracking(driverUid = model.uid, busId = busId)
        }
    }

    DisposableEffect(Unit) {
        onDispose { locationViewModel.stopLocationUpdates() }
    }

    Card(modifier = modifier.fillMaxWidth().height(300.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(8.dp)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = model?.typeofUser == "Driver"),
            uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = model?.typeofUser == "Driver")
        ) {
            if (model?.typeofUser == "Parent" && busIcon != null) {
                Marker(state = markerState, title = "Bus Location", icon = busIcon)
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