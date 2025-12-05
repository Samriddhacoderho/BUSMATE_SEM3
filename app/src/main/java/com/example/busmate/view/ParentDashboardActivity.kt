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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busmate.R
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BackgroundLightGray
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.BusMateGreen
import com.example.busmate.ui.theme.BusMateOrange
import com.example.busmate.viewmodel.SupportViewModel


class ParentDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParentDashboardScreen()
        }
    }
}


@Composable
fun ParentDashboardScreen() {
    val context = LocalContext.current
    val activity = context as? Activity

    val userModel = activity?.intent?.getParcelableExtra<UserModel>("model")
    val parentName = userModel?.firstName ?: "Parent"

    data class NavItem(val label: String, val icon: Int)

    var selectedItem by remember { mutableStateOf(0) }

    val navList = listOf(
        NavItem("Home", R.drawable.baseline_home_24),
        NavItem("Support", R.drawable.baseline_support_24),
        NavItem("Location", R.drawable.baseline_location_on_24),
        NavItem("Setting", R.drawable.baseline_settings_24),
    )

//    val supportViewModel: SupportViewModel = viewModel()

    Scaffold(
        containerColor = BackgroundLightGray,

        // ---------------- TOP BAR FIXED ----------------
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.weight(0.5f)
                )
                Row(
                    Modifier.weight(0.5f),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        },

        // ---------------- BOTTOM NAVIGATION ----------------
        bottomBar = {
            NavigationBar {
                navList.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { paddingValues ->

        // ---------------- SCREEN SWITCHER ----------------
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedItem) {
                0 -> HomeScreen()
//                1 -> SupportScreen(viewModel = supportViewModel)
                2 -> LocationScreen()
                3 -> SettingScreen()
                else -> HomeScreen()
            }
        }
    }
}


//@Composable
//fun TopDashboardBar(){
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//        ,
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.Center
//    ){
//        Image(
//            painter = painterResource(R.drawable.logo),
//            contentDescription = null,
//            modifier=Modifier.weight(0.5f)
//        )
//        Row(Modifier.weight(0.5f),
//            horizontalArrangement = Arrangement.End
//            ) {
//            IconButton(onClick = {}) {
//                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray)
//            }
//            IconButton(onClick = {}) {
//                Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.Red.copy(0.8f))
//            }
//        }
//    }
//}

//@Composable
//fun WelcomeCard(parentName: String?){
//    Column(Modifier
//        .fillMaxWidth()
//        .padding(16.dp)
//        .clip(RoundedCornerShape(20.dp))
//        .background(BusMateBlue)
//        .padding(16.dp)) {
//        Text(
//            text = "Welcome, $parentName!",
//            color = Color.White,
//            fontWeight = FontWeight.Bold,
//            fontSize = 22.sp,
//        )
//        Row(Modifier
//            .padding(top = 20.dp)
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(20.dp))
//            .background(Color.Gray)
//            .height(40.dp)) {
//            Row(Modifier
//                .fillMaxHeight()
//                .weight(0.5f)
//                .background(BusMateOrange),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.Center
//                ) {
//                Icon(Icons.Filled.Home,contentDescription = null)
//                Spacer(Modifier.width(5.dp))
//                Text("School",
//                    color = Color.White,
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 19.sp,
//                    )
//            }
//            Row(Modifier
//                .fillMaxHeight()
//                .weight(0.5f),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.Center
//                ) {
//                Icon(Icons.Filled.LocationOn,contentDescription = null, tint = Color.Cyan)
//                Spacer(Modifier.width(5.dp))
//                Text("Tracking Live",
//                    color = Color.White,
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 18.sp,
//                    )
//            }
//        }
//    }
//}
//
//
//@Composable
//fun MyChildrenHeader() {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = "My Children",
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color.Black.copy(alpha = 0.8f)
//        )
//        OutlinedButton(
//            onClick = {},
//            shape = RoundedCornerShape(20.dp),
//            colors = ButtonDefaults.outlinedButtonColors(
//                contentColor = BusMateBlue
//            ),
//            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
//            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
//            modifier = Modifier.height(35.dp)
//        ) {
//            Icon(
//                Icons.Filled.Add,
//                contentDescription = "Add Child",
//                modifier = Modifier
//                    .size(16.dp)  // Adjust the size as necessary
//                    .background(Color.Red, shape = CircleShape)  // Use CircleShape for full rounding
//                    .clip(CircleShape)  // Ensures the icon is clipped to a circle
//            )
//
//            Spacer(modifier = Modifier.width(4.dp))
//            Text("Add Child", fontSize = 14.sp)
//        }
//    }
//}
//
//@Composable
//fun ChildTrackingCard(childName: String,statusText: String,
//                      subText: String,
//                      statusColor: Color,
//                      imageResource: Int,
//                      mapImageResource: Int){
//    Card(modifier = Modifier.fillMaxWidth().height(150.dp).padding(horizontal = 16.dp, vertical = 5.dp),
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//        ){
//        Row(Modifier.fillMaxSize().padding(8.dp),
//            verticalAlignment = Alignment.CenterVertically
//            ) {
//            //Left Content:Child Information
//            Row(Modifier.weight(0.8f),
//                verticalAlignment = Alignment.CenterVertically
//                ) {
//                Image(painter = painterResource(imageResource),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .size(70.dp)
//                        .clip(CircleShape)
//                        .border(1.dp, Color.LightGray, CircleShape))
//                Spacer(modifier = Modifier.width(10.dp))
//                Column {
//                    Text(
//                        text = childName,
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 16.sp,
//                        color = Color.Black
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Card(
//                        shape = RoundedCornerShape(8.dp),
//                        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.8f))
//                    ) {
//                        Column(
//                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//                        ) {
//                            Text(
//                                text = statusText,
//                                color = Color.White,
//                                fontWeight = FontWeight.SemiBold,
//                                fontSize = 12.sp
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Text(
//                        text = subText,
//                        color = Color.Gray,
//                        fontSize = 12.sp,
//                        lineHeight = 14.sp
//                    )
//                }
//                }
//            //Right Content:Map
//            Row(Modifier.weight(0.2f)) {
//                Image(
//                    painter = painterResource(id = mapImageResource),
//                    contentDescription = "Map or School Visual",
//                    modifier = Modifier
//                        .size(80.dp)
//                        .clip(RoundedCornerShape(8.dp))
//                        .clickable {}
//                )
//            }
//            }
//
//
//        }
//    }
//
//@Composable
//fun NotificationsAlertHeader(){
//    Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 5.dp)) {
//        Text(
//            text = "Notifications & Alerts",
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color.Black.copy(alpha = 0.8f)
//        )
//    }
//    Spacer(modifier = Modifier.height(10.dp))
//        NotificationItem(
//            initial = "S",
//            message = "School Closed on Friday",
//            indicatorColor = BusMateOrange
//        )
//
//}
//
//@Composable
//fun NotificationItem(initial:String,message:String,indicatorColor:Color){
//    Card(
//        modifier = Modifier
//            .fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
//            .height(70.dp),
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 16.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                // Initial Circle
//                Box(
//                    modifier = Modifier
//                        .size(40.dp)
//                        .clip(CircleShape)
//                        .background(indicatorColor.copy(alpha = 0.1f))
//                        .border(1.dp, indicatorColor.copy(alpha = 0.4f), CircleShape),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = initial,
//                        color = indicatorColor,
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 18.sp
//                    )
//                }
//                Spacer(modifier = Modifier.width(16.dp))
//                Column {
//                    Text(
//                        text = message,
//                        fontWeight = FontWeight.Medium,
//                        fontSize = 14.sp,
//                        color = Color.Black
//                    )
//                }
//            }
//            Icon(
//                painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24), // Placeholder
//                contentDescription = "View notification",
//                tint = Color.Blue,
//                modifier = Modifier.size(24.dp)
//            )
//        }
//    }
//}
//
//
//@Preview(showSystemUi = true, showBackground = true)
//@Composable
//fun PreviewParentDashboardScreen() {
//    ParentDashboardScreen()
//
//}
