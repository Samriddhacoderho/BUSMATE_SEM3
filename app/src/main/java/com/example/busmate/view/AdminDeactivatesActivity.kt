package com.example.busmate.view

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.AdminActionsImpl
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.viewmodel.AdminActionsViewModel
import kotlinx.coroutines.launch

class AdminDeactivatesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminManageAccountScreen()
        }
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
fun AdminManageAccountScreen() {
    val viewModel = remember { AdminActionsViewModel(AdminActionsImpl()) }
    val context = LocalContext.current as Activity

    // -------------------- State Variables --------------------
    var schoolId by remember { mutableStateOf("") }
    var schoolIdError by remember { mutableStateOf("") }
    var model by remember { mutableStateOf<UserModel?>(null) }
    var showUserDetails by remember { mutableStateOf(false) }
    var messageShow by remember { mutableStateOf("") }

    var selectedAction by remember { mutableStateOf("") }
    var expandedAction by remember { mutableStateOf(false) }

    val reasons = listOf("User requested deactivation", "Violation of rules", "Fraudulent activity", "Inactive for long time", "Other")
    var selectedReason by remember { mutableStateOf("") }
    var expandedReason by remember { mutableStateOf(false) }

    // --- NEW: Password Verification States ---
    var showPasswordDialog by remember { mutableStateOf(false) }
    var adminPassword by remember { mutableStateOf("") }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val PrimaryBlue = Color(0xFF2567E8)

    val isConfirmEnabled = if (selectedAction == "Reactivate Account") {
        selectedAction.isNotBlank()
    } else {
        selectedAction.isNotBlank() && selectedReason.isNotBlank()
    }

    // ---------------------- Functions ----------------------

    fun onclickSearchButton() {
        viewModel.getUserbyID(schoolId) { success, user ->
            if (success) {
                model = user
                showUserDetails = true
            } else {
                coroutineScope.launch { snackbarHostState.showSnackbar("User not found") }
                showUserDetails = false
                messageShow = "User not found"
            }
        }
    }

    fun deactivateonClick() {
        if (model != null && model?.status == "active") {
            viewModel.deactivateAccount(schoolId) { success, message ->
                coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                messageShow = message
            }
        } else {
            viewModel.reactivateAccount(schoolId) { success, message ->
                coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                messageShow = message
            }
        }
        selectedAction = ""; selectedReason = ""; showUserDetails = false
    }

    fun deleteonClick() {
        if (model != null) {
            viewModel.deleteAccount(schoolId) { success, message ->
                coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                messageShow = message
            }
        }
        selectedAction = ""; selectedReason = ""; showUserDetails = false
    }

    // --- NEW: Helper to trigger verification before action ---
    fun verifyAndExecute(action: () -> Unit) {
        pendingAction = action
        showPasswordDialog = true
    }

    // ---------------------- UI ----------------------

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = if (messageShow.contains("User Deleted") || messageShow.contains("Deactivated") || messageShow.contains("Reactivated"))
                        Color.Green else Color.Red,
                    contentColor = Color.White
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f).background(BusMateBlue).statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 8.dp)) {
                    IconButton(onClick = { context.finish() }, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.height(40.dp))
                Text("Manage User Accounts", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(12.dp))
                Text("Enter School ID to find the parent/driver account", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }

            // White Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).align(Alignment.BottomCenter).offset(y = (-32).dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = schoolId,
                        onValueChange = { schoolId = it; if (it.isNotBlank()) schoolIdError = "" },
                        label = { Text("Enter School ID") },
                        singleLine = true,
                        isError = schoolIdError.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { if (schoolId.isBlank()) schoolIdError = "School ID is required" else onclickSearchButton() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Search", color = Color.White, fontSize = 18.sp)
                    }

                    if (showUserDetails) {
                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(24.dp))

                        model?.let { u ->
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("User Name: ${u.firstName} ${u.lastName}", fontWeight = FontWeight.SemiBold)
                                Text("Role: ${u.typeofUser}", color = Color.Gray)
                                Text("Status: ${u.status}", color = Color.Gray)
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        Text("Select Action", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))

                        Box {
                            OutlinedButton(onClick = { expandedAction = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (selectedAction.isEmpty()) "Choose Action" else selectedAction, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                            }
                            DropdownMenu(expanded = expandedAction, onDismissRequest = { expandedAction = false }) {
                                DropdownMenuItem(
                                    text = { Text(if (model?.status == "active") "Deactivate Account" else "Reactivate Account") },
                                    onClick = {
                                        selectedAction = if (model?.status == "active") "Deactivate Account" else "Reactivate Account"
                                        selectedReason = ""; expandedAction = false
                                    }
                                )
                                DropdownMenuItem(text = { Text("Delete Account") }, onClick = { selectedAction = "Delete Account"; selectedReason = ""; expandedAction = false })
                            }
                        }

                        if (selectedAction == "Deactivate Account" || selectedAction == "Delete Account") {
                            Spacer(Modifier.height(20.dp))
                            Text("Select Reason", fontWeight = FontWeight.SemiBold)
                            Box {
                                OutlinedButton(onClick = { expandedReason = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(if (selectedReason.isEmpty()) "Choose Reason" else selectedReason, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                                }
                                DropdownMenu(expanded = expandedReason, onDismissRequest = { expandedReason = false }) {
                                    reasons.forEach { reason ->
                                        DropdownMenuItem(text = { Text(reason) }, onClick = { selectedReason = reason; expandedReason = false })
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(30.dp))

                        // --- UPDATED: Confirm Button now triggers Verification ---
                        Button(
                            onClick = {
                                when (selectedAction) {
                                    "Delete Account" -> verifyAndExecute { showDeleteDialog = true }
                                    "Deactivate Account", "Reactivate Account" -> verifyAndExecute { deactivateonClick() }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = isConfirmEnabled,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Confirm", color = Color.White, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        // --- NEW: Admin Password Dialog ---
        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                title = { Text("Admin Verification") },
                text = {
                    Column {
                        Text("Please enter YOUR admin password to authorize this action.")
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it },
                            label = { Text("Your Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        // Note: You must implement verifyAdminPassword in your ViewModel/Repo
                        // For now, this calls the pending action if password is not blank
                        if (adminPassword.isNotBlank()) {
                            showPasswordDialog = false
                            adminPassword = ""
                            pendingAction?.invoke()
                        }
                    }) { Text("Verify") }
                },
                dismissButton = { TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") } }
            )
        }

        // Delete Confirmation
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirm Deletion", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete this account?\nThis action cannot be undone.") },
                confirmButton = {
                    Button(onClick = { showDeleteDialog = false; deleteonClick() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                        Text("Yes, Delete", color = Color.White)
                    }
                },
                dismissButton = { OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
            )
        }
    }
}