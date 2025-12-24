package com.example.busmate.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    // This state tracks which children are currently checked
    val checkedChildren = remember { mutableStateListOf<ChildModel>() }

    LaunchedEffect(driverUid) {
        if (driverUid.isNotEmpty()) {
            viewModel.loadAttendanceList(driverUid)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daily Attendance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Submit Button
            Surface(tonalElevation = 8.dp) {
                Button(
                    onClick = {
                        if (checkedChildren.isEmpty()) {
                            Toast.makeText(context, "Please select students", Toast.LENGTH_SHORT).show()
                        } else {
                            // This now matches the ViewModel function with 3 arguments:
                            // 1. driverUid (String)
                            // 2. checkedChildren.toList() (List)
                            // 3. { success -> ... } (The lambda callback)
                            viewModel.submitAttendance(driverUid, checkedChildren.toList()) { success ->
                                if (success) {
                                    Toast.makeText(context, "Attendance saved!", Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                } else {
                                    Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Attendance (${checkedChildren.size})", fontSize = 16.sp)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (children.isEmpty()) {
                Text(
                    "No students assigned to this route.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(children) { child ->
                        val isChecked = checkedChildren.contains(child)
                        StudentAttendanceCard(
                            child = child,
                            isChecked = isChecked,
                            onCheckChanged = { checked ->
                                if (checked) checkedChildren.add(child)
                                else checkedChildren.remove(child)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentAttendanceCard(
    child: ChildModel,
    isChecked: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isChecked) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Circle
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = child.firstName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = "${child.firstName} ${child.lastName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "ID: ${child.studentId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Checkbox(
                checked = isChecked,
                onCheckedChange = { onCheckChanged(it) }
            )
        }
    }
}