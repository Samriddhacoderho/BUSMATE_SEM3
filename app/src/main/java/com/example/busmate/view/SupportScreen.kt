package com.example.busmate.view

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.LightGrayBackground
import com.example.busmate.viewmodel.SupportViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SupportScreen(viewModel: SupportViewModel) {

    var titleText by remember { mutableStateOf("") }
    var explainText by remember { mutableStateOf("") }
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val model = activity.intent.getParcelableExtra<UserModel>("model")
    val customTextFieldColors = TextFieldDefaults.colors(
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedContainerColor = LightGrayBackground,
        unfocusedContainerColor = LightGrayBackground,
        disabledContainerColor = LightGrayBackground,
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black
    )

    LaunchedEffect(message) {
        if (message.isNotEmpty() && message != "Loading") {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                )
            }

            if(message=="Support request submitted"){
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                    )
                }
                delay(2000)
                activity.finish()
            }
        }
    }

    fun onSupportSubmit() {
        viewModel.writeReport(model?.firstName + model?.lastName, "parent", titleText, explainText)
    }

    Scaffold(snackbarHost = {
        SnackbarHost(hostState = snackbarHostState) {
            Snackbar(
                snackbarData = it,
                containerColor = if (message.isNotEmpty() && message == "Support request submitted") Color.Green else Color.Red,
                contentColor = Color.White
            )
        }
    }) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            // 🔵 TOP BLUE BACKGROUND (updated with logo)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.40f)
                    .background(BusMateBlue)
                    .padding(top = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ⭐ Bus Mate Logo (same as Login screen)
                Image(
                    painter = painterResource(com.example.busmate.R.drawable.logo),
                    contentDescription = "Bus Mate Logo",
                    colorFilter = ColorFilter.tint(Color(0xFFFFB74D)),  // same orange-yellow tint
                    modifier = Modifier.size(140.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Support & Grievance",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "If you are experiencing any issues, please\nlet us know. We will try to solve them as\nsoon as possible.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }


            // WHITE CARD (overlapping)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp)
                    .offset(y = (-35).dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {

                    // Title Label
                    Text(
                        text = "Title",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Title Input
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        placeholder = { Text("Add your grievance title here") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = customTextFieldColors,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Explain Label
                    Text(
                        text = "Explain the problem",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Explain input
                    OutlinedTextField(
                        value = explainText,
                        onValueChange = { explainText = it },
                        placeholder = { Text("Type your query here") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = customTextFieldColors
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Button
                    Button(
                        onClick = { onSupportSubmit() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BusMateBlue
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = "SUBMIT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "You can contact us at ",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "1234567892",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}