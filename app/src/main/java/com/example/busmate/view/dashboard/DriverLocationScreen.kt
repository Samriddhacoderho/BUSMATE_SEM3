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
import com.example.busmate.data.BusRepositoryImpl
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

    // Kathmandu + School
    val kathmandu = remember { LatLng(27.7172, 85.3240) }
    val schoolLatLng = remember { mutableStateOf(LatLng(27.7174, 85.3435)) }

// ADD this LaunchedEffect to fetch school location:
    LaunchedEffect(Unit) {
        val busRepo = BusRepositoryImpl()
        busRepo.getSchoolLocation { location, _ ->
            location?.let {
                schoolLatLng.value = it
            }
        }
    }


    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(kathmandu, 12f)
    }

    val apiKey = remember {
        try {
            context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                .metaData.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) { "" }
    }

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(permissionGranted, busId) {
        if (permissionGranted) {
            viewModel.startTracking(busId = busId, driverUid = driverUid)
            viewModel.fetchStudentsForRoute(busId)
        } else {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Route calculation (already correct)
    LaunchedEffect(currentGpsLocation, students, previewTripType) {
        if (currentGpsLocation != null && students.isNotEmpty() && apiKey.isNotEmpty()) {
            viewModel.fetchDriverRouteWithWaypoints(
                origin = currentGpsLocation!!,
                schoolLocation = schoolLatLng.value,
                students = students,
                tripType = previewTripType,
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

            // HEADER + TOGGLE
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2567E8),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Route Preview",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Row {
                        FilterChip(
                            selected = previewTripType == "Pickup",
                            onClick = { previewTripType = "Pickup" },
                            label = { Text("Pickup Mode") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White,
                                selectedLabelColor = Color(0xFF2567E8), // active text
                                containerColor = Color(0xFF2567E8),     // inactive background
                                labelColor = Color.White                // inactive text (WHITE)
                            )
                        )

                        Spacer(Modifier.width(8.dp))

                        FilterChip(
                            selected = previewTripType == "Drop-off",
                            onClick = { previewTripType = "Drop-off" },
                            label = { Text("Drop-off Mode") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White,
                                selectedLabelColor = Color(0xFF2567E8),
                                containerColor = Color(0xFF2567E8),
                                labelColor = Color.White
                            )
                        )
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

                    // ROUTE POLYLINE
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

                    // ===============================
                    // NUMBERED STUDENT STOP MARKERS
                    // ===============================
                    students.forEachIndexed { index, student ->

                        val position = if (previewTripType == "Pickup") {
                            LatLng(student.pickUpLat, student.pickUpLng)
                        } else {
                            LatLng(student.dropOffLat, student.dropOffLng)
                        }

                        Marker(
                            state = MarkerState(position = position),
                            title = "${index + 1}. ${student.firstName} ${student.lastName}",
                            snippet = if (previewTripType == "Pickup")
                                "Pickup Stop"
                            else
                                "Drop-off Stop",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                if (previewTripType == "Pickup")
                                    BitmapDescriptorFactory.HUE_AZURE
                                else
                                    BitmapDescriptorFactory.HUE_ORANGE
                            )
                        )
                    }

                    // SCHOOL MARKER (FIXED)
                    Marker(
                        state = MarkerState(position = schoolLatLng.value),
                        title = "School Location",
                        snippet = " ",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED
                        )
                    )
                }

                // MAP CONTROLS
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.zoomIn()
                                )
                            }
                        },
                        containerColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In")
                    }

                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.zoomOut()
                                )
                            }
                        },
                        containerColor = Color.White
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                    }

                    FloatingActionButton(
                        onClick = {
                            val bounds = LatLngBounds.Builder()

                            currentGpsLocation?.let { bounds.include(it) }

                            students.forEach {
                                val p = if (previewTripType == "Pickup")
                                    LatLng(it.pickUpLat, it.pickUpLng)
                                else
                                    LatLng(it.dropOffLat, it.dropOffLng)

                                bounds.include(p)
                            }

                            bounds.include(schoolLatLng.value)

                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngBounds(
                                        bounds.build(),
                                        200
                                    )
                                )
                            }
                        },
                        containerColor = Color(0xFF2567E8)
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Center",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}