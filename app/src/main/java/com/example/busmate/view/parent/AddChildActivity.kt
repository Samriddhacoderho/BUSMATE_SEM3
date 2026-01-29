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
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.busmate.data.ChildRepositoryImpl
import com.example.busmate.model.ChildModel
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.viewmodel.ChildViewModel

class AddChildActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefilledStudentId = intent.getStringExtra("STUDENT_ID") ?: ""
        val prefilledParentId = intent.getStringExtra("PARENT_ID") ?: ""

        val repository = ChildRepositoryImpl()
        val viewModel = ChildViewModel(repository)

        setContent {
            // Theme wrapper to match EditChild design consistency
            BusMateTheme {
                AddChildScreen(
                    viewModel = viewModel,
                    prefilledStudentId = prefilledStudentId,
                    prefilledParentId = prefilledParentId,
                    onBack = { finish() }
                )
            }
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
    val busMateBlue = Color(0xFF2567E8)
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // ---------- FORM STATE ----------
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

    // ---------- VALIDATION ----------
    val isFormValid by remember(
        firstName, lastName, studentId, parentSchoolId, busRouteId,
        pickUpLocation, dropOffLocation, pLat, pLng, dLat, dLng, imageUri
    ) {
        derivedStateOf {
            firstName.isNotBlank() && lastName.isNotBlank() &&
                    studentId.isNotBlank() && parentSchoolId.isNotBlank() &&
                    busRouteId.isNotBlank() && pickUpLocation.isNotBlank() &&
                    pLat != 0.0 && imageUri != null
        }
    }

    // ---------- LAUNCHERS ----------
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

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri = it }

    // ---------- OBSERVERS ----------
    val isSuccess by viewModel.isSuccess.collectAsState()
    val routes by viewModel.availableRoutes.collectAsState()
    val message by viewModel.message.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAvailableRoutes()
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Toast.makeText(context, "Child added and linked to parent!", Toast.LENGTH_LONG).show()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Child Details", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = busMateBlue)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* ---------- IMAGE PICKER ---------- */
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, busMateBlue, CircleShape)
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
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = busMateBlue,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            /* ---------- FORM FIELDS ---------- */

            AdminTextField(
                label = "Parent School ID",
                value = parentSchoolId,
                readOnly = prefilledParentId.isNotEmpty()
            ) { parentSchoolId = it }

            AdminTextField("First Name", firstName) { firstName = it }
            AdminTextField("Last Name", lastName) { lastName = it }

            AdminTextField(
                label = "Student ID",
                value = studentId,
                readOnly = prefilledStudentId.isNotEmpty()
            ) { studentId = it }

            Spacer(Modifier.height(8.dp))

            // BUS ROUTE DROPDOWN (Styled to match EditField)
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = busRouteId,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Assign Bus Route") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded, { expanded = false }) {
                    routes.forEach { route ->
                        DropdownMenuItem(
                            text = { Text(route) },
                            onClick = {
                                busRouteId = route
                                expanded = false
                            }
                        )
                    }
                }
            }

            // MAP PICKERS (Using the ChildMapLocationField style)
            AddChildMapLocationField("Pick-up Location", pickUpLocation) {
                pickUpLauncher.launch(Intent(context, MapPickerActivity::class.java))
            }

            AddChildMapLocationField("Drop-off Location", dropOffLocation) {
                dropOffLauncher.launch(Intent(context, MapPickerActivity::class.java))
            }

            Spacer(Modifier.height(30.dp))

            /* ---------- SUBMIT BUTTON ---------- */

            val isProcessing = message == "Processing..." || message == "Uploading data..."

            Button(
                onClick = {
                    val child = ChildModel(
                        firstName, lastName, studentId, busRouteId,
                        pickUpLocation, dropOffLocation, pLat, pLng, dLat, dLng
                    )
                    viewModel.adminAddChild(parentSchoolId, context, imageUri, child)
                },
                enabled = isFormValid && !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = busMateBlue,
                    disabledContainerColor = Color.Gray
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Adding...", fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        "CONFIRM & ADD CHILD",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/* ---------------- REUSABLE COMPONENTS (Styled like EditChild) ---------------- */

@Composable
fun AdminTextField(
    label: String,
    value: String,
    readOnly: Boolean = false,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        readOnly = readOnly,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2567E8),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun AddChildMapLocationField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF1976D2))
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    )
}