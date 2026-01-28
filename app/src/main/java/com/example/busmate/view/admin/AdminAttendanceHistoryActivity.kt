package com.example.busmate.view.admin

import android.annotation.SuppressLint
import android.app.Activity
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

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAttendanceHistoryScreen() {

    val activity = LocalContext.current as Activity

    val viewModel = remember {
        AttendanceViewModel(
            BusRepositoryImpl(),
            AttendanceRepositoryImpl()
        )
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

    Box(modifier = Modifier.fillMaxSize()) {

        /* ---------- HEADER ---------- */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.32f)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            BusMateBlue,
                            BusMateBlue.copy(alpha = 0.92f)
                        )
                    )
                )
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {

            /* BACK BUTTON */
            IconButton(
                onClick = { activity.finish() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Attendance History",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Track attendance by date and bus",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )
        }

        /* ---------- FLOATING CARD ---------- */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-40).dp),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                /* ---------- FILTER SECTION ---------- */
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFF6F8FE),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    /* DATE PICKER */
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    ) {
                        OutlinedTextField(
                            value = selectedDateText,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Date") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = PrimaryBlue
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor = Color(0xFFFDFEFF),
                                disabledTextColor = Color.Black,
                                disabledBorderColor = PrimaryBlue.copy(alpha = 0.4f),
                                disabledLabelColor = PrimaryBlue,
                                disabledLeadingIconColor = PrimaryBlue
                            )
                        )
                    }

                    /* BUS DROPDOWN */
                    ExposedDropdownMenuBox(
                        expanded = isBusDropdownExpanded,
                        onExpandedChange = {
                            isBusDropdownExpanded = !isBusDropdownExpanded
                        }
                    ) {
                        OutlinedTextField(
                            value = selectedBus?.let {
                                "Bus ${it.busNumber} • Route ${it.routeId}"
                            } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Bus") },
                            placeholder = { Text("Select Bus") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = isBusDropdownExpanded
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = PrimaryBlue.copy(alpha = 0.4f),
                                focusedContainerColor = Color(0xFFFDFEFF),
                                unfocusedContainerColor = Color(0xFFFDFEFF),
                                focusedLabelColor = PrimaryBlue
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = isBusDropdownExpanded,
                            onDismissRequest = {
                                isBusDropdownExpanded = false
                            }
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
                                            viewModel.loadHistory(
                                                formattedDateForFirebase,
                                                bus.routeId
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                /* ---------- STUDENT LIST (SCROLLABLE) ---------- */
                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = PrimaryBlue,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Fetching attendance…", color = Color.Gray)
                        }
                    }

                    history.isEmpty() &&
                            selectedBus != null &&
                            formattedDateForFirebase.isNotEmpty() -> {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No attendance records",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try selecting another date or bus",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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

    /* ---------- DATE PICKER ---------- */
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
                            viewModel.loadHistory(
                                formattedDateForFirebase,
                                it.routeId
                            )
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

/* ---------- HISTORY CARD ---------- */
@Composable
fun HistoryCard(record: Map<String, Any?>) {

    val name = record["childName"] as? String ?: "Unknown"
    val status = record["status"] as? String ?: "Unknown"
    val isPresent = status == "Present"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFDFEFF)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPresent) "Present" else "Absent",
                    fontSize = 13.sp,
                    color = if (isPresent)
                        Color(0xFF2E7D32)
                    else
                        Color(0xFFC62828)
                )
            }

            Surface(
                color = if (isPresent)
                    Color(0xFFE8F5E9)
                else
                    Color(0xFFFDECEA),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = if (isPresent) "Present" else "Absent",
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 6.dp
                    ),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isPresent)
                        Color(0xFF2E7D32)
                    else
                        Color(0xFFC62828)
                )
            }
        }
    }
}
