package com.example.busmate.view.dashboard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.busmate.viewmodel.LocationViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun DriverLocationScreen(
    viewModel: LocationViewModel,
    driverUid: String,
    busId: String
) {
    val context = LocalContext.current
    val currentGpsLocation by viewModel.location.collectAsState()
    val isTripRunning by viewModel.isTripRunning.collectAsState()

    // 1. Handle Permissions specifically for the Driver
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        permissionGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    // 2. Start Tracking on Launch
    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            // This triggers the repo.startLocationUpdates and syncs to Firebase via driverUid
            viewModel.startTracking(busId = busId, driverUid = driverUid)
        } else {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Driver Console: Active Trip",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // Status Bar
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (permissionGranted) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = if (permissionGranted) Color(0xFF2E7D32) else Color.Red
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (permissionGranted) "Broadcasting Live Location..." else "GPS Offline - Check Permissions",
                        fontWeight = FontWeight.Medium,
                        color = if (permissionGranted) Color(0xFF2E7D32) else Color.Red
                    )
                }
            }

            // Driver Map (Shows current GPS position)
            Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(27.7172, 85.3240), 15f)
                }

                // Auto-center camera on driver's current position
                LaunchedEffect(currentGpsLocation) {
                    currentGpsLocation?.let {
                        cameraPositionState.centerOnLocation(it)
                    }
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = permissionGranted,
                        mapType = MapType.NORMAL
                    ),
                    uiSettings = MapUiSettings(zoomControlsEnabled = true)
                )
            }
        }
    }
}

// Extension to help centering the camera
private suspend fun CameraPositionState.centerOnLocation(latLng: LatLng) {
    animate(
        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng, 15f)
    )
}