package com.example.busmate.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busmate.data.ChildRepositoryImpl
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.model.ChildModel
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.utils.QRCodeGenerator
import com.example.busmate.viewmodel.ChildViewModel
import com.example.busmate.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class StudentIdCard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val studentId = intent.getStringExtra("STUDENT_ID")
        setContent {
            BusMateTheme {
                val userViewModel: UserViewModel = viewModel { UserViewModel(UserRepositoryImpl()) }
                val childViewModel: ChildViewModel = viewModel { ChildViewModel(ChildRepositoryImpl()) }

                LaunchedEffect(Unit) {
                    FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                        userViewModel.loadUserProfile(uid)
                        childViewModel.observeChildren(uid)
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    StudentIdRoute(
                        userViewModel = userViewModel,
                        childViewModel = childViewModel,
                        studentId = studentId,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentIdRoute(
    userViewModel: UserViewModel,
    childViewModel: ChildViewModel,
    studentId: String? = null,
    onBack: () -> Unit = {}
) {
    val userState by userViewModel.user.collectAsState()
    val childrenList by childViewModel.children.collectAsState()
    val message by userViewModel.message.collectAsState()

    val isLoading = message.contains("Loading") || userState == null
    val busMateBlue = Color(0xFF2567E8) // Updated color

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digital Student ID", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = busMateBlue)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = busMateBlue)
            } else if (childrenList.isEmpty()) {
                Text("No children data found.", modifier = Modifier.align(Alignment.Center))
            } else {
                if (!studentId.isNullOrEmpty()) {
                    val selectedChild = childrenList.find { it.studentId == studentId }
                    if (selectedChild != null) {
                        DigitalStudentIdContent(child = selectedChild, parent = userState)
                    } else {
                        Text("Child not found.", modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    val pagerState = rememberPagerState(pageCount = { childrenList.size })
                    Column {
                        Text(
                            text = "Swipe to view children (${pagerState.currentPage + 1}/${childrenList.size})",
                            modifier = Modifier.padding(12.dp).align(Alignment.CenterHorizontally),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                            DigitalStudentIdContent(child = childrenList[page], parent = userState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DigitalStudentIdContent(child: ChildModel, parent: UserModel?) {
    val busMateBlue = Color(0xFF2567E8) // Updated color
    val scrollState = rememberScrollState()

    val qrBitmap = remember(child, parent) {
        if (parent != null) {
            val content = QRCodeGenerator.generateFullDataString(child, parent)
            QRCodeGenerator.createQRCode(content)
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Blue Header Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(busMateBlue)
        )

        // White Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = (-60).dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                }
                Spacer(Modifier.height(10.dp))
                Text("STUDENT PROFILE", color = busMateBlue, fontWeight = FontWeight.Bold)
                HorizontalDivider(Modifier.padding(vertical = 10.dp))

                IdInfoRow("Name", "${child.firstName} ${child.lastName}")
                IdInfoRow("ID", child.studentId)
                IdInfoRow("Route", child.busRouteId)
                IdInfoRow("Parent", "${parent?.firstName ?: ""} ${parent?.lastName ?: ""}")
                IdInfoRow("Contact", parent?.phone ?: "N/A")
            }
        }

        // QR Code Section
        if (qrBitmap != null) {
            Column(
                modifier = Modifier
                    .padding(bottom = 40.dp)
                    .offset(y = (-30).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(180.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Scan for Verification",
                    fontSize = 12.sp,
                    color = busMateBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun IdInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("$label: ", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(80.dp))
        Text(value, fontSize = 13.sp, color = Color.DarkGray)
    }
}