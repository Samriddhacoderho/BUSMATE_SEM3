package com.example.busmate.view.admin

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
import androidx.compose.material.icons.filled.Phone
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
import com.example.busmate.ui.theme.isDarkMode
import com.example.busmate.view.parent.MapPickerActivity
import com.example.busmate.viewmodel.ChildViewModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class EditStudentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val child = intent.getParcelableExtra<ChildModel>("student_data")
        val viewModel = ChildViewModel(ChildRepositoryImpl())

        setContent {
            val context = LocalContext.current
            val sharedPrefs = remember {
                context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
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
                    if (child != null) {
                        EditStudentScreen(child, viewModel, onBack = { finish() })
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error: Student data not found")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStudentScreen(child: ChildModel, viewModel: ChildViewModel, onBack: () -> Unit) {
    // FORCE BLUE COLOR (Fixes the Purple issue)
    val busMateBlue = Color(0xFF2567E8)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var parentName by remember { mutableStateOf("Fetching...") }
    var parentPhone by remember { mutableStateOf("Fetching...") }

    var firstName by remember { mutableStateOf(child.firstName) }
    var lastName by remember { mutableStateOf(child.lastName) }
    var routeId by remember { mutableStateOf(child.busRouteId) }
    var pickUp by remember { mutableStateOf(child.pickUpLocation) }
    var dropOff by remember { mutableStateOf(child.dropOffLocation) }

    var pickUpLat by remember { mutableStateOf(child.pickUpLat) }
    var pickUpLng by remember { mutableStateOf(child.pickUpLng) }
    var dropOffLat by remember { mutableStateOf(child.dropOffLat) }
    var dropOffLng by remember { mutableStateOf(child.dropOffLng) }

    val message by viewModel.message.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    val pickUpLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pickUpLat = result.data?.getDoubleExtra("lat", 0.0) ?: 0.0
            pickUpLng = result.data?.getDoubleExtra("lng", 0.0) ?: 0.0
            pickUp = result.data?.getStringExtra("address") ?: ""
        }
    }

    val dropOffLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            dropOffLat = result.data?.getDoubleExtra("lat", 0.0) ?: 0.0
            dropOffLng = result.data?.getDoubleExtra("lng", 0.0) ?: 0.0
            dropOff = result.data?.getStringExtra("address") ?: ""
        }
    }

    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        newImageUri = uri
    }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(child.studentId) {
        val db = FirebaseDatabase.getInstance()
        db.getReference("studentIdIndex").child(child.studentId).get().addOnSuccessListener { index ->
            val parentUid = index.child("parentUid").getValue(String::class.java)
            if (parentUid != null) {
                db.getReference("users").child(parentUid).get().addOnSuccessListener { user ->
                    val fName = user.child("firstName").value ?: ""
                    val lName = user.child("lastName").value ?: ""
                    parentName = "$fName $lName"
                    parentPhone = user.child("phone").value.toString()
                }
            } else {
                parentName = "Not Found"
                parentPhone = "Not Found"
            }
        }
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Toast.makeText(context, "Student Updated!", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Student Details", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Student Profile Photo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, busMateBlue, CircleShape) // Applied Blue border
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = newImageUri ?: child.profileImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Text("Updating ID: ${child.studentId}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(20.dp))

            EditField("First Name", firstName) { firstName = it }
            EditField("Last Name", lastName) { lastName = it }
            EditField("Route ID", routeId) { routeId = it }

            MapLocationField(label = "Pick-up Point", value = pickUp, blueColor = busMateBlue) {
                pickUpLauncher.launch(Intent(context, MapPickerActivity::class.java))
            }
            MapLocationField(label = "Drop-off Point", value = dropOff, blueColor = busMateBlue) {
                dropOffLauncher.launch(Intent(context, MapPickerActivity::class.java))
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    scope.launch {
                        isSaving = true
                        if (pickUpLat != 0.0 && pickUpLng != 0.0 && dropOffLat != 0.0 && dropOffLng != 0.0) {
                            if (newImageUri != null) {
                                ChildRepositoryImpl().uploadChildImage(context, newImageUri!!) { imageUrl ->
                                    if (imageUrl != null) {
                                        viewModel.updateChild(child.copy(
                                            firstName = firstName, lastName = lastName, busRouteId = routeId,
                                            pickUpLocation = pickUp, dropOffLocation = dropOff,
                                            pickUpLat = pickUpLat, pickUpLng = pickUpLng,
                                            dropOffLat = dropOffLat, dropOffLng = dropOffLng, profileImage = imageUrl
                                        ))
                                    } else {
                                        Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                                        isSaving = false
                                    }
                                }
                            } else {
                                viewModel.updateChild(child.copy(
                                    firstName = firstName, lastName = lastName, busRouteId = routeId,
                                    pickUpLocation = pickUp, dropOffLocation = dropOff,
                                    pickUpLat = pickUpLat, pickUpLng = pickUpLng,
                                    dropOffLat = dropOffLat, dropOffLng = dropOffLng
                                ))
                            }
                        } else {
                            Toast.makeText(context, "Please select valid locations", Toast.LENGTH_SHORT).show()
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving && firstName.isNotBlank() && pickUp.isNotBlank() && dropOff.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = busMateBlue) // Applied Blue color
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("SAVE CHANGES", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }

            // Driver/Parent Section
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(20.dp))
            Text("Parent Contact Information", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            ReadOnlyField(label = "Parent Name", value = parentName, icon = Icons.Default.Person)
            ReadOnlyField(label = "Parent Phone", value = parentPhone, icon = Icons.Default.Phone)

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
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

@Composable
fun MapLocationField(label: String, value: String, blueColor: Color, onClick: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.Map, contentDescription = null, tint = blueColor)
            }
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
@Composable
fun ReadOnlyField(label: String, value: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}