package com.example.busmate.view.parent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.busmate.data.ChildRepositoryImpl
import com.example.busmate.model.ChildModel
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.viewmodel.ChildViewModel

class AddChildActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefilledStudentId = intent.getStringExtra("STUDENT_ID") ?: ""
        val prefilledParentId = intent.getStringExtra("PARENT_ID") ?: ""

        val repository = ChildRepositoryImpl()
        val viewModel = ChildViewModel(repository)

        setContent {
            AddChildScreen(
                viewModel = viewModel,
                prefilledStudentId = prefilledStudentId,
                prefilledParentId = prefilledParentId,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildScreen(
    viewModel: ChildViewModel,
    prefilledStudentId: String,
    prefilledParentId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // ---------------- FORM STATE ----------------
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf(prefilledStudentId) }
    var parentSchoolId by remember { mutableStateOf(prefilledParentId) }
    var busRouteId by remember { mutableStateOf("") }

    var pickUpLocation by remember { mutableStateOf("") }
    var dropOffLocation by remember { mutableStateOf("") }

    var pLat by remember { mutableDoubleStateOf(0.0) }
    var pLng by remember { mutableDoubleStateOf(0.0) }
    var dLat by remember { mutableDoubleStateOf(0.0) }
    var dLng by remember { mutableDoubleStateOf(0.0) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // ---------------- MAP PICKERS (FIX) ----------------
    val pickUpLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pickUpLocation = result.data?.getStringExtra("address") ?: ""
            pLat = result.data?.getDoubleExtra("lat", 0.0) ?: 0.0
            pLng = result.data?.getDoubleExtra("lng", 0.0) ?: 0.0
        }
    }

    val dropOffLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            dropOffLocation = result.data?.getStringExtra("address") ?: ""
            dLat = result.data?.getDoubleExtra("lat", 0.0) ?: 0.0
            dLng = result.data?.getDoubleExtra("lng", 0.0) ?: 0.0
        }
    }

    // ---------------- IMAGE PICKER ----------------
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri = it }

    // ---------------- OBSERVABLES ----------------
    val message by viewModel.message.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val routes by viewModel.availableRoutes.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAvailableRoutes()
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Toast.makeText(context, "Child added and linked to parent!", Toast.LENGTH_LONG).show()
            (context as? Activity)?.finish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin: Add Child Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BusMateBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------------- IMAGE PICKER UI ----------------
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .border(2.dp, BusMateBlue, CircleShape)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------- PARENT ID ----------------
            OutlinedTextField(
                value = parentSchoolId,
                onValueChange = { parentSchoolId = it },
                label = { Text("Parent School ID") },
                readOnly = prefilledParentId.isNotEmpty(),
                leadingIcon = { Icon(Icons.Default.Badge, null, tint = BusMateBlue) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField("First Name", firstName) { firstName = it }
            CustomTextField("Last Name", lastName) { lastName = it }

            OutlinedTextField(
                value = studentId,
                onValueChange = { studentId = it },
                label = { Text("Student ID") },
                readOnly = prefilledStudentId.isNotEmpty(),
                leadingIcon = { Icon(Icons.Default.Numbers, null, tint = BusMateBlue) },
                modifier = Modifier.fillMaxWidth()
            )

            // ---------------- ROUTE DROPDOWN ----------------
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
                OutlinedTextField(
                    value = busRouteId,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Assign Bus Route") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded, { expanded = false }) {
                    routes.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                busRouteId = it
                                expanded = false
                            }
                        )
                    }
                }
            }

            // ---------------- MAP PICKERS (WORKING) ----------------
            MapLocationField("Pickup Location", pickUpLocation) {
                pickUpLauncher.launch(Intent(context, MapPickerActivity::class.java))
            }

            MapLocationField("Dropoff Location", dropOffLocation) {
                dropOffLauncher.launch(Intent(context, MapPickerActivity::class.java))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val child = ChildModel(
                        firstName = firstName,
                        lastName = lastName,
                        studentId = studentId,
                        busRouteId = busRouteId,
                        pickUpLocation = pickUpLocation,
                        dropOffLocation = dropOffLocation,
                        pickUpLat = pLat,
                        pickUpLng = pLng,
                        dropOffLat = dLat,
                        dropOffLng = dLng
                    )
                    viewModel.adminAddChild(parentSchoolId, context, imageUri, child)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BusMateBlue)
            ) {
                Text("Confirm & Add Child", fontSize = 18.sp)
            }
        }
    }
}

// ---------------- REUSABLE COMPONENTS ----------------

@Composable
fun CustomTextField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Person, null, tint = BusMateBlue) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}

@Composable
fun MapLocationField(label: String, value: String, onClick: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.Map, null, tint = BusMateBlue)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.Black,
            disabledBorderColor = Color.Gray
        )
    )
}
