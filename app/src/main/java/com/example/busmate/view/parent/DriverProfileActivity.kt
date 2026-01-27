package com.example.busmate.view.parent

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.data.ChildRepositoryImpl
import com.example.busmate.model.BusModel
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.viewmodel.BusViewModel
import com.example.busmate.viewmodel.ChildViewModel
import com.google.firebase.auth.FirebaseAuth
import coil3.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

class DriverProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val busViewModel = BusViewModel(BusRepositoryImpl())
        val childViewModel = ChildViewModel(ChildRepositoryImpl())

        setContent {
            BusMateTheme {
                DriverProfileScreen(busViewModel, childViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileScreen(busViewModel: BusViewModel, childViewModel: ChildViewModel) {
    val context = LocalContext.current
    val children by childViewModel.children.collectAsState()
    var selectedChild by remember { mutableStateOf(children.firstOrNull()) }
    var busDetails by remember { mutableStateOf<BusModel?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // --- LOGIC PRESERVED (NO CHANGES) ---
    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            childViewModel.observeChildren(it)
        }
    }
    LaunchedEffect(children) {
        if (selectedChild == null && children.isNotEmpty()) {
            selectedChild = children.first()
        }
    }
    LaunchedEffect(selectedChild) {
        selectedChild?.let { child ->
            isLoading = true
            busViewModel.getBusByRouteId(child.busRouteId) { fetchedBus ->
                busDetails = fetchedBus
                isLoading = false
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF7F7F7)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 🔵 BLUE HEADER AREA (Matching BusProfile/Admin UI) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp) // Height enough to hold title, child selector, and top of card
                    .background(Color(0xFF2854D8)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(40.dp))

                // Integrated Top Bar Content
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { (context as? Activity)?.onBackPressed() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Driver Profile",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(text = "Select Child", fontSize = 14.sp, color = Color.White.copy(0.8f),fontWeight = FontWeight.SemiBold)
                LazyRow(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                    items(children) { child ->
                        FilterChip(
                            modifier = Modifier.padding(end = 8.dp),
                            selected = selectedChild == child,
                            onClick = { selectedChild = child },
                            label = { Text("${child.firstName} ${child.lastName}",fontWeight = FontWeight.SemiBold) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White,
                                selectedLabelColor = Color(0xFF2854D8),
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }

            // --- 🔳 OVERLAPPING WHITE CARD ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-40).dp), // The Overlap
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        Box(Modifier.height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF2854D8))
                        }
                    } else if (busDetails?.driver != null) {
                        val driver = busDetails!!.driver!!

                        // --- Driver Image Section ---
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F3F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!driver.profileImage.isNullOrEmpty()) {
                                AsyncImage(
                                    model = driver.profileImage,
                                    contentDescription = "Driver Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Default Driver Icon",
                                    modifier = Modifier.size(80.dp),
                                    tint = Color.LightGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // --- Details List ---
                        DetailText(label = "First Name", value = driver.firstName)
                        DetailText(label = "Last Name", value = driver.lastName)
                        DetailText(label = "Email Address", value = driver.email)
                        DetailText(label = "Phone Number", value = driver.phone)
                        DetailText(label = "School ID", value = driver.schoolId)
                        DetailText(label = "Status", value = driver.typeofUser ?: "Active")

                    } else {
                        Box(Modifier.padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No driver assigned yet.", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
