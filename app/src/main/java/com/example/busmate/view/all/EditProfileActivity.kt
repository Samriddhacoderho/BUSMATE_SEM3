package com.example.busmate.view.all

import android.app.Activity
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
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
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.view.ImageUtils
import com.example.busmate.view.dashboard.ButtonGray
import com.example.busmate.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class EditProfileActivity : ComponentActivity() {
    private lateinit var viewModel: UserViewModel
    private lateinit var imageUtils: ImageUtils // Initialized here as per teacher's guide

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = UserRepositoryImpl()
        viewModel = UserViewModel(repository)

        // 1. Initialize ImageUtils
        imageUtils = ImageUtils(this, this)

        setContent {
            val context = LocalContext.current

            // 2. Register the launchers to handle the image result
            LaunchedEffect(Unit) {
                imageUtils.registerLaunchers { uri ->
                    uri?.let {
                        // 3. Trigger the Cloudinary upload in the ViewModel
                        viewModel.uploadProfileImage(context, it)
                    }
                }
            }

            EditProfileScreen(
                viewModel = viewModel,
                onPickImage = { imageUtils.launchImagePicker() } // Pass click action to UI
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(viewModel: UserViewModel, onPickImage: () -> Unit) {
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var firstName by remember { mutableStateOf(user?.firstName ?: "") }
    var lastName by remember { mutableStateOf(user?.lastName ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: return

    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    LaunchedEffect(user) {
        user?.let {
            firstName = it.firstName
            lastName = it.lastName
            phone = it.phone
        }
    }

    LaunchedEffect(message) {
        if (message.isNotEmpty() && !message.contains("Updating") && !message.contains("Loading")) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                // This check determines if the background is Green or Red
                val isSuccess = message.contains("successfully", ignoreCase = true) ||
                        message.contains("Updated", ignoreCase = true) ||
                        message.contains("Loaded", ignoreCase = true)
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isSuccess) Color(0xFF4CAF50) else Color(0xFF4CAF50),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.onBackPressed() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F7F7)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Image Section with Gallery Picker
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable { onPickImage() } // 4. Click to open gallery via ImageUtils
                ) {
                    // 5. Use AsyncImage to display the Cloudinary URL
                    if (!user?.profileImage.isNullOrEmpty()) {
                        AsyncImage(
                            model = user?.profileImage,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(ButtonGray),
                            tint = Color.LightGray
                        )
                    }
                    // Camera Icon Badge
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color.LightGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Change Picture",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { viewModel.updateUserProfile(firstName, lastName, phone) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2854D8))
            ) {
                Text("Save", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}


