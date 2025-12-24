package com.example.busmate.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busmate.model.ChildModel
import com.example.busmate.viewmodel.AttendanceViewModel

class AttendanceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 🔹 Retrieve the UID using the EXACT key from ParentDashboard
        val driverUid = intent.getStringExtra("EXTRA_DRIVER_UID") ?: ""
        Log.d("AttendanceFlow", "Activity Started with UID: $driverUid")

        setContent {
            AttendanceScreen(
                driverUid = driverUid,
                onBackClick = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    driverUid: String,
    onBackClick: () -> Unit,
    viewModel: AttendanceViewModel = viewModel()
) {
    val children by viewModel.children.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(driverUid) {
        if (driverUid.isNotEmpty()) {
            viewModel.loadAttendanceList(driverUid)
        } else {
            Log.e("AttendanceFlow", "Driver UID is EMPTY")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Student Attendance") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (children.isEmpty()) {
                Text(
                    "No students found for this route.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(children) { child ->
                        StudentAttendanceCard(child)
                    }
                }
            }
        }
    }
}

@Composable
fun StudentAttendanceCard(child: ChildModel) {
    var isPresent by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(child.firstName.take(1).uppercase(), fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text("${child.firstName} ${child.lastName}", fontWeight = FontWeight.Bold)
                Text("ID: ${child.studentId}", fontSize = 12.sp, color = Color.Gray)
            }

            Checkbox(checked = isPresent, onCheckedChange = { isPresent = it })
        }
    }
}