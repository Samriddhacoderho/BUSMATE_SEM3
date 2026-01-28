package com.example.busmate.view.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import com.example.busmate.model.ChildModel
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.ui.theme.isDarkMode
import com.example.busmate.viewmodel.ChildViewModel

class AdminSearchChildActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ChildViewModel(ChildRepositoryImpl())

        setContent {
            val context = LocalContext.current

            // --- FIX: ADD THEME REFRESH LISTENER ---
            val sharedPrefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
            var themeUpdateTrigger by remember { mutableIntStateOf(0) }

            DisposableEffect(Unit) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "dark_mode_pref") {
                        themeUpdateTrigger++ // Forces recomposition when setting changes
                    }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            // Wrapping with key ensures the whole theme block refreshes
            key(themeUpdateTrigger) {
                BusMateTheme(darkTheme = isDarkMode()) {
                    AdminSearchScreen(
                        viewModel = viewModel,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSearchScreen(viewModel: ChildViewModel, onBack: () -> Unit) {
    val busMateBlue = Color(0xFF2567E8)
    val context = LocalContext.current
    val childrenList by viewModel.children.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.observeAllChildren()
    }

    val filteredChildren = childrenList.filter {
        it.firstName.contains(searchQuery, ignoreCase = true) ||
                it.lastName.contains(searchQuery, ignoreCase = true) ||
                it.studentId.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Students", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                // FIX: Use Theme background instead of hardcoded 0xFFF5F5F5
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(busMateBlue)
                    .padding(horizontal = 24.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(text = "Enter student name or ID...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = busMateBlue)
                    },
                    colors = TextFieldDefaults.colors(
                        // FIX: Use surface color so it's not bright white in dark mode
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = busMateBlue,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )
            }

            if (filteredChildren.isEmpty() && searchQuery.isNotEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No students found matching '$searchQuery'", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-30).dp)
                        .padding(horizontal = 20.dp)
                ) {
                    items(filteredChildren) { child ->
                        AdminStudentCard(
                            child = child,
                            onClick = {
                                val intent = Intent(context, EditStudentActivity::class.java).apply {
                                    putExtra("student_data", child)
                                }
                                context.startActivity(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStudentCard(child: ChildModel, onClick: () -> Unit) {
    val busMateBlue = Color(0xFF2567E8)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        // FIX: Use MaterialTheme.colorScheme.surface for adaptive background
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${child.firstName} ${child.lastName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    // FIX: Use onSurface for adaptive text color
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${child.studentId}",
                    fontSize = 13.sp,
                    // FIX: Use onSurfaceVariant for muted gray/white text
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = busMateBlue.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}