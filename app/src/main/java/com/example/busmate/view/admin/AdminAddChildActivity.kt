package com.example.busmate.view.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.ChildRepositoryImpl
import com.example.busmate.view.parent.AddChildActivity
import com.example.busmate.viewmodel.ChildViewModel
import kotlinx.coroutines.launch

class AdminAddChildActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
fun AdminAddChildScreen(
    viewModel: ChildViewModel,
    prefilledParentId: String
) {
    var parentId by remember { mutableStateOf(prefilledParentId) }
    var studentId by remember { mutableStateOf("") }

    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    val isFromCreateAccount = prefilledParentId.isNotEmpty()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // AUTOMATIC NAVIGATION LOGIC (UNCHANGED)
    LaunchedEffect(message) {
        if (message.contains("successfully", ignoreCase = true)) {
            val intent = Intent(context, AddChildActivity::class.java).apply {
                putExtra("STUDENT_ID", studentId)
                putExtra("PARENT_ID", parentId)
            }
            context.startActivity(intent)
            (context as? Activity)?.finish()
        } else if (message.isNotEmpty() && message != "Processing...") {
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    val cardAlpha by animateFloatAsState(targetValue = 1f)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // 🔵 ADMIN HEADER (MATCHED)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF2567E8),
                                Color(0xFF1D4ED8)
                            )
                        )
                    )
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(vertical = 28.dp, horizontal = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isFromCreateAccount) {
                        IconButton(
                            onClick = { (context as Activity).finish() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ChildCare,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Admin",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isFromCreateAccount) "Step 2: Add Child" else "Add Child to Parent",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // 🧾 MAIN CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .graphicsLayer { alpha = cardAlpha },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Child Registration",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Link a student to a parent account",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(Modifier.height(24.dp))

                    // 👨‍👩‍👧 PARENT ID
                    if (isFromCreateAccount) {
                        Text(
                            text = "Linked Parent ID: $parentId",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2567E8)
                        )
                    } else {
                        OutlinedTextField(
                            value = parentId,
                            onValueChange = { parentId = it },
                            label = { Text("Parent School ID") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF2567E8)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2567E8),
                                focusedLabelColor = Color(0xFF2567E8)
                            )
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // 🆔 STUDENT ID
                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = { Text("Student ID") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = Color(0xFF2567E8)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2567E8),
                            focusedLabelColor = Color(0xFF2567E8)
                        )
                    )

                    Spacer(Modifier.height(28.dp))

                    // 🚀 ACTION BUTTON
                    Button(
                        onClick = {
                            if (parentId.isBlank()) return@Button
                            if (studentId.isBlank()) return@Button
                            viewModel.validateAndPreRegister(parentId, studentId)
                        },
                        enabled = message != "Processing...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2567E8)
                        )
                    ) {
                        if (message == "Processing...") {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Verifying...")
                        } else {
                            Text(
                                text = "Next: Child Details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
