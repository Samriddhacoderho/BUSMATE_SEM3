package com.example.busmate.view
import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.R
import com.example.busmate.model.UserModel
import com.example.busmate.ui.theme.BackgroundLightGray
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.BusMateGreen
import com.example.busmate.ui.theme.BusMateOrange

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val activity = context as Activity

    var parentName by remember {
        mutableStateOf(activity.intent.getParcelableExtra<UserModel>("model"))
    }

    Scaffold(containerColor = BackgroundLightGray) { paddingValues ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
//                TopDashboardBarScreen()

                WelcomeCardScreen(parentName?.firstName + " " + parentName?.lastName)

                MyChildrenHeaderScreen()

                ChildTrackingCardScreen(
                    childName = "Swikrit Ghimire",
                    statusText = "Reached School",
                    subText = "Bus No: 1511\n2 min ago",
                    statusColor = BusMateGreen,
                    imageResource = R.drawable.boy,
                    mapImageResource = R.drawable.school
                )

                ChildTrackingCardScreen(
                    childName = "Shahana Katwal",
                    statusText = "In Bus",
                    subText = "Bus No: 1533\n8 min ago",
                    statusColor = BusMateOrange,
                    imageResource = R.drawable.girl,
                    mapImageResource = R.drawable.map
                )

//                NotificationsAlertHeaderScreen()
//
//                Button(
//                    onClick = {
//                        val intent = Intent(context, SupportActivity::class.java)
//                        intent.putExtra("model", parentName)
//                        context.startActivity(intent)
//                    }
//                ) {
//                    Text("Click to open support page")
//                }
            }
        }
    }
}

//@Composable
//fun TopDashboardBarScreen() {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.Center
//    ) {
//        Image(
//            painter = painterResource(R.drawable.logo),
//            contentDescription = null,
//            modifier = Modifier.weight(0.5f)
//        )
//
//        Row(
//            Modifier.weight(0.5f),
//            horizontalArrangement = Arrangement.End
//        ) {
//            IconButton(onClick = {}) {
//                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray)
//            }
//
//            IconButton(onClick = {}) {
//                Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.Red.copy(0.8f))
//            }
//        }
//    }
//}

@Composable
fun WelcomeCardScreen(parentName: String?) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BusMateBlue)
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome, $parentName!",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )

        Row(
            Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Gray)
                .height(40.dp)
        ) {
            Row(
                Modifier
                    .fillMaxHeight()
                    .weight(0.5f)
                    .background(BusMateOrange),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Home, contentDescription = null)
                Spacer(Modifier.width(5.dp))
                Text("School", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
            }

            Row(
                Modifier
                    .fillMaxHeight()
                    .weight(0.5f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.Cyan)
                Spacer(Modifier.width(5.dp))
                Text("Tracking Live", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun MyChildrenHeaderScreen() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "My Children",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.8f)
        )

        OutlinedButton(
            onClick = {},
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BusMateBlue),
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(35.dp)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add Child",
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Red, shape = CircleShape)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Child", fontSize = 14.sp)
        }
    }
}

@Composable
fun ChildTrackingCardScreen(
    childName: String,
    statusText: String,
    subText: String,
    statusColor: Color,
    imageResource: Int,
    mapImageResource: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                Modifier.weight(0.8f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(imageResource),
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.LightGray, CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = childName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.8f))
                    ) {
                        Column(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = statusText,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subText,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Row(Modifier.weight(0.2f)) {
                Image(
                    painter = painterResource(id = mapImageResource),
                    contentDescription = "Map or School Visual",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {}
                )
            }
        }
    }
}

@Composable
fun NotificationsAlertHeaderScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 5.dp)
    ) {
        Text(
            text = "Notifications & Alerts",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.8f)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // FIXED FUNCTION NAME
    NotificationItemScreen(
        initial = "S",
        message = "School Closed on Friday",
        indicatorColor = BusMateOrange
    )
}

@Composable
fun NotificationItemScreen(initial: String, message: String, indicatorColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .height(70.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(indicatorColor.copy(alpha = 0.1f))
                        .border(1.dp, indicatorColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        color = indicatorColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = message,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }

            Icon(
                painter = painterResource(id = R.drawable.outline_arrow_forward_ios_24),
                contentDescription = "View notification",
                tint = Color.Blue,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
