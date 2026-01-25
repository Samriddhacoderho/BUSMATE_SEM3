package com.example.busmate.view.parent

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
    // Observe the StateFlow from ViewModel
    val children by viewModel.children.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Select Child", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (children.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.padding(bottom = 8.dp))
                    Text("Loading children...", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(children) { child ->
                    ChildCard(child = child, onClick = { onChildClick(child) })
                }
            }
        }
    }
}

@Composable
fun ChildCard(child: ChildModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(Modifier.width(16.dp))
            Column {
                Text("${child.firstName} ${child.lastName}", fontWeight = FontWeight.Bold)
                Text("ID: ${child.studentId}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}