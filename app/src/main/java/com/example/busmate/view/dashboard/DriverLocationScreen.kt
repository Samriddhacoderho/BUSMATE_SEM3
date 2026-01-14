package com.example.busmate.view.dashboard

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import com.google.android.gms.maps.model.*
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

    val currentGpsLocation by viewModel.location.collectAsState()
    val students by viewModel.driverStudents.collectAsState()
    val snappedPath by viewModel.polylinePoints.collectAsState()
    var previewTripType by remember { mutableStateOf("Pickup") }

    // 1. Define Kathmandu Coordinates
    val kathmandu = remember { LatLng(27.7172, 85.3240) }
    val schoolLatLng = remember { LatLng(27.7174, 85.3435) }

    // 2. Initialize the camera specifically at Kathmandu instead of (0,0)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(kathmandu, 12f)
    }

    val apiKey = remember {
        try {
            context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                .metaData.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) { "" }
    }

    var permissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        permissionGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(permissionGranted, busId) {
        if (permissionGranted) {
            viewModel.startTracking(busId = busId, driverUid = driverUid)
            viewModel.fetchStudentsForRoute(busId)
        } else {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    // Road Snapping Logic
    LaunchedEffect(currentGpsLocation, students, previewTripType) {
        if (currentGpsLocation != null && students.isNotEmpty() && apiKey.isNotEmpty()) {
            viewModel.fetchDriverRouteWithWaypoints(
                origin = currentGpsLocation!!,
                schoolLocation = schoolLatLng, // Deerwalk
                students = students,
                tripType = previewTripType,    // Local toggle value
                apiKey = apiKey
            )
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header with Local Toggle
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
                        Text(
                            text = "Route Preview",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Row(modifier = Modifier.padding(top = 8.dp)) {
                            FilterChip(
                                selected = previewTripType == "Pickup",
                                onClick = { previewTripType = "Pickup" },
                                label = { Text("Pickup Mode") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.White,
                                    labelColor = if (previewTripType == "Pickup") Color(0xFF2567E8) else Color.White
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            FilterChip(
                                selected = previewTripType == "Drop-off",
                                onClick = { previewTripType = "Drop-off" },
                                label = { Text("Drop-off Mode") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.White,
                                    labelColor = if (previewTripType == "Drop-off") Color(0xFF2567E8) else Color.White
                                )
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    if (snappedPath.isNotEmpty()) {
                        Polyline(
                            points = snappedPath,
                            color = Color(0xFF2567E8),
                            width = 12f,
                            jointType = JointType.ROUND,
                            startCap = RoundCap(),
                            endCap = RoundCap()
                        )
                    }

                    // FIXED: Student Markers now move their position based on toggle
                    students.forEach { student ->
                        // Determine the correct LatLng based on the toggle
                        val currentTargetLocation = if (previewTripType == "Pickup") {
                            LatLng(student.pickUpLat, student.pickUpLng)
                        } else {
                            LatLng(student.dropOffLat, student.dropOffLng)
                        }

                        Marker(
                            // This position now updates dynamically
                            state = rememberMarkerState(position = currentTargetLocation),
                            title = "${student.firstName} ${student.lastName}",
                            snippet = if (previewTripType == "Pickup") "Pickup Point" else "Drop-off Point",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                if (previewTripType == "Pickup") BitmapDescriptorFactory.HUE_AZURE
                                else BitmapDescriptorFactory.HUE_ORANGE
                            )
                        )
                    }

                    // School Marker (Deerwalk)
                    Marker(
                        state = rememberMarkerState(position = schoolLatLng),
                        title = "Deerwalk Institute",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }

                // UI Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FloatingActionButton(
                        onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) } },
                        containerColor = Color.White, modifier = Modifier.size(54.dp)
                    ) { Icon(Icons.Default.Add, "+", tint = Color.Black) }

                    FloatingActionButton(
                        onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) } },
                        containerColor = Color.White, modifier = Modifier.size(54.dp)
                    ) { Icon(Icons.Default.Remove, "-", tint = Color.Black) }

                    FloatingActionButton(
                        onClick = {
                            val builder = LatLngBounds.Builder()
                            currentGpsLocation?.let { builder.include(it) }

                            // FIXED: Include the correct points (Pick vs Drop) when centering
                            students.forEach { student ->
                                if (previewTripType == "Pickup") {
                                    builder.include(LatLng(student.pickUpLat, student.pickUpLng))
                                } else {
                                    builder.include(LatLng(student.dropOffLat, student.dropOffLng))
                                }
                            }

                            builder.include(schoolLatLng)
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngBounds(builder.build(), 200)
                                )
                            }
                        },
                        containerColor = Color(0xFF2567E8),
                        modifier = Modifier.size(54.dp)
                    ) { Icon(Icons.Default.MyLocation, "Center", tint = Color.White) }
                }
            }
        }
    }
}