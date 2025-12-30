package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.busmate.data.AttendanceRepositoryImpl
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.model.BusModel
import com.example.busmate.viewmodel.AttendanceViewModel

class AdminAttendanceHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminAttendanceHistoryScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAttendanceHistoryScreen() {
    val viewModel = remember { AttendanceViewModel(BusRepositoryImpl(), AttendanceRepositoryImpl()) }
    val buses by viewModel.allBuses.collectAsState()
    val history by viewModel.attendanceHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedBus by remember { mutableStateOf<BusModel?>(null) }
    var selectedDateText by remember { mutableStateOf("Select Date") }
    var formattedDateForFirebase by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var isBusDropdownExpanded by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    LaunchedEffect(Unit) {
        viewModel.fetchAllBusesForAdmin()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Attendance History", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = selectedDateText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Date") },
            leadingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = isBusDropdownExpanded,
            onExpandedChange = { isBusDropdownExpanded = !isBusDropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedBus?.let { "Bus ${it.busNumber} (Route ${it.routeId})" } ?: "Select Bus",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Bus") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBusDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = isBusDropdownExpanded,
                onDismissRequest = { isBusDropdownExpanded = false }
            ) {
                buses.forEach { bus ->
                    DropdownMenuItem(
                        text = { Text("Bus ${bus.busNumber} - Route ${bus.routeId}") },
                        onClick = {
                            selectedBus = bus
                            isBusDropdownExpanded = false
                            if (formattedDateForFirebase.isNotEmpty()) {
                                // 🔹 FIXED: Use bus.routeId instead of it.uid
                                viewModel.loadHistory(formattedDateForFirebase, bus.routeId)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (history.isEmpty() && selectedBus != null && formattedDateForFirebase.isNotEmpty()) {
            Text("No records found for this date.", modifier = Modifier.align(Alignment.CenterHorizontally), color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history) { record ->
                    HistoryCard(record)
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val date = java.util.Date(selectedMillis)
                        val uiFormatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                        selectedDateText = uiFormatter.format(date)

                        val fbFormatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        formattedDateForFirebase = fbFormatter.format(date)

                        // 🔹 FIXED: Use selectedBus?.routeId instead of it.uid
                        selectedBus?.let { bus ->
                            viewModel.loadHistory(formattedDateForFirebase, bus.routeId)
                        }
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun HistoryCard(record: Map<String, Any?>) {
    val name = record["childName"] as? String ?: "Unknown"
    val status = record["status"] as? String ?: "Unknown"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (status == "Present") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = status,
                color = if (status == "Present") Color(0xFF2E7D32) else Color(0xFFC62828),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}