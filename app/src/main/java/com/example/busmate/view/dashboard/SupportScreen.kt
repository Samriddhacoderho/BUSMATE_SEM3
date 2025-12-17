package com.example.busmate.view.dashboard

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.R
import com.example.busmate.model.SupportModel
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
    val supportMessages by viewModel.supportMessages.collectAsState()
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

    // Show snackbars
    LaunchedEffect(message) {
        if (message.isNotEmpty() && message != "Loading") {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
            if (message == "Support request submitted") {
                delay(2000)
                activity.finish()
            }
        }
    }

    // Fetch all support messages
    LaunchedEffect(true) {
        viewModel.fetchSupportMessages()
    }

    fun onSupportSubmit() {
        viewModel.writeReport(
            "${model?.firstName} ${model?.lastName}",
            model?.typeofUser,
            titleText,
            explainText
        )
    }

    Scaffold(snackbarHost = {
        SnackbarHost(hostState = snackbarHostState) {
            Snackbar(
                snackbarData = it,
                containerColor = if (message == "Support request submitted") Color.Green else Color.Red,
                contentColor = Color.White
            )
        }
    }) { paddingValues ->

        if (model?.typeofUser == "Parent" || model?.typeofUser == "Driver") {

            val userSupportMessages = supportMessages.filter { it.uid == model.uid }

            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Form for submitting new support
                item {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.40f)
                                .background(BusMateBlue)
                                .padding(top = 50.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(R.drawable.logo),
                                contentDescription = "Bus Mate Logo",
                                colorFilter = ColorFilter.tint(Color(0xFFFFB74D)),
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
                            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                                Text("Title", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                OutlinedTextField(
                                    value = titleText,
                                    onValueChange = { titleText = it },
                                    placeholder = { Text("Add your grievance title here") },
                                    modifier = Modifier.fillMaxWidth().height(55.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = customTextFieldColors,
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Text("Explain the problem", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                OutlinedTextField(
                                    value = explainText,
                                    onValueChange = { explainText = it },
                                    placeholder = { Text("Type your query here") },
                                    modifier = Modifier.fillMaxWidth().height(160.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = customTextFieldColors
                                )

                                Spacer(modifier = Modifier.height(30.dp))

                                Button(
                                    onClick = { onSupportSubmit() },
                                    modifier = Modifier.fillMaxWidth().height(55.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BusMateBlue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("SUBMIT", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("You can contact us at ", color = Color.Gray, fontSize = 14.sp)
                                    Text("1234567892", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                }

                // Display previous support messages + admin replies
                items(userSupportMessages) { support ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Title: ${support.title}", fontWeight = FontWeight.SemiBold)
                            Text("Message: ${support.message}")
                            Text("Reply: ${support.reply.ifEmpty { "No reply yet" }}")
                        }
                    }
                }
            }

        } else if (model?.typeofUser == "Admin") {

            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(supportMessages) { support ->
                    var adminReply by remember { mutableStateOf("") }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("User ID: ${support.uid ?: "N/A"}", fontWeight = FontWeight.Bold)
                            Text("Name: ${support.name ?: "N/A"}")
                            Text("User Type: ${support.typeofUser ?: "N/A"}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Title: ${support.title ?: "N/A"}", fontWeight = FontWeight.SemiBold)
                            Text("Message: ${support.message ?: "N/A"}")
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = adminReply,
                                onValueChange = { adminReply = it },
                                placeholder = { Text("Type your reply here") },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = customTextFieldColors
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    if (adminReply.isNotEmpty()) {
                                        viewModel.replyToSupport(support.uid ?: "", adminReply)
                                        adminReply = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(45.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BusMateBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("REPLY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item { Text("User type not recognized") }
            }
        }
    }
}
