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
import androidx.compose.foundation.interaction.MutableInteractionSource // New Import
import androidx.compose.foundation.interaction.collectIsFocusedAsState // New Import
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.R
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.view.ui.theme.BUSMATETheme
import com.example.busmate.viewmodel.UserViewModel

// --- Custom Colors ---
private val PrimaryBlue = Color(0xFF2567E8)
private val PlaceholderBusColor = Color(0xFFFFB74D) // The orange/yellow color for the bus logo

class LoginScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BUSMATETheme {
                val repo= UserRepositoryImpl()
                val viewModel= UserViewModel(repo)
                    LoginScreenUI(viewModel)
            }
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


    fun clickSignup(){
        val intent= Intent(context, SignUpScreen::class.java)
        context.startActivity(intent)
    }

    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

            if(message=="Successful Login"){
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                val intent= Intent(context, ParentDashboardActivity::class.java)
                context.startActivity(intent)
                activity.finish()
            }
        }
    }


    fun loginFunc(){
        viewModel.login(userId,password)
    }

    // Main screen structure uses Box for layering the blue background and the white card
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Top Blue Background Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.60f)
                .background(BusMateBlue),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(48.dp)) // Top padding

            // Bus Logo
            Image(
                painter = painterResource(com.example.busmate.R.drawable.logo),
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
                    onValueChange = { userId = it },
                    label = { Text("Enter ID here") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    // Apply InteractionSource to track focus
                    interactionSource = passwordInteractionSource,
                    // Conditional label now checks if the field is empty AND NOT focused.
                    // If focused (or not empty), it shows "Password".
                    label = {
                        Text(if (password.isEmpty() && !isPasswordFocused) "********" else "Password")
                    },
                    // -----------------------------------------------------
                    singleLine = true,
                    // Toggle visibility transformation based on state
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle password visibility")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
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
                        /*logi for navigation to password reset */
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Log In Button
                Button(
                    onClick = { loginFunc()},
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Log In",
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
//
//@Preview(showBackground = true)
//@Composable
//fun LoginScreenPreview() {
//    LoginScreenUI()
//}