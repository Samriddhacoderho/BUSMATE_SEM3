package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.busmate.view.ui.theme.BUSMATETheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.PlaceholderBusColor
import com.example.busmate.ui.theme.PrimaryBlue
import com.example.busmate.view.ui.theme.BUSMATETheme
import com.example.busmate.viewmodel.ResetPasswordViewModel

class ParentResetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ResetPasswordViewModel = viewModel()
            ParentResetPasswordUI(viewModel)
        }
    }
}


@Composable
fun ParentResetPasswordUI(viewModel: ResetPasswordViewModel) {

    var userId by remember { mutableStateOf("") }

    val uiState by viewModel.message.collectAsState()
    val isLoading = uiState == "Loading"

    val snackbarHostState = remember { SnackbarHostState() }

    // 🔥 This is the correct place to show snackbar
    LaunchedEffect(uiState) {
        if (uiState.isNotBlank() && uiState != "Loading") {
            snackbarHostState.showSnackbar(uiState)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // ---------- TOP BLUE SECTION ----------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.60f)
                    .background(BusMateBlue),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(48.dp))

                Image(
                    painter = painterResource(com.example.busmate.R.drawable.logo),
                    contentDescription = "Logo",
                    colorFilter = ColorFilter.tint(PlaceholderBusColor),
                    modifier = Modifier.size(200.dp)
                )

                Text(
                    text = "Reset\nPassword",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 40.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Enter your recovery Email ID to reset password",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            // ---------- WHITE CARD ----------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.BottomCenter)
                    .height(350.dp)
                    .offset(y = (-100).dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    OutlinedTextField(
                        value = userId,
                        onValueChange = { userId = it },
                        label = { Text("Enter email") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = {
                            if (userId.isNotBlank()) {
                                viewModel.resetPassword(userId.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                text = "Send Reset Link to Email",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}




//@Preview(showBackground = true)
//@Composable
//fun PreviewforPassResetParent() {
//    ParentResetPasswordUI()
//}

