package com.example.busmate.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.view.ui.theme.BUSMATETheme
import com.example.busmate.R
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.ui.theme.BusMateOrange
import com.example.busmate.ui.theme.PrimaryBlue
import com.example.busmate.ui.theme.SignUpTitleColor
import com.example.busmate.viewmodel.UserViewModel
import kotlinx.coroutines.launch


class SignUpScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BUSMATETheme {
                val repo= UserRepositoryImpl()
                val viewModel= UserViewModel(repo)
                SignUpScreenUI(viewModel)
            }
        }
    }
}

@Composable
fun SignUpScreenUI(viewModel: UserViewModel) {
    // State variables for all input fields
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var schoolId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // InteractionSources to track focus state for conditional labels
    val passwordInteractionSource = remember { MutableInteractionSource() }
    val isPasswordFocused by passwordInteractionSource.collectIsFocusedAsState()
    val confirmPasswordInteractionSource = remember { MutableInteractionSource() }
    val isConfirmPasswordFocused by confirmPasswordInteractionSource.collectIsFocusedAsState()
    val context= LocalContext.current
    val activity=context as Activity
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope ()

    fun clickLogin(){
        activity.finish()
    }

    LaunchedEffect(message) {
        if (message.isNotEmpty() && message!="Loading") {

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                )
            }
            // If successful, close the activity
            if (message == "Successful Registration") {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                    )
                }
                activity.finish()
            }
        }
    }


    fun registerFunc(){
        viewModel.register(firstName, lastName,email,schoolId,phone, password)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState){
            Snackbar(
                snackbarData = it,
                containerColor = if (message.isNotEmpty() && message == "Successful Registration") Color.Green else Color.Red,
                contentColor = Color.White
            )
        } },
        bottomBar = {
            // Floating Register Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // Background to match the card/page
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        registerFunc()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = message!="Loading"
                ) {
                    Text(
                        text = if (message!="Loading") "Register" else "Registering In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        // Main screen content (Blue background and White Card)
        // We apply the verticalScroll modifier here to make the entire content area scrollable,
        // while the bottomBar remains fixed.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Important: Use padding from Scaffold
                .verticalScroll(rememberScrollState()), // Makes the entire column scrollable
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Blue Background Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp) // Fixed height for blue section (adjust as needed)
                    .background(BusMateBlue),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(48.dp)) // Top padding

                // Bus Logo (Kept the logo)
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Bus Mate Logo",
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(BusMateOrange),
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))
            }

            // 2. White Sign Up Card (Scrollable content)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-32).dp) // Offset for overlap
                    .fillMaxHeight(), // Allow the card height to be dictated by the content/scroll
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                // Inner content column
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Back Arrow and Centered Title/Link
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Arrow (Aligned left)
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.clickable { /* UX: navigate back */ }
                        )

                        // Spacer to push content right
                        Spacer(modifier = Modifier.weight(1f))

                        // Placeholder for right alignment (to balance the back arrow if needed, or remove)
                        Spacer(modifier = Modifier.width(24.dp))
                    }

                    // Sign Up Title (Centered)
                    Text(
                        text = "Sign Up",
                        color = SignUpTitleColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Login Link (Centered under title)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Already have an account?",
                            color = Color.Gray.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Login",
                            color = BusMateBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {clickLogin()}
                        )
                    }
                    // --- Input Fields ---

                    // First Name / Last Name Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // First Name
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("First Name") },
                            placeholder = { Text("Ram") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BusMateBlue, focusedLabelColor = BusMateBlue),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        // Last Name
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Last Name") },
                            placeholder = { Text("Thapa...") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BusMateBlue, focusedLabelColor = BusMateBlue),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text("ramthapa@gmail.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BusMateBlue, focusedLabelColor = BusMateBlue),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // School ID Field
                    OutlinedTextField(
                        value = schoolId,
                        onValueChange = { schoolId = it },
                        label = { Text("ID provided by school") },
                        placeholder = { Text("ATX6647") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BusMateBlue, focusedLabelColor = BusMateBlue),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone Number Field
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("(454) 726-0592") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BusMateBlue, focusedLabelColor = BusMateBlue),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Set Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        interactionSource = passwordInteractionSource,
                        label = {
                            Text(if (password.isEmpty() && !isPasswordFocused) "********" else "Set Password")
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BusMateBlue, focusedLabelColor = BusMateBlue),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        interactionSource = confirmPasswordInteractionSource,
                        label = {
                            Text(if (confirmPassword.isEmpty() && !isConfirmPasswordFocused) "********" else "Confirm Password")
                        },
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle confirm password visibility")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BusMateBlue, focusedLabelColor = BusMateBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Final Spacer at the bottom of the Card content
                    Spacer(modifier = Modifier.height(48.dp))

                }
            }
        }

    }
}