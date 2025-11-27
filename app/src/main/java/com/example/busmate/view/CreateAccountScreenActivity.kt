package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// COLORS
val PrimaryBlueLight = Color(0xFF4C98FB)
val PrimaryBlueDark = Color(0xFF1E88E5)
val DarkBlueBackground = Color(0xFF1C5F9D)

class CreateAccountScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CreateAccountScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen() {

    // Focus Requesters for inputs
    val focusUsername = remember { FocusRequester() }
    val focusFullName = remember { FocusRequester() }
    val focusCountry = remember { FocusRequester() }
    val focusEmail = remember { FocusRequester() }
    val focusPhone = remember { FocusRequester() }
    val focusPassword = remember { FocusRequester() }

    // State for input fields
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var termsAgreed by remember { mutableStateOf(false) }

    // Dropdown menu state for role selection
    var expanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("Select Role") }
    val roles = listOf("Parent", "Driver")

    val scrollState = rememberScrollState()

    Scaffold(containerColor = Color.White) { padding ->

        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // TOP WAVE
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(bottomEnd = 120.dp))
                    .background(DarkBlueBackground)
            )

            // BOTTOM WAVE
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 120.dp))
                    .background(DarkBlueBackground)
            )

            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(140.dp))

                // Title and dropdown Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 25.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Create Account",
                        color = DarkBlueBackground,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // DROPDOWN MENU for Role selection
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = selectedRole,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .width(150.dp)
                                .menuAnchor(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = DarkBlueBackground,
                                unfocusedTextColor = DarkBlueBackground
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            roles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role) },
                                    onClick = {
                                        selectedRole = role
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // USER INPUTS
                InputField(
                    value = username,
                    placeholder = "Username",
                    onValueChange = { username = it },
                    focusRequester = focusUsername,
                    onNext = { focusFullName.requestFocus() }
                )
                Spacer(Modifier.height(14.dp))

                InputField(
                    value = fullName,
                    placeholder = "Full name",
                    onValueChange = { fullName = it },
                    focusRequester = focusFullName,
                    onNext = { focusCountry.requestFocus() }
                )
                Spacer(Modifier.height(14.dp))

                InputField(
                    value = country,
                    placeholder = "Country",
                    onValueChange = { country = it },
                    focusRequester = focusCountry,
                    onNext = { focusEmail.requestFocus() }
                )
                Spacer(Modifier.height(14.dp))

                InputField(
                    value = email,
                    placeholder = "E-mail",
                    keyboardType = KeyboardType.Email,
                    onValueChange = { email = it },
                    focusRequester = focusEmail,
                    onNext = { focusPhone.requestFocus() }
                )
                Spacer(Modifier.height(14.dp))

                InputField(
                    value = phoneNumber,
                    placeholder = "Phone number",
                    keyboardType = KeyboardType.Number,
                    onValueChange = { phoneNumber = it },
                    focusRequester = focusPhone,
                    onNext = { focusPassword.requestFocus() }
                )
                Spacer(Modifier.height(14.dp))

                InputField(
                    value = password,
                    placeholder = "Password",
                    isPassword = true,
                    onValueChange = { password = it },
                    focusRequester = focusPassword,
                    onNext = {}
                )
                Spacer(Modifier.height(20.dp))

                // 🌟 FIXED TERMS & CONDITIONS — SAME STYLE AS YOUR FIRST CODE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Checkbox(
                        checked = termsAgreed,
                        onCheckedChange = { termsAgreed = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryBlueDark,
                            uncheckedColor = PrimaryBlueDark,
                            checkmarkColor = Color.White
                        )
                    )

                    Text(
                        text = "I agree to Terms & Conditions",
                        color = PrimaryBlueDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(40.dp))

                // CREATE ACCOUNT BUTTON
                Button(
                    onClick = {
                        // Handle create account
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(PrimaryBlueDark)
                ) {
                    Text(
                        "Create account",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun InputField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    focusRequester: FocusRequester,
    keyboardType: KeyboardType = KeyboardType.Text,
    onNext: () -> Unit
) {

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, color = Color.White.copy(alpha = 0.8f))
        },
        singleLine = true,
        shape = RoundedCornerShape(25.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .focusRequester(focusRequester),
        visualTransformation =
            if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { onNext() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = PrimaryBlueLight,
            unfocusedContainerColor = PrimaryBlueLight,
            cursorColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCreateAccountUI() {
    CreateAccountScreen()
}
