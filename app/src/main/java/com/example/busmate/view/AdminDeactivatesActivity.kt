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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.material3.Divider


class AdminDeactivatesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

        }
    }
}

@Composable
fun AdminManageAccountScreen() {

    // -------------------- State Variables --------------------
    var schoolId by remember { mutableStateOf("") }
    var schoolIdError by remember { mutableStateOf("") }

    var showUserDetails by remember { mutableStateOf(false) }

    // ---- Action type ----
    var selectedAction by remember { mutableStateOf("") } // "Deactivate" or "Delete"
    var expandedAction by remember { mutableStateOf(false) }

    // ---- Reason for action ----
    val reasons = listOf(
        "User requested deactivation",
        "Violation of rules",
        "Fraudulent activity",
        "Inactive for long time",
        "Other"
    )

    var selectedReason by remember { mutableStateOf("") }
    var expandedReason by remember { mutableStateOf(false) }

    val isConfirmEnabled = selectedAction.isNotBlank() && selectedReason.isNotBlank()

    // -------------------- Colors --------------------
    val PrimaryBlue = Color(0xFF2567E8)

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // TOP BLUE SECTION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .background(BusMateBlue),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Manage User Accounts",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Enter School ID to find the parent/driver account",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
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
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ------------ SCHOOL ID FIELD ------------
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
                            text = schoolIdError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ------------ SEARCH BUTTON ------------
                    Button(
                        onClick = {
                            if (schoolId.isBlank()) {
                                schoolIdError = "School ID is required"
                            } else {
                                showUserDetails = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = "Search",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }

                    // ----------------------------------------------------
                    //            USER DETAILS SECTION (SHOWN AFTER SEARCH)
                    // ----------------------------------------------------
                    if (showUserDetails) {
                        Spacer(Modifier.height(24.dp))

                        Divider(color = Color.LightGray)

                        Spacer(Modifier.height(24.dp))

                        // ------- Dummy User preview UI --------
                        // Replace with real backend data later
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("User Name: John Doe", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("Role: Parent", fontSize = 14.sp, color = Color.Gray)
                            Text("Status: Active", fontSize = 14.sp, color = Color.Gray)
                        }

                        Spacer(Modifier.height(24.dp))

                        // ------------ ACTION DROPDOWN (Deactivate/Delete) ------------
                        Text("Select Action", fontWeight = FontWeight.SemiBold)

                        Spacer(Modifier.height(8.dp))

                        Box {
                            OutlinedButton(
                                onClick = { expandedAction = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = ButtonDefaults.outlinedButtonBorder,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                            ) {
                                Text(
                                    text = if (selectedAction.isEmpty()) "Choose Action" else selectedAction,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            }

                            DropdownMenu(
                                expanded = expandedAction,
                                onDismissRequest = { expandedAction = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Deactivate Account") },
                                    onClick = {
                                        selectedAction = "Deactivate Account"
                                        expandedAction = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Account") },
                                    onClick = {
                                        selectedAction = "Delete Account"
                                        expandedAction = false
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ------------ REASON DROPDOWN ------------
                        Text("Select Reason", fontWeight = FontWeight.SemiBold)

                        Spacer(Modifier.height(8.dp))

                        Box {
                            OutlinedButton(
                                onClick = { expandedReason = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = ButtonDefaults.outlinedButtonBorder,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                            ) {
                                Text(
                                    text = if (selectedReason.isEmpty()) "Choose Reason" else selectedReason,
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

                        // ------------ CONFIRM BUTTON ------------
                        Button(
                            onClick = {
                                // TODO: Connect to backend deactivate/delete endpoint
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = isConfirmEnabled,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                disabledContainerColor = Color.LightGray
                            )
                        ) {
                            Text(
                                text = "Confirm",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun previewDeactivate(){
    AdminManageAccountScreen()
}