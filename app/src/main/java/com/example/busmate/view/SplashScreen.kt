package com.example.busmate.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busmate.R
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.BusMateOrange
import kotlinx.coroutines.delay

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashScreenUI()
        }
    }
}

@Composable
fun SplashScreenUI() {
    val context= LocalContext.current
    val activity=context as Activity

    LaunchedEffect(Unit) {
        delay(2000)
        val intent = Intent(
            context, LoginScreen::class.java
        )
        context.startActivity(intent)
        activity.finish()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BusMateBlue,
        contentColor = Color.White
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Logo Image
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.6f) // Logo size adjustment
                    .height(100.dp),
                colorFilter = ColorFilter.tint(BusMateOrange)
            )

            // "Powered by SSK Tech" text
            Text(
                text = "Powered by SSK Tech",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

@Preview
@Composable
fun PreviewFunc(){
    SplashScreenUI()
}
