package com.example.busmate.view.dashboard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.busmate.data.AdminActionsImpl
import com.example.busmate.data.LocationImpl
import com.example.busmate.model.UserModel
import com.example.busmate.viewmodel.AdminActionsViewModel
import com.example.busmate.viewmodel.LocationViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LiveLocationScreen(viewModel:LocationViewModel, busId: String) {
    val coordinates by viewModel.currentBusCoordinates.collectAsState()
    val bg = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardOrange = MaterialTheme.colorScheme.primaryContainer
    val cardGreen = MaterialTheme.colorScheme.secondaryContainer
    val context = LocalContext.current
    val activity = context as Activity
    var model by remember {
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

            // Display Current Fetch LatLng as Text
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

            // Map
            MapPrototype(
                modifier = Modifier.padding(horizontal = 16.dp),
                cardColor = MaterialTheme.colorScheme.surfaceVariant,
                model = model,
                context = context
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Username
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

            // Cards Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    ETACard(
                        modifier = Modifier.width(280.dp),
                        cardColor = cardOrange
                    )
                }

                item {
                    ETACard(
                        modifier = Modifier.width(280.dp),
                        cardColor = cardGreen
                    )
                }
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
    context: Context
) {
    var permissionGranted by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionGranted =
            (result[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                    result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
    }

    // Request permissions only for Driver or Parent user types
    LaunchedEffect(model?.typeofUser) {
        if (model?.typeofUser == "Driver" || model?.typeofUser == "Parent") {
            if (!permissionGranted) {
                launcher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    // If permission is granted, show the map
    if (permissionGranted) {
        cardMap(cardColor, modifier, context)
    } else {
        // If the permission is not granted and the user is a Driver or Parent, show a Toast
        if (model?.typeofUser == "Driver" || model?.typeofUser == "Parent") {
            Toast.makeText(context, "Please enable location permission.", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun cardMap(cardColor: Color, modifier: Modifier, context: Context) {
    val viewModel = remember { LocationViewModel(LocationImpl(context)) }
    val currentLocation by viewModel.location.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation ?: LatLng(27.7172, 85.3240), // Default location
            16f
        )
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
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true // Blue dot enabled
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = true
            )
        ) {
            //marker sarker or bus ko icon halna sakincha pachi
        }
    }
}

@Composable
fun ETACard(modifier: Modifier = Modifier, cardColor: Color) {
    val textWhite = MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.aspectRatio(2.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Image Placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(100))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("👩", fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text info
            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = "ETA 15 minutes",
                    color = textWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Bus No: 1533",
                    color = textWhite.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            // Bus Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = "Bus",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
//testing live location



//task 1 check if child is assigned to bus/driver if both have same routeid. for example: child has routeId:10 and driver/bus has routeID:10. Are they linked.

// task 2 i have tested with static driverUid, now fetch data of location of driver. take reference to BusProfile.kt
// as reference how i fetched data of driver/bus. And then implement location by fetching driverUid from
// firebase. Parent should see live location of bus where location.