package com.example.busmate.view.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.R
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.testTag

// --- Custom Colors ---
private val PrimaryBlue = Color(0xFF2567E8)
private val PlaceholderBusColor = Color(0xFFFFB74D) // The orange/yellow color for the bus logo

fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
fun isValidNepaliPhone(phone: String): Boolean {
    val regex = Regex("^(\\+977)?[9][6-9]\\d{8}$")
    return regex.matches(phone)
}


class SignUpScreen : ComponentActivity() {
    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repo= UserRepositoryImpl()
            val viewModel= UserViewModel(repo)
            SignUpScreenUI(viewModel)

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
    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var schoolIdError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    val isFormValid =
        firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                email.isNotBlank() &&
                isValidEmail(email) &&
                schoolId.isNotBlank() &&
                isValidNepaliPhone(phone) &&
                password.matches(Regex("(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#\$%^&*()]).*")) &&
                confirmPassword.isNotBlank() &&
                password == confirmPassword &&
                password.length >= 6

    // InteractionSources to track focus state for conditional labels
    val passwordInteractionSource = remember { MutableInteractionSource() }
    val isPasswordFocused by passwordInteractionSource.collectIsFocusedAsState()
    val confirmPasswordInteractionSource = remember { MutableInteractionSource() }
    val isConfirmPasswordFocused by confirmPasswordInteractionSource.collectIsFocusedAsState()
    val context= LocalContext.current
    val activity=context as Activity
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val isButtonEnabled = isFormValid && message != "Loading..."

    fun clickLogin(){
        activity.finish()
    }

    LaunchedEffect(message) {
        if (message.isNotEmpty() && message!="Loading...") {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                )
            }
            // If successful, close the activity after showing snackbar
            // Check for success in multiple ways to be robust
            if (message.contains("Success", ignoreCase = true) ||
                message.contains("successful", ignoreCase = true) ||
                message == "Successful Registration") {
                kotlinx.coroutines.delay(1500) // Wait 1.5 seconds to show success message
                activity.finish()
            }
        }
    }


    fun registerFunc() {
        // Reset errors
        firstNameError = ""
        lastNameError = ""
        emailError = ""
        schoolIdError = ""
        phoneError = ""
        passwordError = ""
        confirmPasswordError = ""

        when {
            firstName.isBlank() -> firstNameError = "First name is required"
            lastName.isBlank() -> lastNameError = "Last name is required"
            email.isBlank() -> emailError = "Email is required"
            !isValidEmail(email) -> emailError = "Invalid email format"
            schoolId.isBlank() -> schoolIdError = "School ID is required"
            phone.isBlank() -> phoneError = "Phone number is required"
            !isValidNepaliPhone(phone) -> phoneError = "Enter valid Nepali phone number"
            password.isBlank() -> passwordError = "Password is required"
            password.length < 6 -> passwordError = "Password must be at least 6 characters"
            !password.matches(Regex(".*[A-Z].*")) -> passwordError = "Must include 1 uppercase letter"
            !password.matches(Regex(".*[a-z].*")) -> passwordError = "Must include 1 lowercase letter"
            !password.matches(Regex(".*\\d.*")) -> passwordError = "Must include 1 number"
            !password.matches(Regex(".*[!@#\$%^&*()].*")) -> passwordError = "Must include 1 special character"
            confirmPassword.isBlank() -> confirmPasswordError = "Confirm password is required"
            password != confirmPassword -> confirmPasswordError = "Passwords do not match"
            else -> {
                viewModel.register(firstName, lastName, email, schoolId, phone, password)
            }
        }
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = if (message.contains("Success", ignoreCase = true) ||
                        message.contains("successful", ignoreCase = true))
                        Color(0xFF4CAF50) else Color.Red,
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // 1. Top Blue Background Section with transparent background image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .background(BusMateBlue)
            ) {
                // Background image with transparency
                Image(
                    painter = painterResource(id = R.drawable.loginscreenphoto),
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.22f
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Back arrow
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = { clickLogin() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sign Up title
                    Text(
                        text = "Create your\nAccount",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }

            // 2. White Sign Up Card (Scrollable, overlaps the blue section)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.78f)
                    .padding(horizontal = 24.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-16).dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(0xFFF8F9FE)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Name Fields Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // First Name Field
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = firstName,
                                    onValueChange = {
                                        firstName = it
                                        firstNameError = if (it.isBlank()) "Required" else ""
                                    },
                                    label = { Text("First Name", fontWeight = FontWeight.Medium) },
                                    singleLine = true,
                                    isError = firstNameError.isNotEmpty(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryBlue,
                                        focusedLabelColor = PrimaryBlue,
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White,
                                        errorBorderColor = Color(0xFFE57373),
                                        errorLabelColor = Color(0xFFE57373)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("firstName")
                                )
                                AnimatedVisibility(
                                    visible = firstNameError.isNotEmpty(),
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Text(
                                        text = firstNameError,
                                        color = Color(0xFFE57373),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                                    )
                                }
                            }

                            // Last Name Field
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = lastName,
                                    onValueChange = {
                                        lastName = it
                                        lastNameError = if (it.isBlank()) "Required" else ""
                                    },
                                    label = { Text("Last Name", fontWeight = FontWeight.Medium) },
                                    singleLine = true,
                                    isError = lastNameError.isNotEmpty(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryBlue,
                                        focusedLabelColor = PrimaryBlue,
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White,
                                        errorBorderColor = Color(0xFFE57373),
                                        errorLabelColor = Color(0xFFE57373)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("lastName")
                                )
                                AnimatedVisibility(
                                    visible = lastNameError.isNotEmpty(),
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Text(
                                        text = lastNameError,
                                        color = Color(0xFFE57373),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = when {
                                    it.isBlank() -> "Email required"
                                    !isValidEmail(it) -> "Invalid email"
                                    else -> ""
                                }
                            },
                            label = { Text("Email", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("example@gmail.com", color = Color(0xFF9E9E9E)) },
                            singleLine = true,
                            isError = emailError.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                focusedLabelColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                errorBorderColor = Color(0xFFE57373),
                                errorLabelColor = Color(0xFFE57373)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("email")
                        )
                        AnimatedVisibility(
                            visible = emailError.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = emailError,
                                color = Color(0xFFE57373),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))

                        // School ID Field
                        OutlinedTextField(
                            value = schoolId,
                            onValueChange = {
                                schoolId = it
                                schoolIdError = if (it.isBlank()) "School ID required" else ""
                            },
                            label = { Text("ID provided by school", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("ATX6647", color = Color(0xFF9E9E9E)) },
                            singleLine = true,
                            isError = schoolIdError.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                focusedLabelColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                errorBorderColor = Color(0xFFE57373),
                                errorLabelColor = Color(0xFFE57373)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("schoolId")
                        )
                        AnimatedVisibility(
                            visible = schoolIdError.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = schoolIdError,
                                color = Color(0xFFE57373),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))

                        // Phone Number Field
                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                                phoneError = when {
                                    it.isBlank() -> "Phone number required"
                                    !isValidNepaliPhone(it) -> "Enter valid Nepali phone number"
                                    else -> ""
                                }
                            },
                            label = { Text("Phone Number", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("98XXXXXXXX", color = Color(0xFF9E9E9E)) },
                            singleLine = true,
                            isError = phoneError.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                focusedLabelColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                errorBorderColor = Color(0xFFE57373),
                                errorLabelColor = Color(0xFFE57373)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("phone")
                        )
                        AnimatedVisibility(
                            visible = phoneError.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = phoneError,
                                color = Color(0xFFE57373),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))

                        // Set Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                passwordError = when {
                                    it.isBlank() -> "Password required"
                                    it.length < 6 -> "Minimum 6 characters"
                                    !it.matches(Regex(".*[A-Z].*")) -> "Include 1 uppercase"
                                    !it.matches(Regex(".*[a-z].*")) -> "Include 1 lowercase"
                                    !it.matches(Regex(".*\\d.*")) -> "Include 1 number"
                                    !it.matches(Regex(".*[!@#\$%^&*()].*")) -> "Include 1 special char"
                                    else -> ""
                                }
                            },
                            interactionSource = passwordInteractionSource,
                            label = {
                                Text("Set Password", fontWeight = FontWeight.Medium)
                            },
                            placeholder = { Text("Set Password", color = Color(0xFF9E9E9E)) },
                            singleLine = true,
                            isError = passwordError.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = Color(0xFF757575))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                focusedLabelColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                errorBorderColor = Color(0xFFE57373),
                                errorLabelColor = Color(0xFFE57373)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("password")
                        )
                        AnimatedVisibility(
                            visible = passwordError.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = passwordError,
                                color = Color(0xFFE57373),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Confirm Password Field
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                confirmPasswordError = when {
                                    it.isBlank() -> "Confirm password"
                                    it != password -> "Passwords do not match"
                                    else -> ""
                                }
                            },
                            interactionSource = confirmPasswordInteractionSource,
                            label = {
                                Text("Confirm Password", fontWeight = FontWeight.Medium)
                            },
                            placeholder = { Text("********", color = Color(0xFF9E9E9E)) },
                            singleLine = true,
                            isError = confirmPasswordError.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(imageVector = image, contentDescription = "Toggle confirm password visibility", tint = Color(0xFF757575))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                focusedLabelColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                errorBorderColor = Color(0xFFE57373),
                                errorLabelColor = Color(0xFFE57373)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("confirmPassword")
                        )
                        AnimatedVisibility(
                            visible = confirmPasswordError.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = confirmPasswordError,
                                color = Color(0xFFE57373),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Register Button
                        Button(
                            onClick = { registerFunc() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                disabledContainerColor = Color(0xFFBDBDBD),
                                disabledContentColor = Color.White,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .testTag("signUpButton"),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            ),
                            enabled = isButtonEnabled
                        ) {
                            if (message == "Loading...") {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(26.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = "Register",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        // Already have account link
                        Row {
                            Text(
                                text = "Already have an account?",
                                fontSize = 14.sp,
                                color = Color(0xFF757575),
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Log In",
                                color = PrimaryBlue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { clickLogin() }
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}