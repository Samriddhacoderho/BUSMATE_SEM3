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
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.ui.theme.isDarkMode
import com.example.busmate.view.ImageUtils
import com.example.busmate.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.testTag

class EditProfileActivity : ComponentActivity() {
    private lateinit var viewModel: UserViewModel
    private lateinit var imageUtils: ImageUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = UserRepositoryImpl()
        viewModel = UserViewModel(repository)
        imageUtils = ImageUtils(this, this)

        setContent {
            // APPLY THE THEME Logic
            BusMateTheme(darkTheme = isDarkMode()) {
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    imageUtils.registerLaunchers { uri ->
                        uri?.let {
                            viewModel.uploadProfileImage(context, it)
                        }
                    }
                }

                EditProfileScreen(
                    viewModel = viewModel,
                    onPickImage = { imageUtils.launchImagePicker() }
                )
            }
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
                val isSuccess = message.contains("successfully", ignoreCase = true) ||
                        message.contains("Updated", ignoreCase = true) ||
                        message.contains("Loaded", ignoreCase = true)
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.onBackPressed() },modifier = Modifier.testTag("backButton")) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background // Changed from F7F7F7
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant) // Adaptive background
                        .clickable { onPickImage() }
                        .testTag("profileImageContainer")
                ) {
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
                                .clip(CircleShape),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.surface, CircleShape) // Adaptive badge
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Change Picture",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Changed from Color.White
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth()
                            .testTag("firstNameField"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth()
                            .testTag("lastNameField"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                            .testTag("phoneField"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { viewModel.updateUserProfile(firstName, lastName, phone) },
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("saveButton"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2854D8))
            ) {
                Text("Save", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}