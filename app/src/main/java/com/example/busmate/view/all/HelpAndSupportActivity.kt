package com.example.busmate.view.all

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.SupportRepositoryImpl
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.ui.theme.isDarkMode
import com.example.busmate.viewmodel.SupportViewModel
import com.example.busmate.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class HelpAndSupportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val sharedPrefs = remember {
                context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            }
            var themeUpdateTrigger by remember { mutableIntStateOf(0) }

            DisposableEffect(Unit) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "dark_mode_pref") {
                        themeUpdateTrigger++
                    }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            key(themeUpdateTrigger) {
                val supportViewModel = remember { SupportViewModel(SupportRepositoryImpl()) }
                val userViewModel = remember { UserViewModel(UserRepositoryImpl()) }

                BusMateTheme(darkTheme = isDarkMode()) {
                    SupportScreen(
                        viewModel = supportViewModel,
                        userViewModel = userViewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    viewModel: SupportViewModel,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val activity = context as Activity

    val intentModel = activity.intent.getParcelableExtra<UserModel>("model")
    val userState by userViewModel.user.collectAsState()
    val model = intentModel ?: userState

    val supportMessages by viewModel.supportMessages.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var titleText by remember { mutableStateOf("") }
    var explainText by remember { mutableStateOf("") }

    val customTextFieldColors = TextFieldDefaults.colors(
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )

    LaunchedEffect(Unit) {
        if (intentModel == null) {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            userViewModel.loadUserProfile(currentUid)
        }
        viewModel.fetchSupportMessages()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Help & Support", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BusMateBlue)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (model?.typeofUser == "Parent" || model?.typeofUser == "Driver") {
                val userSupportMessages = supportMessages.filter { it.uid == model.uid }

                items(userSupportMessages) { UserSupportItem(it) }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Submit a Support Request", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BusMateBlue)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = titleText,
                                onValueChange = { titleText = it },
                                placeholder = { Text("Title") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = customTextFieldColors,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = explainText,
                                onValueChange = { explainText = it },
                                placeholder = { Text("Explain your issue") },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                colors = customTextFieldColors,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (titleText.isNotEmpty() && explainText.isNotEmpty()) {
                                        viewModel.writeReport("${model.firstName} ${model.lastName}", model.typeofUser, titleText, explainText)
                                        titleText = ""; explainText = ""
                                    } else {
                                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BusMateBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("SUBMIT", color = Color.White, fontWeight = FontWeight.Bold) }
                        }
                    }
                }

            } else if (model?.typeofUser == "Admin") {
                val groupedMessages = supportMessages.groupBy { it.uid }

                items(groupedMessages.keys.toList()) { userUid ->
                    val userMessages = groupedMessages[userUid] ?: emptyList()
                    val firstMsg = userMessages.first()

                    AdminUserGroupCard(
                        userName = firstMsg.name,
                        userType = firstMsg.typeofUser ?: "User",
                        messages = userMessages,
                        viewModel = viewModel,
                        textFieldColors = customTextFieldColors
                    )
                }
            }
        }
    }
}

@Composable
fun AdminUserGroupCard(
    userName: String,
    userType: String,
    messages: List<com.example.busmate.model.SupportModel>,
    viewModel: SupportViewModel,
    textFieldColors: TextFieldColors
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("User: $userName ($userType)", fontWeight = FontWeight.Bold, color = BusMateBlue, fontSize = 18.sp)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            messages.forEach { support ->
                var adminReply by remember { mutableStateOf("") }

                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Text("Title: ${support.title}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Message: ${support.message}", modifier = Modifier.padding(bottom = 4.dp), color = MaterialTheme.colorScheme.onSurface)

                    if (support.reply.isNotEmpty()) {
                        Text("Current Reply: ${support.reply}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = adminReply,
                        onValueChange = { adminReply = it },
                        placeholder = { Text("Type reply for this ticket...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            if (adminReply.isNotBlank()) {
                                viewModel.replyToSupport(support.supportId, adminReply)
                                adminReply = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = BusMateBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        Text("REPLY", color = Color.White, fontSize = 12.sp)
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun UserSupportItem(support: com.example.busmate.model.SupportModel) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // User Message Card (Adaptive Colors)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Title: ${support.title}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = support.message,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Admin Reply Card (Adaptive Colors)
        if (support.reply.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, start = 20.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Admin Reply:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary, // Distinct color for "Admin Reply" label
                        fontSize = 13.sp
                    )
                    Text(
                        text = support.reply,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}