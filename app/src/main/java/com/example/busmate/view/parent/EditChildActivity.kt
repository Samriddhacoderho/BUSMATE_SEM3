package com.example.busmate.view.parent

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
    // UI Colors from EditStudentActivity
    val busMateBlue = Color(0xFF2567E8)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel = remember { ChildViewModel(ChildRepositoryImpl()) }

    val message by viewModel.message.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    var firstName by remember { mutableStateOf(initialChild.firstName) }
    var lastName by remember { mutableStateOf(initialChild.lastName) }
    var busRouteId by remember { mutableStateOf(initialChild.busRouteId) }
    var pickUpAddr by remember { mutableStateOf(initialChild.pickUpLocation) }
    var dropOffAddr by remember { mutableStateOf(initialChild.dropOffLocation) }

    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

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
                .background(Color(0xFFF8F9FA)) // Light gray background from EditStudent
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Photo Section (Matching EditStudent UI)
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9ECEF))
                    .border(2.dp, busMateBlue, CircleShape)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    newImageUri != null -> {
                        AsyncImage(
                            model = newImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    initialChild.profileImage.isNotEmpty() -> {
                        AsyncImage(
                            model = initialChild.profileImage,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.LightGray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            // Input Fields using the EditStudent style
            EditField("First Name", firstName) { firstName = it }
            EditField("Last Name", lastName) { lastName = it }
            EditField("Route ID", busRouteId) { busRouteId = it }
            EditField("Pick-up Address", pickUpAddr) { pickUpAddr = it }
            EditField("Drop-off Address", dropOffAddr) { dropOffAddr = it }
            Spacer(modifier = Modifier.height(30.dp))
            // Save Button using EditStudent style
            Button(
                onClick = {
                    scope.launch {
                        isSaving = true
                        val baseUpdated = initialChild.copy(
                            firstName = firstName,
                            lastName = lastName,
                            busRouteId = busRouteId,
                            pickUpLocation = pickUpAddr,
                            dropOffLocation = dropOffAddr
                        )

                        if (newImageUri != null) {
                            ChildRepositoryImpl().uploadChildImage(context, newImageUri!!) { url ->
                                if (url != null) {
                                    viewModel.updateChild(baseUpdated.copy(profileImage = url))
                                } else {
                                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                                    isSaving = false
                                }
                            }
                        } else {
                            viewModel.updateChild(baseUpdated)
                        }
                    }
                },
                enabled = !isSaving && firstName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
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