package com.example.busmate.view.parent

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busmate.viewmodel.AttendanceViewModel
import com.example.busmate.ui.theme.BusMateTheme
import java.text.SimpleDateFormat
import java.util.*

class ParentAttendanceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get the UID passed from Dashboard. If null, we can't fetch data.
        val parentUid = intent.getStringExtra("PARENT_UID") ?: ""

        setContent {
            // Observe SharedPreferences changes for dark mode
            val context = androidx.compose.ui.platform.LocalContext.current
            val sharedPrefs = remember {
                context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            }
            var themeChanged by remember { mutableStateOf(sharedPrefs.getInt("dark_mode_pref", 0)) }

            androidx.compose.runtime.DisposableEffect(Unit) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "dark_mode_pref") {
                        themeChanged = sharedPrefs.getInt("dark_mode_pref", 0)
                    }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)

                onDispose {
                    sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            key(themeChanged) {
                BusMateTheme {
                    if (parentUid.isEmpty()) {
                        ErrorScreen("User ID not found. Please log in again.")
                    } else {
                        ParentAttendanceScreen(
                            parentUid = parentUid,
                            onBackClick = { finish() }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentAttendanceScreen(
    parentUid: String,
    viewModel: AttendanceViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val attendanceRecords by viewModel.parentAttendance.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Match this format to how your Driver app saves data (e.g., "yyyy-MM-dd")
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var selectedDate by remember { mutableStateOf(dateFormatter.format(Date())) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Initial load
    LaunchedEffect(selectedDate) {
        viewModel.loadParentAttendance(parentUid, selectedDate)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Blue Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF2854D8)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(40.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Child Attendance",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Date Selection Button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date", modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(selectedDate, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }

            // Main Content Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .offset(y = (-40).dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF2854D8)
                        )
                    } else if (attendanceRecords.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No records found for this date.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Try selecting a different date.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Grouping by student name in case one child has multiple route entries
                            val grouped = attendanceRecords.groupBy { it["childName"] as? String ?: "Unknown" }

                            items(grouped.keys.toList()) { childName ->
                                val recordsForChild = grouped[childName] ?: emptyList()
                                ParentAttendanceCard(childName, recordsForChild)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        AttendanceDatePicker(
            onDateSelected = { millis ->
                selectedDate = dateFormatter.format(Date(millis))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun ParentAttendanceCard(childName: String, records: List<Map<String, Any?>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = childName,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF2854D8)
            )

            Spacer(modifier = Modifier.height(12.dp))

            records.forEach { record ->
                val route = record["busRouteId"] as? String ?: "N/A"
                val status = record["status"] as? String ?: "No Record"

                // Different colors for Present, Absent, and No Record
                val statusColor = when (status) {
                    "Present" -> Color(0xFF2E7D32)  // Green
                    "Absent" -> Color(0xFFC62828)   // Red
                    else -> Color(0xFFFF9800)       // Orange for "No Record"
                }

                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Route ID",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = route,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = statusColor.copy(alpha = 0.1f),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = status,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }

                    if (records.indexOf(record) < records.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 8.dp),
                            thickness = 0.5.dp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceDatePicker(onDateSelected: (Long) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onDateSelected(it) }
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}