package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.model.BusModel
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.viewmodel.BusViewModel

class SearchBusActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = BusViewModel(BusRepositoryImpl())
        setContent {
            BusMateTheme {
                SearchBusScreen(viewModel) { finish() }
            }

        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBusScreen(
    viewModel: BusViewModel,
    onBack: () -> Unit
) {
    val busMateBlue = Color(0xFF2567E8)
    val context = LocalContext.current
    val buses by viewModel.buses.collectAsState()
    var searchText by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        viewModel.observeAllBuses()
    }

    val filteredBuses = buses.filter {
        it.busNumber.contains(searchText, true) ||
                it.routeId.contains(searchText, true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Bus", color = Color.White, fontWeight = FontWeight.SemiBold) },
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
                .background(Color(0xFFF5F5F5))
        ) {
            // Blue header box matching Admin Search Student
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(busMateBlue)
                    .padding(horizontal = 24.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text(text = "Search by Bus Number or Route ID") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = busMateBlue)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = busMateBlue
                    ),
                    singleLine = true
                )
            }

            if (filteredBuses.isEmpty() && searchText.isNotEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No buses found matching '$searchText'", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-30).dp) // Overlap effect
                        .padding(horizontal = 20.dp)
                ) {
                    items(filteredBuses) { bus ->
                        BusItem(bus) {
                            context.startActivity(
                                Intent(context, EditBusActivity::class.java)
                                    .putExtra("bus_data", bus)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
@Composable
fun BusItem(bus: BusModel, onClick: () -> Unit) {
    val busMateBlue = Color(0xFF2567E8)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Content - Takes up the space since circle is removed
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bus Number: ${bus.busNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = "Route ID: ${bus.routeId}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            // Edit icon to indicate action
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = busMateBlue.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

