package com.example.busmate.view
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.viewmodel.BusViewModel
@Composable
fun BusDetailsScreen(viewModel: BusViewModel, routeId: String) {
    var bus by remember { mutableStateOf<com.example.busmate.model.BusModel?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(routeId) {
        viewModel.getBusByRouteId(routeId) { fetchedBus ->
            bus = fetchedBus
            loading = false
        }
    }
    Column(Modifier.padding(16.dp)) {
        Text("Bus Information", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator()
        } else if (bus != null) {
            Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(Modifier.padding(16.dp)) {
                    DetailText("Bus Number", bus!!.busNumber)
                    DetailText("License Plate", bus!!.licensePlate)
                    DetailText(label = " Bus Route ID", value = bus!!.routeId)
                    DetailText("Capacity", "${bus!!.capacity} Students")
                    DetailText("Maintenance", bus!!.maintenanceStatus)
                    val driverName = bus!!.driver?.let { "${it.firstName} ${it.lastName}" } ?: "Not Assigned"
                    DetailText(label = "Driver Name", value = driverName)
                }
            }
        } else {
            Text("Details not found for route: $routeId")
        }
    }
}
@Composable
fun DetailText(label: String, value: String) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Divider(thickness = 0.5.dp, color = Color.LightGray)
    }
}