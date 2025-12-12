package com.example.busmate.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.R
import com.example.busmate.data.AdminActionsImpl
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.viewmodel.AdminActionsViewModel

class DriverProfileScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val selectMode = intent.getBooleanExtra("select_mode", false)

        setContent {
            DriverProfileMainScreen(selectMode)
        }
    }
}

@Composable
fun DriverProfileMainScreen(selectMode: Boolean) {

    val viewModel = remember { AdminActionsViewModel(AdminActionsImpl()) }
    val drivers = remember { mutableStateListOf<UserModel>() }

    LaunchedEffect(Unit) {
        viewModel.getAllDrivers { success, list ->
            if (success && list != null) drivers.addAll(list)
        }
    }

    DriverProfileScreenUI(drivers, selectMode)
}

@Composable
fun DriverProfileScreenUI(
    drivers: List<UserModel>,
    selectMode: Boolean
) {

    Scaffold { padding ->

        if (drivers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading drivers...", fontSize = 20.sp)
            }
            return@Scaffold
        }

        val pagerState = rememberPagerState(pageCount = { drivers.size })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) { page ->
            SingleDriverProfile(driver = drivers[page], selectMode = selectMode)
        }
    }
}

@Composable
fun SingleDriverProfile(
    driver: UserModel,
    selectMode: Boolean
) {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔵 Blue top header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(BusMateBlue)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.clickable { (context as Activity).finish() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "${driver.firstName} ${driver.lastName}",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "ID: ${driver.schoolId}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
        }

        // 🔳 White Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-130).dp)
                .then(
                    if (selectMode) Modifier.clickable {
                        // Set selected driver result
                        val result = Intent()
                        result.putExtra("driverId", driver.schoolId)
                        result.putExtra("driverName", "${driver.firstName} ${driver.lastName}")
                        (context as Activity).setResult(Activity.RESULT_OK, result)
                        context.finish()
                    }
                    else Modifier
                ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Profile image
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(4.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.driver),
                        contentDescription = "Driver Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (selectMode) "TAP TO SELECT THIS DRIVER" else "ABOUT DRIVER",
                    color = BusMateBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                DriverProfileItem(Icons.Default.Person, "Name: ${driver.firstName} ${driver.lastName}")
                DriverProfileItem(Icons.Default.Email, "Email: ${driver.email}")
                DriverProfileItem(Icons.Default.Phone, "Phone: ${driver.phone}")
                DriverProfileItem(Icons.Default.Badge, "School ID: ${driver.schoolId}")

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DriverProfileItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(text, fontSize = 16.sp)
    }
}
