package com.example.busmate.view.parent

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.data.ChildRepositoryImpl
import com.example.busmate.model.BusModel
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.viewmodel.BusViewModel
import com.example.busmate.viewmodel.ChildViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.example.busmate.R

class BusDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val busRepository = BusRepositoryImpl()
        val childRepository = ChildRepositoryImpl()
        val busViewModel = BusViewModel(busRepository)
        val childViewModel = ChildViewModel(childRepository)

        setContent {
            BusMateTheme {
                BusDetailsScreen(busViewModel, childViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusDetailsScreen(busViewModel: BusViewModel, childViewModel: ChildViewModel) {
    val context = LocalContext.current
    val children by childViewModel.children.collectAsState()

    var selectedChild by remember { mutableStateOf(children.firstOrNull()) }
    var busDetails by remember { mutableStateOf<BusModel?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            childViewModel.observeChildren(it)
        }
    }

    LaunchedEffect(children) {
        if (selectedChild == null && children.isNotEmpty()) {
            selectedChild = children.first()
        }
    }
    LaunchedEffect(selectedChild) {
        selectedChild?.let { child ->
            isLoading = true
            busViewModel.getBusByRouteId(child.busRouteId) { fetchedBus ->
                busDetails = fetchedBus
                isLoading = false
            }
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF7F7F7)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color(0xFF2854D8)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(40.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { (context as? Activity)?.onBackPressed() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Bus Information",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))


                Text(text = "Select Child", fontSize = 14.sp, color = Color.White.copy(0.8f))
                LazyRow(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                    items(children) { child ->
                        FilterChip(
                            modifier = Modifier.padding(end = 8.dp),
                            selected = selectedChild == child,
                            onClick = { selectedChild = child },
                            label = { Text("${child.firstName} ${child.lastName}",fontWeight = FontWeight.SemiBold)},
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White,
                                selectedLabelColor = Color(0xFF2854D8),
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-40).dp), // Creates the overlapping effect
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        Box(Modifier.height(180.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF2854D8))
                        }
                    } else if (busDetails != null) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F3F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            val busImageUrl = busDetails!!.busImage.trim()
                            if (busImageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = busImageUrl,
                                    contentDescription = "Bus Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Image(
                                    painter = painterResource(R.drawable.schoolbus),
                                    contentDescription = "Default Bus",
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))


                        DetailText(label = "Route ID", value = busDetails!!.routeId)
                        DetailText(label = "Bus Number", value = busDetails!!.busNumber)
                        DetailText(label = "License Plate", value = busDetails!!.licensePlate)
                        DetailText(label = "Seating Capacity", value = "${busDetails!!.capacity} Seats")
                        DetailText(label = "Maintenance Status", value = busDetails!!.maintenanceStatus)
                    } else {
                        Text("No bus data found.", color = Color.Gray, modifier = Modifier.padding(20.dp))
                    }
                }
            }
        }
    }
}
@Composable
fun DetailText(label: String, value: String) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 0.5.dp, color = Color.LightGray)
    }
}

