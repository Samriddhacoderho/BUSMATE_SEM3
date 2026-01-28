package com.example.busmate.view.parent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
        topBar = {
            TopAppBar(
                title = { Text("Child Attendance") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Date Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(
                    text = "Showing attendance for: $selectedDate",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (attendanceRecords.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No records found for this date.")
                        Text(
                            "Check if the date is correct.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = childName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            records.forEach { record ->
                val route = record["busRouteId"] as? String ?: "N/A"
                val status = record["status"] as? String ?: "No Record"

                // ✅ Updated: Different colors for Present, Absent, and No Record
                val statusColor = when (status) {
                    "Present" -> Color(0xFF2E7D32)  // Green
                    "Absent" -> Color(0xFFC62828)   // Red
                    else -> Color(0xFFFF9800)       // Orange for "No Record"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Route: $route", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = status,
                        color = statusColor,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
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