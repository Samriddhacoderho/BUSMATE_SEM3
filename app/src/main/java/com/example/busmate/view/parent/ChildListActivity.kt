package com.example.busmate.view.parent

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.ChildRepositoryImpl
import com.example.busmate.model.ChildModel
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.viewmodel.ChildViewModel
import com.google.firebase.auth.FirebaseAuth // Added for Auth

class ChildListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ChildViewModel(ChildRepositoryImpl())

        // Get the current logged-in Parent's UID
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid

        setContent {
            BusMateTheme {
                // Use LaunchedEffect to start observing data when the screen opens
                LaunchedEffect(Unit) {
                    currentUid?.let { uid ->
                        viewModel.observeChildren(uid)
                    }
                }

                ChildListScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() },
                    onChildClick = { selectedChild ->
                        val intent = Intent(this, EditChildActivity::class.java).apply {
                            putExtra("CHILD_DATA", selectedChild)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildListScreen(
    viewModel: ChildViewModel,
    onBackClick: () -> Unit,
    onChildClick: (ChildModel) -> Unit
) {
    // --- ORIGINAL LOGIC PRESERVED ---
    val children by viewModel.children.collectAsState()
    val busMateBlue = Color(0xFF2567E8) // Color from AdminSearchChildActivity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Child", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                .background(Color(0xFFF5F5F5)) // Background color from Admin UI
        ) {
            // --- BLUE HEADER AREA (Matches Admin UI shape but NO Search Bar) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(busMateBlue)
            )

            // --- LIST CONTENT ---
            if (children.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = busMateBlue)
                        Spacer(Modifier.height(8.dp))
                        Text("Loading children...", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-30).dp) // Overlap effect from Admin UI
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(children) { child ->
                        ChildCard(child = child, onClick = { onChildClick(child) })
                    }
                }
            }
        }
    }
}
@Composable
fun ChildCard(child: ChildModel, onClick: () -> Unit) {
    val busMateBlue = Color(0xFF2567E8)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp), // Radius updated to match Admin UI
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    color = Color.Black
                )
                Text(
                    text = "ID: ${child.studentId}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Select",
                tint = busMateBlue.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}