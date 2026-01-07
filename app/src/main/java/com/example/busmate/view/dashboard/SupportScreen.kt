package com.example.busmate.view.dashboard

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.model.SupportModel
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.LightGrayBackground
import com.example.busmate.viewmodel.SupportViewModel
import com.example.busmate.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    viewModel: SupportViewModel,
    userViewModel: UserViewModel = remember { UserViewModel(UserRepositoryImpl()) } // Add this
) {
    val context = LocalContext.current
    val activity = context as Activity

    // Try to get from intent, otherwise use the ViewModel state
    val intentModel = activity.intent.getParcelableExtra<UserModel>("model")
    val userState by userViewModel.user.collectAsState()
    val model = intentModel ?: userState // Use intent if available, else use ViewModel

    // Trigger load if model is null
    LaunchedEffect(Unit) {
        if (intentModel == null) {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            userViewModel.loadUserProfile(currentUid)
        }
    }

//    val model = activity.intent.getParcelableExtra<UserModel>("model")

    val message by viewModel.message.collectAsState()
    val supportMessages by viewModel.supportMessages.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var titleText by remember { mutableStateOf("") }
    var explainText by remember { mutableStateOf("") }

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

    // Snackbars
    LaunchedEffect(message) {
        if (message.isNotEmpty() && message != "Loading") {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
            if (message == "Support request submitted") {
                delay(2000)
            }
        }
    }

    // Fetch messages
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Help & Support", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) { // Closes activity and goes back
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = BusMateBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (message == "Support request submitted") Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (model?.typeofUser == "Parent" || model?.typeofUser == "Driver") {
                val userSupportMessages = supportMessages.filter { it.uid == model.uid }

                // Submit Form
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Submit a Support Request", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BusMateBlue)

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = titleText,
                                onValueChange = { titleText = it },
                                placeholder = { Text("Title") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = customTextFieldColors,
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = explainText,
                                onValueChange = { explainText = it },
                                placeholder = { Text("Explain your issue") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = customTextFieldColors
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { onSupportSubmit() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BusMateBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("SUBMIT", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Display previous messages
                items(userSupportMessages) { support ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // User message
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            elevation = CardDefaults.cardElevation(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Title: ${support.title}", fontWeight = FontWeight.Bold)
                                Text(support.message)
                            }
                        }

                        // Admin reply
                        if (support.reply.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                                elevation = CardDefaults.cardElevation(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Admin Reply:", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                    Text(support.reply)
                                }
                            }
                        }
                    }
                }

            } else if (model?.typeofUser == "Admin") {
                // Admin view to reply
                items(supportMessages) { support ->
                    var adminReply by remember { mutableStateOf("") }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("User: ${support.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BusMateBlue)
                            Text("Type: ${support.typeofUser ?: "N/A"}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Title: ${support.title}", fontWeight = FontWeight.SemiBold)
                            Text(support.message)

                            if (support.reply.isNotEmpty()) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                    elevation = CardDefaults.cardElevation(4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Previous Reply:", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        Text(support.reply)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = adminReply,
                                onValueChange = { adminReply = it },
                                placeholder = { Text("Type your reply here") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = customTextFieldColors
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (adminReply.isNotEmpty()) {
                                        viewModel.replyToSupport(support.uid ?: "", adminReply)
                                        adminReply = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BusMateBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("REPLY", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                item { Text("User type not recognized") }
            }
        }
    }
}
