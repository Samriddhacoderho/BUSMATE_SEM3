package com.example.busmate.view.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.interaction.MutableInteractionSource // New Import
import androidx.compose.foundation.interaction.collectIsFocusedAsState // New Import
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.view.dashboard.ParentDashboardActivity
import com.example.busmate.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import com.example.busmate.R
import com.example.busmate.view.all.ResetPasswordActivity
import com.google.gson.Gson
import androidx.compose.ui.platform.testTag

// --- Custom Colors ---
private val PrimaryBlue = Color(0xFF2567E8)
private val PlaceholderBusColor = Color(0xFFFFB74D) // The orange/yellow color for the bus logo

class LoginScreen : ComponentActivity() {
    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val repo= UserRepositoryImpl()
            val viewModel= UserViewModel(repo)
            LoginScreenUI(viewModel)
        }
    }
}



@Composable
fun LoginScreenUI(viewModel: UserViewModel) {
    val context= LocalContext.current
    val activity=context as Activity
    // State variables for input fields and checkbox (required for TextField components)
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    // InteractionSource to track focus state of the password field
    val passwordInteractionSource = remember { MutableInteractionSource() }
    val isPasswordFocused by passwordInteractionSource.collectIsFocusedAsState()
    val message by viewModel.message.collectAsState()
    val user by viewModel.user.collectAsState()
    val snackbarHostState=remember { SnackbarHostState() }
    val coroutineScope=rememberCoroutineScope()

    var userIdError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    // Logic for enabling the button now checks both ID and Password UI constraints
    val isPasswordLengthValid = password.isEmpty() || password.length >= 8
    val isUserIdNumeric = userId.isEmpty() || userId.all { it.isDigit() } // New numeric check

    val isValid = userId.isNotBlank() && password.isNotBlank() && isPasswordLengthValid && isUserIdNumeric
    val isButtonEnabled = isValid && message != "Loading"

    fun clickSignup(){
        val intent= Intent(context, SignUpScreen::class.java)
        context.startActivity(intent)
    }
    fun forgotPassword(){
        val intent= Intent(context, ResetPasswordActivity::class.java)
        context.startActivity(intent)
    }

    LaunchedEffect(message) {
        if (message.isNotEmpty() && message != "Loading") {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                )
            }

            if (message == "Successful Login") {
                //  Show snackbar
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message = message)
                }
                // Save user to SharedPreferences
                if (rememberMe==true){
                    val sharedPreferences = context.getSharedPreferences("User", Activity.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    val gson = Gson()
                    val json = gson.toJson(user)
                    editor.putString("user_model", json)
                    editor.apply()

                }
                //  Navigate to dashboard
                val intent = Intent(context, ParentDashboardActivity::class.java)
                intent.putExtra("model", user)
                context.startActivity(intent)
                activity.finish()
            }

        }
    }


    fun loginFunc() {

        // Reset errors
        userIdError = ""
        passwordError = ""

        if (userId.isBlank()) {
            userIdError = "User ID is required"
        } else if (!userId.all { it.isDigit() }) {
            userIdError = "Only numbers are allowed"
        }

        if (password.isBlank()) {
            passwordError = "Password is required"
        }
        else if (password.length < 8) {
            passwordError = "Password length should be minimum 8 characters."
        }

        if (userIdError.isNotEmpty() || passwordError.isNotEmpty()) {
            return
        }

        viewModel.login(userId, password)
    }


    Scaffold(Modifier.fillMaxSize(),
        snackbarHost = {SnackbarHost(hostState = snackbarHostState){
            Snackbar(
                snackbarData = it,
                containerColor = if (message.isNotEmpty() && message == "Successful Login") Color.Green else Color.Red,
                contentColor = Color.White
            )
        } }
    )
    {paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // 1. Top Blue Background Section with transparent background image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.60f)
                    .background(BusMateBlue)
            ) {
                // Background image - REPLACE 'your_background_image' with your actual drawable resource name
                // For example: R.drawable.login_background
                // Uncomment the line below and replace with your image resource
                 Image(
                     painter = painterResource(id = R.drawable.loginscreenphoto),
                     contentDescription = "Background",
                     modifier = Modifier.fillMaxSize(),
                     contentScale = ContentScale.Crop,
                     alpha = 0.22f // Adjust transparency (0.1f to 1.0f)
                 )

                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(24.dp)) // FIX: Reduced top padding to 24.dp

                    // Bus Logo
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "Bus Mate Logo",
                        colorFilter = ColorFilter.tint(PlaceholderBusColor),
                        modifier = Modifier.size(200.dp)
                    )



                    // Log in title
                    Text(
                        text = "Log in to your\nAccount",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 40.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(30.dp))

                    // Subtitle
                    Text(
                        text = "Enter your ID and password to log in",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 2. White Login Card (Overlaps the blue section)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-32).dp), // Negative offset to make it overlap the blue section
                // Custom shape for rounded top corners
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // User ID Field
                    OutlinedTextField(
                        value = userId,
                        onValueChange = { newValue ->
                            userId = newValue

                            // FIX: Real-time validation for numeric input
                            if (newValue.isNotBlank() && !newValue.all { it.isDigit() }) {
                                userIdError = "Only numbers are allowed"
                            } else if (newValue.isNotBlank()) {
                                userIdError = ""
                            } else if (newValue.isBlank()) {
                                userIdError = ""
                            }
                        },
                        label = { Text("Enter ID here") },
                        singleLine = true,
                        isError = userIdError.isNotEmpty(),
                        // FIX: Set keyboard to numeric
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            errorBorderColor = Color.Red,
                            errorLabelColor = Color.Red
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("schoolId"),
                    )

                    // Animated error message for User ID
                    AnimatedVisibility(
                        visible = userIdError.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = userIdError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { newValue ->
                            password = newValue

                            if (newValue.length > 0 && newValue.length < 8) {
                                passwordError = "Password length should be minimum 8 characters."
                            } else if (newValue.isNotBlank() && newValue.length >= 8) {
                                passwordError = "" // Clear error if criteria is met
                            } else if (newValue.isBlank()) {
                                passwordError = "" // Clear error if field is empty
                            }
                        },
                        interactionSource = passwordInteractionSource,
                        label = { Text(if (password.isEmpty() && !isPasswordFocused) "********" else "Password") },
                        singleLine = true,
                        isError = passwordError.isNotEmpty(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            errorBorderColor = Color.Red,
                            errorLabelColor = Color.Red
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("password")
                    )

                    // Animated error message for Password
                    AnimatedVisibility(
                        visible = passwordError.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = passwordError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Remember me & Forgot Password Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Remember me Checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                            )
                            Text(
                                text = "Remember me",
                                fontSize = 14.sp
                            )
                        }

                        // Forgot Password Link
                        Text(
                            text = "Forgot Password ?",
                            color = PrimaryBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                forgotPassword()
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Log In Button
                    Button(
                        onClick = { loginFunc()},
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.White,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp).testTag("loginButton"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isButtonEnabled

                    ) {
                        Text(
                            text = if (message!="Loading") "Log In" else "Logging In",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Sign Up Link
                    Row {
                        Text(
                            text = "Don't have an account?",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sign Up",
                            color = PrimaryBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {clickSignup()}
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}