package com.example.busmate.view.dashboard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
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
import com.example.busmate.model.ChildModel
import com.example.busmate.viewmodel.LocationViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun DriverLocationScreen(
    viewModel: LocationViewModel,
    driverUid: String,
    busId: String // This receives user?.schoolId (e.g., "101")
) {
    val context = LocalContext.current
    val currentGpsLocation by viewModel.location.collectAsState()

    // IMPORTANT: Observe the student list from the ViewModel
    val students by viewModel.driverStudents.collectAsState()

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        permissionGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    // TRIGGER: Fetch data and start tracking when screen opens or busId changes
    LaunchedEffect(permissionGranted, busId) {
        if (permissionGranted) {
            // Start sending driver GPS to Firebase
            viewModel.startTracking(busId = busId, driverUid = driverUid)

            // REUSE: Use your Attendance logic to fetch the manifest
            viewModel.fetchStudentsForRoute(busId)
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
            // Header: Route Info
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Driver's Navigation Route", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Students assigned: ${students.size}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = if(permissionGranted) Color.Green else Color.Red)
                }
            }

            // MAP SECTION: Shows Driver and Student Pickups
            Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(27.7172, 85.3240), 14f)
                }

                // Focus camera on driver
                LaunchedEffect(currentGpsLocation) {
                    currentGpsLocation?.let { cameraPositionState.centerOnLocation(it) }
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = permissionGranted)
                ) {
                    // Place markers for all students on the route
                    students.forEach { student ->
                        Marker(
                            state = rememberMarkerState(position = LatLng(student.pickUpLat, student.pickUpLng)),
                            title = "${student.firstName} ${student.lastName}",
                            snippet = "Pickup: ${student.pickUpLocation}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }
                }
            }

            // STUDENT LIST SECTION
            Text(
                text = "Pick-up Manifest",
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            LazyColumn(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // 'items' works here because of the 'import androidx.compose.foundation.lazy.items'
                items(students) { student ->
                    StudentCard(student)
                }
            }
        }
    }
}

@Composable
fun StudentCard(student: ChildModel) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${student.firstName} ${student.lastName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = "Location: ${student.pickUpLocation}", fontSize = 12.sp, color = Color.DarkGray)
                // GPS Coordinates as requested
                Text(
                    text = "Lat: ${student.pickUpLat}, Lng: ${student.pickUpLng}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            IconButton(onClick = { /* Add Google Maps Intent here */ }) {
                Icon(Icons.Default.Directions, contentDescription = "Navigate", tint = Color(0xFF1976D2))
            }
        }
    }
}

private suspend fun CameraPositionState.centerOnLocation(latLng: LatLng) {
    animate(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng, 15f))
}