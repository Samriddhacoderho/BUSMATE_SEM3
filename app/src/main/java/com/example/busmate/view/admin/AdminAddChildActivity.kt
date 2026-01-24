package com.example.busmate.view.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.ChildRepositoryImpl
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.view.parent.AddChildActivity
import com.example.busmate.viewmodel.ChildViewModel

class AdminAddChildActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // NEW: Get the Parent ID passed from CreateAccountScreenActivity
        val prefilledParentId = intent.getStringExtra("PARENT_ID") ?: ""

        val repo = ChildRepositoryImpl()
        val viewModel = ChildViewModel(repo)

        setContent {
            AdminAddChildScreen(
                viewModel = viewModel,
                prefilledParentId = prefilledParentId
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddChildScreen(viewModel: ChildViewModel, prefilledParentId: String) {
    // If prefilledParentId is empty, we let the admin type it
    var parentId by remember { mutableStateOf(prefilledParentId) }
    var studentId by remember { mutableStateOf("") }

    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    val isFromCreateAccount = prefilledParentId.isNotEmpty()

    // AUTOMATIC NAVIGATION LOGIC
    LaunchedEffect(message) {
        if (message.contains("successfully", ignoreCase = true)) {
            val intent = Intent(context, AddChildActivity::class.java).apply {
                putExtra("STUDENT_ID", studentId)
                putExtra("PARENT_ID", parentId) // Use the state variable
            }
            context.startActivity(intent)
            (context as? Activity)?.finish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isFromCreateAccount) "Step 2: Student ID" else "Add Child to Parent", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BusMateBlue)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Child Registration",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BusMateBlue
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // PARENT ID SECTION
                    if (isFromCreateAccount) {
                        // Show read-only if coming from Account Creation
                        Text(
                            text = "Linking to Parent: $parentId",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32)
                        )
                    } else {
                        // Show Input if adding a second child or existing parent
                        OutlinedTextField(
                            value = parentId,
                            onValueChange = { parentId = it },
                            label = { Text("Enter Parent School ID") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BusMateBlue,
                                focusedLabelColor = BusMateBlue
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // STUDENT ID SECTION
                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = { Text("Enter New Student ID") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BusMateBlue,
                            focusedLabelColor = BusMateBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Inside AdminAddChildScreen in AdminAddChildActivity.kt
                    Button(
                        onClick = {
                            if (parentId.isBlank()) {
                                // Local check for empty field
                                return@Button
                            }
                            if (studentId.isBlank()) {
                                // Local check for empty field
                                return@Button
                            }

                            // Call the new combined validation function
                            viewModel.validateAndPreRegister(parentId, studentId)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BusMateBlue),
                        enabled = message != "Processing..."
                    ) {
                        Text(
                            text = if (message == "Processing...") "Verifying..." else "Next: Child Details",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Error/Success Message
                    if (message.isNotEmpty() && message != "Processing...") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = message,
                            color = if (message.contains("successfully", true)) Color(0xFF2E7D32) else Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}