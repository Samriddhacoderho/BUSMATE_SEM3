//package com.example.busmate.view
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.busmate.model.BusModel
//import com.example.busmate.model.ChildModel
//import com.example.busmate.viewmodel.BusViewModel
//
//@Composable
//fun BusDetailsScreen(
//    viewModel: BusViewModel,
//    children: List<ChildModel>
//) {
//    // State to track which child is currently selected
//    var selectedChild by remember { mutableStateOf(children.firstOrNull()) }
//    var bus by remember { mutableStateOf<BusModel?>(null) }
//    var isLoading by remember { mutableStateOf(false) }
//
//    // Re-fetch bus data whenever the selected child changes
//    LaunchedEffect(selectedChild) {
//        selectedChild?.let { child ->
//            isLoading = true
//            viewModel.getBusByRouteId(child.busRouteId) { fetchedBus ->
//                bus = fetchedBus
//                isLoading = false
//            }
//        }
//    }
//
//    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
//        Text(text = "Select Child", fontSize = 18.sp, fontWeight = FontWeight.Bold)
//
//        // Chip Row for switching between children
//        LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
//            items(children) { child ->
//                FilterChip(
//                    modifier = Modifier.padding(end = 8.dp),
//                    selected = selectedChild == child,
//                    onClick = { selectedChild = child },
//                    label = { Text("${child.firstName} ${child.lastName}") }
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        if (isLoading) {
//            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                CircularProgressIndicator()
//            }
//        } else if (bus != null) {
//            Text(
//                text = "Bus Details for ${selectedChild?.firstName}",
//                fontSize = 20.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = MaterialTheme.colorScheme.primary
//            )
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                elevation = CardDefaults.cardElevation(6.dp),
//                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
//            ) {
//                Column(modifier = Modifier.padding(20.dp)) {
//                    // ✅ Replaced DetailRow with DetailText
//                    DetailText(label = "Route ID", value = bus!!.routeId)
//
//                    val driverName = bus!!.driver?.let { "${it.firstName} ${it.lastName}" } ?: "Not Assigned"
//                    DetailText(label = "Driver Name", value = driverName)
//
//                    DetailText(label = "Bus Number", value = bus!!.busNumber)
//                    DetailText(label = "License Plate", value = bus!!.licensePlate)
//                    DetailText(label = "Seating Capacity", value = "${bus!!.capacity} Seats")
//                    DetailText(label = "Maintenance Status", value = bus!!.maintenanceStatus)
//                }
//            }
//        } else {
//            Text(text = "No bus found for ${selectedChild?.firstName}'s route.", color = Color.Red)
//        }
//    }
//}
//
//// ✅ The helper composable you want to use
//@Composable
//fun DetailText(label: String, value: String) {
//    Column(Modifier.padding(vertical = 8.dp)) {
//        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
//        Text(value, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
//        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 0.5.dp, color = Color.LightGray)
//    }
//}