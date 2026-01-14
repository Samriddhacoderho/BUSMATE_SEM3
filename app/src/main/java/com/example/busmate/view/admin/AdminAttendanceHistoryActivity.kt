package com.example.busmate.view.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.AttendanceRepositoryImpl
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.model.BusModel
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.*

private val PrimaryBlue = Color(0xFF2567E8)

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

    Box(modifier = Modifier.fillMaxSize()) {

        /* ---------------- TOP BLUE HEADER ---------------- */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.32f)
                .background(BusMateBlue),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Attendance History",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "View attendance records by date & bus",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }

        /* ---------------- WHITE CONTENT CARD ---------------- */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-32).dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {

                /* -------- DATE PICKER -------- */
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                /* -------- BUS DROPDOWN -------- */
                ExposedDropdownMenuBox(
                    expanded = isBusDropdownExpanded,
                    onExpandedChange = { isBusDropdownExpanded = !isBusDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedBus?.let {
                            "Bus ${it.busNumber} (Route ${it.routeId})"
                        } ?: "Select Bus",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Bus") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBusDropdownExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = isBusDropdownExpanded,
                        onDismissRequest = { isBusDropdownExpanded = false }
                    ) {
                        buses.forEach { bus ->
                            DropdownMenuItem(
                                text = {
                                    Text("Bus ${bus.busNumber} - Route ${bus.routeId}")
                                },
                                onClick = {
                                    selectedBus = bus
                                    isBusDropdownExpanded = false
                                    if (formattedDateForFirebase.isNotEmpty()) {
                                        viewModel.loadHistory(formattedDateForFirebase, bus.routeId)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                /* -------- CONTENT -------- */
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = PrimaryBlue
                        )
                    }

                    history.isEmpty() && selectedBus != null && formattedDateForFirebase.isNotEmpty() -> {
                        Text(
                            text = "No records found for this date.",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(history) { record ->
                                HistoryCard(record)
                            }
                        }
                    }
                }
            }
        }
    }

    /* ---------------- DATE PICKER DIALOG ---------------- */
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val date = Date(millis)
                        selectedDateText = SimpleDateFormat(
                            "MMM dd, yyyy",
                            Locale.getDefault()
                        ).format(date)

                        formattedDateForFirebase = SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).format(date)

                        selectedBus?.let {
                            viewModel.loadHistory(formattedDateForFirebase, it.routeId)
                        }
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/* ---------------- HISTORY CARD ---------------- */
@Composable
fun HistoryCard(record: Map<String, Any?>) {
    val name = record["childName"] as? String ?: "Unknown"
    val status = record["status"] as? String ?: "Unknown"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (status == "Present")
                Color(0xFFE8F5E9)
            else
                Color(0xFFFFEBEE)
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = status,
                fontWeight = FontWeight.Bold,
                color = if (status == "Present")
                    Color(0xFF2E7D32)
                else
                    Color(0xFFC62828)
            )
        }
    }
}
