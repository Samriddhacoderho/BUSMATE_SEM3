package com.example.busmate.view.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.busmate.data.AdminActionsImpl
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.BusMateTheme
import com.example.busmate.viewmodel.AdminActionsViewModel

class DriverProfileScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val selectMode = intent.getBooleanExtra("select_mode", false)

        setContent {
            // 🌙 Dark mode observer (LOGIC ONLY)
            val context = LocalContext.current
            val sharedPrefs = remember {
                context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            }
            var themeChanged by remember {
                mutableStateOf(sharedPrefs.getInt("dark_mode_pref", 0))
            }

            DisposableEffect(Unit) {
                val listener =
                    android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                        if (key == "dark_mode_pref") {
                            themeChanged = sharedPrefs.getInt("dark_mode_pref", 0)
                        }
                    }
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            key(themeChanged) {
                BusMateTheme {
                    DriverProfileMainScreen(selectMode)
                }
            }
        }
    }
}

@Composable
fun DriverProfileMainScreen(selectMode: Boolean) {

    val viewModel = remember {
        AdminActionsViewModel(AdminActionsImpl(), UserRepositoryImpl())
    }

    val drivers = remember { mutableStateListOf<UserModel>() }

    LaunchedEffect(Unit) {
        viewModel.getAllDrivers { success, list ->
            if (success && list != null) drivers.addAll(list)
        }
    }

    DriverProfileScreenUI(drivers, selectMode)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileScreenUI(
    drivers: List<UserModel>,
    selectMode: Boolean
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { (context as Activity).finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BusMateBlue
                )
            )
        }
    ) { padding ->

        if (drivers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BusMateBlue)
            }
            return@Scaffold
        }

        val pagerState = rememberPagerState(pageCount = { drivers.size })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Text(
                text = "Swipe to view drivers (${pagerState.currentPage + 1}/${drivers.size})",
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .align(Alignment.CenterHorizontally),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                SingleDriverProfile(
                    driver = drivers[page],
                    selectMode = selectMode
                )
            }
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
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔵 Gradient Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            BusMateBlue,
                            BusMateBlue
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${driver.firstName} ${driver.lastName}",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "ID: ${driver.schoolId}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-60).dp)
                .then(
                    if (selectMode) Modifier.clickable {
                        val result = Intent()
                        result.putExtra("driverId", driver.schoolId)
                        result.putExtra(
                            "driverName",
                            "${driver.firstName} ${driver.lastName}"
                        )
                        (context as Activity).setResult(Activity.RESULT_OK, result)
                        context.finish()
                    } else Modifier
                ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {

            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (!driver.profileImage.isNullOrEmpty()) {
                        AsyncImage(
                            model = driver.profileImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = driver.firstName.take(1).uppercase(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = BusMateBlue
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = if (selectMode) "TAP TO SELECT DRIVER" else "DRIVER DETAILS",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = BusMateBlue
                )

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                DriverProfileItem(Icons.Default.Person, "Name: ${driver.firstName} ${driver.lastName}")
                DriverProfileItem(Icons.Default.Email, "Email: ${driver.email}")
                DriverProfileItem(Icons.Default.Phone, "Phone: ${driver.phone}")
                DriverProfileItem(Icons.Default.Badge, "School ID: ${driver.schoolId}")
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun DriverProfileItem(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BusMateBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = BusMateBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
