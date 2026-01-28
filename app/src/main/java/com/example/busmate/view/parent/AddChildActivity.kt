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
import androidx.compose.ui.graphics.Brush
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
import com.example.busmate.viewmodel.ChildViewModel

/* ---------------- COLORS ---------------- */

private val PrimaryBlue = Color(0xFF2567E8)
private val SecondaryBlue = Color(0xFF1D4ED8)
private val ScreenBg = Color(0xFFF6F8FC)
private val CardBg = Color.White

/* ---------------- ACTIVITY ---------------- */

class AddChildActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

/* ---------------- SCREEN ---------------- */

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
        firstName,
        lastName,
        studentId,
        parentSchoolId,
        busRouteId,
        pickUpLocation,
        dropOffLocation,
        pLat,
        pLng,
        dLat,
        dLng,
        imageUri
    ) {
        mutableStateOf(
            firstName.isNotBlank() &&
                    lastName.isNotBlank() &&
                    studentId.isNotBlank() &&
                    parentSchoolId.isNotBlank() &&
                    busRouteId.isNotBlank() &&
                    pickUpLocation.isNotBlank() &&
                    dropOffLocation.isNotBlank() &&
                    pLat != 0.0 &&
                    pLng != 0.0 &&
                    dLat != 0.0 &&
                    dLng != 0.0 &&
                    imageUri != null
        )
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
    val message by viewModel.message.collectAsState() // Add this line

    LaunchedEffect(Unit) {
        viewModel.fetchAvailableRoutes()
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Toast.makeText(context, "Child added and linked to parent!", Toast.LENGTH_LONG).show()
            (context as? Activity)?.finish()
        }
    }

    Scaffold(containerColor = ScreenBg) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            /* ---------- HEADER ---------- */

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(PrimaryBlue, SecondaryBlue)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            Icons.Default.ChildCare,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(28.dp)
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            "Admin Panel",
                            color = Color.White.copy(0.85f),
                            fontSize = 13.sp
                        )
                        Text(
                            "Add Child Details",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            /* ---------- FORM CARD ---------- */

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    /* ---------- IMAGE PICKER ---------- */

                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .border(
                                    3.dp,
                                    if (imageUri == null) Color.Red else PrimaryBlue,
                                    CircleShape
                                )
                                .background(Color(0xFFEAF0FF))
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
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = PrimaryBlue,
                            shadowElevation = 6.dp,
                            modifier = Modifier.offset((-6).dp, (-6).dp)
                        ) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    /* ---------- FORM FIELDS ---------- */

                    AdminTextField(
                        label = "Parent School ID",
                        value = parentSchoolId,
                        icon = Icons.Default.Badge,
                        readOnly = prefilledParentId.isNotEmpty()
                    ) { parentSchoolId = it }

                    AdminTextField("First Name", firstName, Icons.Default.Person) { firstName = it }
                    AdminTextField("Last Name", lastName, Icons.Default.Person) { lastName = it }

                    AdminTextField(
                        label = "Student ID",
                        value = studentId,
                        icon = Icons.Default.Numbers,
                        readOnly = prefilledStudentId.isNotEmpty()
                    ) { studentId = it }

                    Spacer(Modifier.height(12.dp))

                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
                        OutlinedTextField(
                            value = busRouteId,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assign Bus Route") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
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

                    MapLocationField("Pickup Location", pickUpLocation) {
                        pickUpLauncher.launch(Intent(context, MapPickerActivity::class.java))
                    }

                    MapLocationField("Dropoff Location", dropOffLocation) {
                        dropOffLauncher.launch(Intent(context, MapPickerActivity::class.java))
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val child = ChildModel(
                                firstName,
                                lastName,
                                studentId,
                                busRouteId,
                                pickUpLocation,
                                dropOffLocation,
                                pLat,
                                pLng,
                                dLat,
                                dLng
                            )
                            viewModel.adminAddChild(
                                parentSchoolId,
                                context,
                                imageUri,
                                child
                            )
                        },
                        // DISABLE button if the message is "Processing..." or "Uploading data..."
                        enabled = isFormValid && message != "Processing..." && message != "Uploading data...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        elevation = ButtonDefaults.buttonElevation(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.White
                        )
                    ) {
                        // SHOW SPINNER if loading
                        if (message == "Processing..." || message == "Uploading data...") {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Adding Child...")
                        } else {
                            Text(
                                "Confirm & Add Child",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- REUSABLE COMPONENTS ---------------- */

@Composable
fun AdminTextField(
    label: String,
    value: String,
    icon: ImageVector,
    readOnly: Boolean = false,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        readOnly = readOnly,
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, null, tint = PrimaryBlue)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = PrimaryBlue.copy(alpha = 0.4f),
            focusedLabelColor = PrimaryBlue,
            disabledBorderColor = PrimaryBlue.copy(alpha = 0.2f)
        )
    )
}

@Composable
fun MapLocationField(
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
            Icon(Icons.Default.Map, null, tint = PrimaryBlue)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        enabled = false
    )
}
