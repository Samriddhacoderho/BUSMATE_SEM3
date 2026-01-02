package com.example.busmate.view.dashboard

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun DriverLocationScreen(
    viewModel: LocationViewModel,
    driverUid: String,
    busId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observing state from your shared ViewModel
    val currentGpsLocation by viewModel.location.collectAsState()
    val students by viewModel.driverStudents.collectAsState()
    val snappedPath by viewModel.polylinePoints.collectAsState()

    val schoolLatLng = remember { LatLng(27.7174, 85.3435) } // Deerwalk Location
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(27.7172, 85.3240), 14f)
    }

    // Get API Key from Manifest for Road Snapping
    val apiKey = remember {
        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData.getString("com.google.android.geo.API_KEY") ?: ""
    }

    var permissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        permissionGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    // Initialize tracking and data
    LaunchedEffect(permissionGranted, busId) {
        if (permissionGranted) {
            viewModel.startTracking(busId = busId, driverUid = driverUid)
            viewModel.fetchStudentsForRoute(busId)
        } else {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    // ROAD SNAPPING TRIGGER: Updates whenever driver moves or student list loads
    LaunchedEffect(currentGpsLocation, students) {
        currentGpsLocation?.let { startPos ->
            val waypoints = students.map { LatLng(it.pickUpLat, it.pickUpLng) }
            // This calls your ViewModel's existing road-snapping logic
            viewModel.fetchRoadSnappedRoute(
                origin = startPos,
                destination = schoolLatLng,
                apiKey = apiKey
            )
        }
    }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Minimal Header to maximize map space
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2567E8),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Navigation: $busId", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Destination: Deerwalk Institute", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White)
                }
            }

            // FULL SCREEN MAP CARD
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = permissionGranted,
                        isTrafficEnabled = true
                    ),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    // 1. THE ROAD-SNAPPED POLYLINE
                    if (snappedPath.isNotEmpty()) {
                        Polyline(
                            points = snappedPath,
                            color = Color(0xFF2567E8),
                            width = 15f,
                            jointType = JointType.ROUND,
                            startCap = RoundCap(),
                            endCap = RoundCap()
                        )
                    }

                    // 2. STUDENT MARKERS
                    students.forEach { student ->
                        Marker(
                            state = rememberMarkerState(position = LatLng(student.pickUpLat, student.pickUpLng)),
                            title = "${student.firstName} (Pickup)",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }

                    // 3. SCHOOL MARKER
                    Marker(
                        state = rememberMarkerState(position = schoolLatLng),
                        title = "Deerwalk Institute",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }

                // ZOOM CONTROLS OVERLAY
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FloatingActionButton(
                        onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) } },
                        containerColor = Color.White,
                        modifier = Modifier.size(50.dp)
                    ) { Icon(Icons.Default.Add, contentDescription = "Zoom In") }

                    FloatingActionButton(
                        onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) } },
                        containerColor = Color.White,
                        modifier = Modifier.size(50.dp)
                    ) { Icon(Icons.Default.Remove, contentDescription = "Zoom Out") }

                    FloatingActionButton(
                        onClick = {
                            val builder = LatLngBounds.Builder()
                            currentGpsLocation?.let { builder.include(it) }
                            students.forEach { builder.include(LatLng(it.pickUpLat, it.pickUpLng)) }
                            builder.include(schoolLatLng)
                            scope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(builder.build(), 200))
                            }
                        },
                        containerColor = Color.White,
                        modifier = Modifier.size(50.dp)
                    ) { Icon(Icons.Default.MyLocation, contentDescription = "Recenter") }
                }
            }
        }
    }
}