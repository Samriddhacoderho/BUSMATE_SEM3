package com.example.busmate.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

    // Load children on start
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

    // Load bus/driver when child selection changes
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
        topBar = {
            TopAppBar(
                title = { Text("Driver Profile", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.onBackPressed() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F7F7)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "View driver for child:", fontSize = 14.sp, color = Color.Gray)

            LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                items(children) { child ->
                    FilterChip(
                        selected = selectedChild == child,
                        onClick = { selectedChild = child },
                        label = { Text("${child.firstName} ${child.lastName}") },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Black)
                }
            } else if (busDetails?.driver != null) {
                val driver = busDetails!!.driver!!

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- Driver Image ---
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!driver.profileImage.isNullOrEmpty()) {
                            AsyncImage(
                                model = driver.profileImage,
                                contentDescription = "Driver Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(2.dp, Color.LightGray, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Default Driver Icon",
                                modifier = Modifier.size(110.dp),
                                tint = Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- Driver Details Card ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DetailText(label = "First Name", value = driver.firstName)
                            DetailText(label = "Last Name", value = driver.lastName)
                            DetailText(label = "Email Address", value = driver.email)
                            DetailText(label = "Phone Number", value = driver.phone)
                            DetailText(label = "School ID", value = driver.schoolId)
                            DetailText(label = "Employment Status", value = driver.typeofUser ?: "Active")
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text("No driver assigned yet.", color = Color.Gray)
                }
            }
        }
    }
}
//testing driverprofileactivity