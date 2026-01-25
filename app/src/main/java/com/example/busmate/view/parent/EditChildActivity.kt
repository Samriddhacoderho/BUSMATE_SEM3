package com.example.busmate.view.parent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch

class EditChildActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Retrieve child data from Intent
        val child = intent.getParcelableExtra<ChildModel>("CHILD_DATA") ?: return finish()

        setContent {
            BusMateTheme {
                EditChildScreen(initialChild = child, onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChildScreen(initialChild: ChildModel, onBackClick: () -> Unit) {
    val busMateBlue = Color(0xFF2567E8)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel = remember { ChildViewModel(ChildRepositoryImpl()) }

    val message by viewModel.message.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    // Form States
    var firstName by remember { mutableStateOf(initialChild.firstName) }
    var lastName by remember { mutableStateOf(initialChild.lastName) }
    var busRouteId by remember { mutableStateOf(initialChild.busRouteId) }

    // Location States (Lat/Lng from MapPicker)
    var pickUpAddr by remember { mutableStateOf(initialChild.pickUpLocation) }
    var pickUpLat by remember { mutableStateOf(initialChild.pickUpLat) }
    var pickUpLng by remember { mutableStateOf(initialChild.pickUpLng) }

    var dropOffAddr by remember { mutableStateOf(initialChild.dropOffLocation) }
    var dropOffLat by remember { mutableStateOf(initialChild.dropOffLat) }
    var dropOffLng by remember { mutableStateOf(initialChild.dropOffLng) }

    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Map Pickers
    val pickUpLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pickUpLat = result.data?.getDoubleExtra("lat", 0.0) ?: 0.0
            pickUpLng = result.data?.getDoubleExtra("lng", 0.0) ?: 0.0
            pickUpAddr = result.data?.getStringExtra("address") ?: ""
        }
    }

    val dropOffLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            dropOffLat = result.data?.getDoubleExtra("lat", 0.0) ?: 0.0
            dropOffLng = result.data?.getDoubleExtra("lng", 0.0) ?: 0.0
            dropOffAddr = result.data?.getStringExtra("address") ?: ""
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        newImageUri = uri
    }

    LaunchedEffect(isSuccess) {
        if (message.isNotEmpty()) Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        if (isSuccess) {
            onBackClick()
            viewModel.resetStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Child Details", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Photo Section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9ECEF))
                    .border(2.dp, busMateBlue, CircleShape)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = newImageUri ?: initialChild.profileImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            EditField("First Name", firstName) { firstName = it }
            EditField("Last Name", lastName) { lastName = it }
            EditField("Route ID", busRouteId) { busRouteId = it }

            // Renamed Map Picker Fields to avoid conflicts
            ChildMapLocationField(label = "Pick-up Point", value = pickUpAddr) {
                pickUpLauncher.launch(Intent(context, MapPickerActivity::class.java))
            }
            ChildMapLocationField(label = "Drop-off Point", value = dropOffAddr) {
                dropOffLauncher.launch(Intent(context, MapPickerActivity::class.java))
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    scope.launch {
                        isSaving = true
                        val baseUpdated = initialChild.copy(
                            firstName = firstName,
                            lastName = lastName,
                            busRouteId = busRouteId,
                            pickUpLocation = pickUpAddr,
                            pickUpLat = pickUpLat,
                            pickUpLng = pickUpLng,
                            dropOffLocation = dropOffAddr,
                            dropOffLat = dropOffLat,
                            dropOffLng = dropOffLng
                        )

                        if (newImageUri != null) {
                            ChildRepositoryImpl().uploadChildImage(context, newImageUri!!) { url ->
                                if (url != null) {
                                    viewModel.updateChild(baseUpdated.copy(profileImage = url))
                                } else {
                                    isSaving = false
                                }
                            }
                        } else {
                            viewModel.updateChild(baseUpdated)
                        }
                    }
                },
                enabled = !isSaving && firstName.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = busMateBlue)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("SAVE CHANGES", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun EditField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    )
}
// Renamed from MapLocationField to ChildMapLocationField to prevent override errors
@Composable
fun ChildMapLocationField(label: String, value: String, onClick: () -> Unit) {
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
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.Black,
            disabledBorderColor = Color.Gray,
            disabledLabelColor = Color.DarkGray
        ),
        shape = RoundedCornerShape(12.dp)
    )
}