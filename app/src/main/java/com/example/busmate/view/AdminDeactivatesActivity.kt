package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.ui.theme.BusMateBlue
import androidx.compose.runtime.rememberCoroutineScope
import com.example.busmate.data.AdminActionsImpl
import com.example.busmate.model.UserModel
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

@Composable
fun AdminManageAccountScreen() {
    val viewModel = remember { AdminActionsViewModel(AdminActionsImpl()) }

    // -------------------- State Variables --------------------
    var schoolId by remember { mutableStateOf("") }
    var schoolIdError by remember { mutableStateOf("") }
    var model by remember { mutableStateOf<UserModel?>(null) }

    var showUserDetails by remember { mutableStateOf(false) }
    var messageShow by remember { mutableStateOf("") }

    // Action type
    var selectedAction by remember { mutableStateOf("") }
    var expandedAction by remember { mutableStateOf(false) }

    // Reason dropdown
    val reasons = listOf(
        "User requested deactivation",
        "Violation of rules",
        "Fraudulent activity",
        "Inactive for long time",
        "Other"
    )

    var selectedReason by remember { mutableStateOf("") }
    var expandedReason by remember { mutableStateOf(false) }

    // Delete Dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val PrimaryBlue = Color(0xFF2567E8)

    // Confirm button enable logic
    val isConfirmEnabled =
        if (selectedAction == "Reactivate Account") {
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
                messageShow="User not found"
            }

        }
    }

    fun deactivateonClick() {
        if (model != null && model?.status=="active") {
            viewModel.deactivateAccount(schoolId) { success, message ->
                coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                messageShow = message
            }
        }else{
            viewModel.reactivateAccount(schoolId){success,message->
                coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                messageShow=message
            }
        }
        selectedAction = ""
        selectedReason = ""
        showUserDetails = false
    }

    fun deleteonClick() {
        if (model != null) {
            viewModel.deleteAccount(schoolId) { success, message ->
                coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                messageShow = message
            }
        }
        selectedAction = ""
        selectedReason = ""
        showUserDetails = false
    }

    // ---------------------- UI ----------------------

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor =
                        if (messageShow == "User Deleted" || messageShow == "User Deactivated" || messageShow=="User Reactivated")
                            Color.Green else Color.Red,
                    contentColor = Color.White
                )
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // TOP HEADER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .background(BusMateBlue),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Manage User Accounts",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Enter School ID to find the parent/driver account",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            // WHITE CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-32).dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // SCHOOL ID
                    OutlinedTextField(
                        value = schoolId,
                        onValueChange = {
                            schoolId = it
                            if (it.isNotBlank()) schoolIdError = ""
                        },
                        label = { Text("Enter School ID") },
                        singleLine = true,
                        isError = schoolIdError.isNotEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            errorBorderColor = Color.Red,
                            errorLabelColor = Color.Red
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (schoolIdError.isNotEmpty()) {
                        Text(
                            schoolIdError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // SEARCH
                    Button(
                        onClick = {
                            if (schoolId.isBlank()) {
                                schoolIdError = "School ID is required"
                            } else onclickSearchButton()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Search", color = Color.White, fontSize = 18.sp)
                    }

                    // ------------------ USER DETAILS ------------------
                    if (showUserDetails) {

                        Spacer(Modifier.height(24.dp))
                        Divider()
                        Spacer(Modifier.height(24.dp))

                        model?.let { u ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("User Name: ${u.firstName} ${u.lastName}", fontWeight = FontWeight.SemiBold)
                                Text("Role: ${u.typeofUser}", color = Color.Gray)
                                Text("Status: ${u.status}", color = Color.Gray)
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // ---------- ACTION DROPDOWN ----------
                        Text("Select Action", fontWeight = FontWeight.SemiBold)

                        Spacer(Modifier.height(8.dp))

                        Box {
                            OutlinedButton(
                                onClick = { expandedAction = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                            ) {
                                Text(
                                    if (selectedAction.isEmpty()) "Choose Action" else selectedAction,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            }

                            DropdownMenu(
                                expanded = expandedAction,
                                onDismissRequest = { expandedAction = false }
                            ) {

                                // Deactivate or Reactivate
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (model?.status == "active")
                                                "Deactivate Account"
                                            else
                                                "Reactivate Account"
                                        )
                                    },
                                    onClick = {
                                        selectedAction =
                                            if (model?.status == "active")
                                                "Deactivate Account"
                                            else
                                                "Reactivate Account"
                                        selectedReason = ""
                                        expandedAction = false
                                    }
                                )

                                // Delete
                                DropdownMenuItem(
                                    text = { Text("Delete Account") },
                                    onClick = {
                                        selectedAction = "Delete Account"
                                        selectedReason = ""
                                        expandedAction = false
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ---------- REASON (ONLY FOR DELETE/DEACTIVATE) ----------
                        if (selectedAction == "Deactivate Account" ||
                            selectedAction == "Delete Account"
                        ) {
                            Text("Select Reason", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))

                            Box {
                                OutlinedButton(
                                    onClick = { expandedReason = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                                ) {
                                    Text(
                                        if (selectedReason.isEmpty()) "Choose Reason" else selectedReason,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                }

                                DropdownMenu(
                                    expanded = expandedReason,
                                    onDismissRequest = { expandedReason = false }
                                ) {
                                    reasons.forEach { reason ->
                                        DropdownMenuItem(
                                            text = { Text(reason) },
                                            onClick = {
                                                selectedReason = reason
                                                expandedReason = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(30.dp))
                        }

                        // ---------- CONFIRM BUTTON ----------
                        Button(
                            onClick = {
                                when (selectedAction) {
                                    "Delete Account" -> showDeleteDialog = true
                                    "Deactivate Account" -> deactivateonClick()
                                    "Reactivate Account" -> deactivateonClick()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = isConfirmEnabled,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                disabledContainerColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm", color = Color.White, fontSize = 18.sp)
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }

        // ---------- DELETE CONFIRMATION POPUP ----------
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text("Confirm Deletion", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "Are you sure you want to delete this account?\n\nThis action cannot be undone.",
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            deleteonClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Yes, Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewDeactivate() {
    AdminManageAccountScreen()
}
