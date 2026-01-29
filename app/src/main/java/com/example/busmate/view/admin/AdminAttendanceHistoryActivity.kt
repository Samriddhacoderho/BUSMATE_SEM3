package com.example.busmate.view.admin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.AttendanceRepositoryImpl
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.model.BusModel
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.ui.theme.isDarkMode
import com.example.busmate.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.*

class AdminAttendanceHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current

            // --- DARK MODE OBSERVATION ---
            val sharedPrefs = remember {
                context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            }
            var themeUpdateTrigger by remember { mutableIntStateOf(0) }

            DisposableEffect(Unit) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "dark_mode_pref") {
                        themeUpdateTrigger++
                    }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            key(themeUpdateTrigger) {
                BusMateTheme(darkTheme = isDarkMode()) {
                    AdminAttendanceHistoryScreen()
                }
            }
        }
    }
}

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAttendanceHistoryScreen() {
    val activity = LocalContext.current as Activity
    val busMateBlue = Color(0xFF2854D8) // Hardcoded Blue to match app theme

    val viewModel = remember {
        AttendanceViewModel(BusRepositoryImpl(), AttendanceRepositoryImpl())
    }

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background // Adapts to Dark Mode
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            /* ---------- HEADER (Always Blue) ---------- */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .background(
                        Brush.verticalGradient(
                            listOf(busMateBlue, busMateBlue.copy(alpha = 0.9f))
                        )
                    )
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { activity.finish() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Attendance History", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Track attendance records", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }

            /* ---------- CONTENT CARD (Adaptive Background) ---------- */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-40).dp),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface // Turns Dark in Dark Mode
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    /* FILTER SECTION */
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Date Field
                        OutlinedTextField(
                            value = selectedDateText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Date") },
                            leadingIcon = { Icon(Icons.Default.DateRange, null, tint = busMateBlue) },
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                            enabled = false,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = busMateBlue.copy(alpha = 0.5f),
                                disabledLabelColor = busMateBlue
                            )
                        )

                        // Bus Dropdown
                        ExposedDropdownMenuBox(
                            expanded = isBusDropdownExpanded,
                            onExpandedChange = { isBusDropdownExpanded = !isBusDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedBus?.let { "Bus ${it.busNumber}" } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Bus") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isBusDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = busMateBlue,
                                    unfocusedBorderColor = busMateBlue.copy(alpha = 0.5f)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = isBusDropdownExpanded,
                                onDismissRequest = { isBusDropdownExpanded = false }
                            ) {
                                buses.forEach { bus ->
                                    DropdownMenuItem(
                                        text = { Text("Bus ${bus.busNumber}") },
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
                    }

                    /* LIST SECTION */
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = busMateBlue)
                    } else if (history.isEmpty() && selectedBus != null) {
                        Text("No records found", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(history) { record ->
                                HistoryCard(record)
                            }
                        }
                    }
                }
            }
        }
    }

    /* DATE PICKER DIALOG */
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        selectedDateText = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
                        formattedDateForFirebase = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                        selectedBus?.let { viewModel.loadHistory(formattedDateForFirebase, it.routeId) }
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun HistoryCard(record: Map<String, Any?>) {
    val name = record["childName"] as? String ?: "Unknown"
    val status = record["status"] as? String ?: "Unknown"
    val isPresent = status == "Present"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = status,
                    fontSize = 12.sp,
                    color = if (isPresent) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            // Status Chip
            Surface(
                color = if (isPresent) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFF44336).copy(alpha = 0.1f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPresent) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }
    }
}