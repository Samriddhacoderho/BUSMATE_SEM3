package com.example.busmate.view.dashboard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.example.busmate.data.LocationImpl
import com.example.busmate.model.UserModel
import com.example.busmate.viewmodel.LocationViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.Marker

@Composable
fun LiveLocationScreen(viewModel: LocationViewModel, busId: String) {
    val coordinates by viewModel.currentBusCoordinates.collectAsState()
    val bg = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardOrange = MaterialTheme.colorScheme.primaryContainer
    val cardGreen = MaterialTheme.colorScheme.secondaryContainer
    val context = LocalContext.current
    val activity = context as Activity

    val model by remember {
        mutableStateOf(activity.intent.getParcelableExtra<UserModel>("model"))
    }

    LaunchedEffect(busId) {
        viewModel.fetchBusLocation(busId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = bg
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Live Location Tracking",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 24.dp)
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Start)
            )

            // Current LatLng Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current LatLng: ",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = coordinates,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Map Section
            MapPrototype(
                modifier = Modifier.padding(horizontal = 16.dp),
                cardColor = MaterialTheme.colorScheme.surfaceVariant,
                model = model,
                context = context,
                coordinates = coordinates
            )

            Spacer(modifier = Modifier.height(24.dp))

            // User Name
            Text(
                text = "Aliza Regmi",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ETA Cards
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item { ETACard(modifier = Modifier.width(280.dp), cardColor = cardOrange) }
                item { ETACard(modifier = Modifier.width(280.dp), cardColor = cardGreen) }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MapPrototype(
    modifier: Modifier = Modifier,
    cardColor: Color,
    model: UserModel?,
    context: Context,
    coordinates: String
) {
    var permissionGranted by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(model?.typeofUser) {
        if (!permissionGranted && (model?.typeofUser == "Driver" || model?.typeofUser == "Parent")) {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    if (permissionGranted) {
        cardMap(cardColor, modifier, context, model, coordinates)
    } else {
        Box(modifier = modifier.fillMaxWidth().height(250.dp).background(cardColor, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Text("Location permission required to view map")
        }
    }
}

@Composable
fun cardMap(
    cardColor: Color,
    modifier: Modifier,
    context: Context,
    model: UserModel?,
    coordinates: String
) {
    val viewModel = remember { LocationViewModel(LocationImpl(context)) }
    val currentLocation by viewModel.location.collectAsState()

    // Parse the LatLng string reactively
    val busLatLng = remember(coordinates) {
        val parts = coordinates.split(",")
        val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull() ?: 27.7172
        val lng = parts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 85.3240
        LatLng(lat, lng)
    }

    // Explicitly manage Marker State and Camera State
    val busMarkerState = rememberMarkerState(position = busLatLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(busLatLng, 15f)
    }

    // Effect: Update both Marker and Camera whenever coordinates change
    LaunchedEffect(busLatLng) {
        if (model?.typeofUser == "Parent") {
            busMarkerState.position = busLatLng // This forces the pin to move
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(busLatLng))
        }
    }

    // Driver Movement Tracking
    LaunchedEffect(currentLocation) {
        if (model?.typeofUser == "Driver" && currentLocation != null) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(currentLocation!!))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startTracking(driverUid = null)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopLocationUpdates() }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth().fillMaxHeight(0.6f)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = model?.typeofUser == "Driver"),
            uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = model?.typeofUser == "Driver")
        ) {
            if (model?.typeofUser == "Parent") {
                Marker(
                    state = busMarkerState,
                    title = "Bus Location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }
        }
    }
}

@Composable
fun ETACard(modifier: Modifier = Modifier, cardColor: Color) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = modifier.height(110.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(100)).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("👩", fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("ETA 15 minutes", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Bus No: 1533", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
            Icon(imageVector = Icons.Default.DirectionsBus, contentDescription = null, tint = Color.White)
        }
    }
}