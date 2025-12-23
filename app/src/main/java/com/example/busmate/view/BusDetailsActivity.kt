package com.example.busmate.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
        topBar = {
            TopAppBar(
                title = { Text("Bus Information", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.onBackPressed() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F7F7)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Select Child", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            LazyRow(modifier = Modifier.padding(vertical = 12.dp)) {
                items(children) { child ->
                    FilterChip(
                        modifier = Modifier.padding(end = 8.dp),
                        selected = selectedChild == child,
                        onClick = { selectedChild = child },
                        label = { Text("${child.firstName} ${child.lastName}") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Black)
                }
            } else if (busDetails != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        DetailText(label = "Route ID", value = busDetails!!.routeId)
                        DetailText(label = "Bus Number", value = busDetails!!.busNumber)
                        DetailText(label = "License Plate", value = busDetails!!.licensePlate)
                        DetailText(label = "Seating Capacity", value = "${busDetails!!.capacity} Seats")
                        DetailText(label = "Maintenance Status", value = busDetails!!.maintenanceStatus)
                    }
                }
            } else {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No bus data found.", color = Color.Gray)
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
//testing the driverdetails
