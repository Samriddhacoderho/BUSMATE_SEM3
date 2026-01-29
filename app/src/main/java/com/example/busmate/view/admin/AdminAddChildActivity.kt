package com.example.busmate.view.admin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
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
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.ui.theme.isDarkMode
import com.example.busmate.view.parent.AddChildActivity
import com.example.busmate.viewmodel.ChildViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminAddChildActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefilledParentId = intent.getStringExtra("PARENT_ID") ?: ""
        val repo = ChildRepositoryImpl()
        val viewModel = ChildViewModel(repo)

        setContent {
            val context = LocalContext.current
            val sharedPrefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
            var themeUpdateTrigger by remember { mutableIntStateOf(0) }

            DisposableEffect(Unit) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "dark_mode_pref") { themeUpdateTrigger++ }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            key(themeUpdateTrigger) {
                BusMateTheme(darkTheme = isDarkMode()) {
                    AdminAddChildScreen(
                        viewModel = viewModel,
                        prefilledParentId = prefilledParentId,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddChildScreen(
    viewModel: ChildViewModel,
    prefilledParentId: String,
    onBack: () -> Unit
) {
    val busMateBlue = Color(0xFF2854D8)

    var parentId by remember { mutableStateOf(prefilledParentId) }
    var studentId by remember { mutableStateOf("") }

    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    val isFromCreateAccount = prefilledParentId.isNotEmpty()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isFormValid = parentId.isNotBlank() && studentId.isNotBlank() && message != "Processing..."

    LaunchedEffect(Unit) {
        val randomId = (100000..999999).random().toString()
        studentId = randomId
    }

    LaunchedEffect(message) {
        if (message.contains("successfully", ignoreCase = true)) {
            // No Snackbar here, just immediate navigation
            delay(500) // Small delay for better UX flow
            val intent = Intent(context, AddChildActivity::class.java).apply {
                putExtra("STUDENT_ID", studentId)
                putExtra("PARENT_ID", parentId)
            }
            context.startActivity(intent)
            (context as? Activity)?.finish()
        } else if (message.isNotEmpty() && message != "Processing...") {
            // Show red snackbar for errors/other messages
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    snackbarData = data
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isFromCreateAccount) "Step 2: Add Child" else "Link Child",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (!isFromCreateAccount) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = busMateBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Child Registration",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Enter IDs to link the student account",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = parentId,
                onValueChange = { if (!isFromCreateAccount) parentId = it },
                readOnly = isFromCreateAccount,
                label = { Text("Parent School ID") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = busMateBlue) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = studentId,
                onValueChange = { studentId = it },
                label = { Text("Student School ID") },
                leadingIcon = { Icon(Icons.Default.Badge, null, tint = busMateBlue) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.validateAndPreRegister(parentId, studentId)
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = busMateBlue)
            ) {
                if (message == "Processing...") {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("NEXT: CHILD DETAILS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}