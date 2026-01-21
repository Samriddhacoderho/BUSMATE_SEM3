package com.example.busmate.view.parent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// ASSUMPTION: You must import your concrete repository implementation here
import com.example.busmate.data.ChildRepositoryImpl
import coil3.compose.AsyncImage
import com.example.busmate.view.parent.MapPickerActivity
import com.example.busmate.viewmodel.ChildViewModel


// --- New Activity Definition ---
class AddChildActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = remember { ChildRepositoryImpl() }
            val childViewModel = remember { ChildViewModel(repository) }
            AddChildScreenUI(childViewModel)
        }
    }
}


@Composable
fun AddChildScreenUI(viewModel: ChildViewModel) {

    // 2. Context and State Setup
    val context = LocalContext.current
    val activity = context as Activity
    val message by viewModel.message.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var pickUpLocation by remember { mutableStateOf("") }
    var dropOffLocation by remember { mutableStateOf("") }
    var busRouteId by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var pickUpLat by remember { mutableStateOf(0.0) }
    var pickUpLng by remember { mutableStateOf(0.0) }
    var dropOffLat by remember { mutableStateOf(0.0) }
    var dropOffLng by remember { mutableStateOf(0.0) }

    // 1. Define Launchers for MapPickerActivity
    val pickUpLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pickUpLat = result.data?.getDoubleExtra("lat", 0.0) ?: 0.0
            pickUpLng = result.data?.getDoubleExtra("lng", 0.0) ?: 0.0
            pickUpLocation = result.data?.getStringExtra("address") ?: ""
        }
    }

    val dropOffLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            dropOffLat = result.data?.getDoubleExtra("lat", 0.0) ?: 0.0
            dropOffLng = result.data?.getDoubleExtra("lng", 0.0) ?: 0.0
            dropOffLocation = result.data?.getStringExtra("address") ?: ""
        }
    }

    // --- NEW IMAGE PICKER STATE ---
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }
    // ------------------------------

    val isLoading = message == "Loading" || message == "Uploading data..."

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val availableRoutes by viewModel.availableRoutes.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAvailableRoutes()
    }

    LaunchedEffect(message) {
        if (message.isNotEmpty() && message != "Loading" && message != "Uploading data...") {
            snackbarHostState.showSnackbar(message)
            if (isSuccess) activity.finish()
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (message.contains("successfully", true)) Color(0xFF4CAF50) else Color.Red,
                    contentColor = Color.White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF0F0F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF1976D2)) // BusMateBlue
                    .padding(top = 16.dp),
            ) {
                IconButton(
                    onClick = { activity.finish() },
                    modifier = Modifier.align(Alignment.TopStart).padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go Back", tint = Color.White)
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Enter Child Details", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    // --- IMAGE PICKER UI SECTION ---
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                            .border(2.dp, Color(0xFF1976D2), CircleShape)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color.Gray)
                                Text("Add Photo", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                    Text("Child Profile Photo", modifier = Modifier.padding(top = 8.dp), fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    // -------------------------------

                    AddChildInputField(firstName, { firstName = it }, "First Name", Icons.Default.Person)
                    AddChildInputField(lastName, { lastName = it }, "Last Name", Icons.Default.Person, isOptional = true)
                    AddChildInputField(studentId, { studentId = it }, "Student ID", Icons.Default.Badge, keyboardType = KeyboardType.Number)

                    @OptIn(ExperimentalMaterial3Api::class)
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = busRouteId,
                            onValueChange = {},
                            readOnly = true, // User must pick from the list
                            label = { Text("Select Bus Route *") },
                            leadingIcon = { Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = Color(0xFF1976D2)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            if (availableRoutes.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No routes available") },
                                    onClick = { expanded = false }
                                )
                            } else {
                                availableRoutes.forEach { route ->
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
                    }
//make static to dynamic option in dropdown option of route id
                    HorizontalDivider(Modifier.padding(vertical = 16.dp))
                    Text("Pickup/Dropoff Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start))

                    MapLocationField(label = "Choose Pickup Location", value = pickUpLocation) {
                        pickUpLauncher.launch(Intent(context, MapPickerActivity::class.java))
                    }
                    MapLocationField(label = "Choose Dropoff Location", value = dropOffLocation) {
                        dropOffLauncher.launch(Intent(context, MapPickerActivity::class.java))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 4. UI VALIDATION (RED TEXT) ABOVE BUTTON
                    val fieldsEmpty = firstName.isBlank() || studentId.isBlank() || busRouteId.isBlank() || pickUpLocation.isBlank() || dropOffLocation.isBlank()

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (fieldsEmpty) {
                            Text("Please fill all the fields", color = Color.Red, fontSize = 12.sp)
                        }
                        if (selectedImageUri == null) {
                            Text("Please upload child's photo", color = Color.Red, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.addChild(
                                context = context,
                                imageUri = selectedImageUri,
                                firstName = firstName,
                                lastName = lastName,
                                studentId = studentId,
                                busRouteId = busRouteId,
                                pickUpLocation = pickUpLocation,
                                dropOffLocation = dropOffLocation,
                                pLat = pickUpLat, // Use stored state
                                pLng = pickUpLng, // Use stored state
                                dLat = dropOffLat, // Use stored state
                                dLng = dropOffLng  // Use stored state
                            )
                        },
                        enabled = !isLoading && firstName.isNotBlank() && studentId.isNotBlank() &&
                                pickUpLocation.isNotBlank() && dropOffLocation.isNotBlank() &&
                                selectedImageUri != null,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("ADD CHILD", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Reusable Composable for Input Field (Moved outside to fix nesting issue)
@Composable
fun AddChildInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isOptional: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val BusMateBlue = Color(0xFF1976D2)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label + if (!isOptional) " *" else "") },
        leadingIcon = { Icon(icon, contentDescription = null, tint = BusMateBlue) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BusMateBlue,
            focusedLabelColor = BusMateBlue
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
fun MapLocationField(label: String, value: String, onClick: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label + " *") },
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF1976D2))
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        enabled = false, // Disables typing but allows the clickable modifier to work
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.Black,
            disabledBorderColor = Color.Gray,
            disabledLabelColor = Color.DarkGray
        )
    )
}