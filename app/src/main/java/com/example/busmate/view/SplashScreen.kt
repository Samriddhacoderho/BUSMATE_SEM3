package com.example.busmate.view

import android.app.Activity
import android.content.Context
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.busmate.R
import com.example.busmate.view.dashboard.ParentDashboardActivity
import com.example.busmate.ui.theme.BusMateBlue
import com.example.busmate.ui.theme.BusMateOrange
import kotlinx.coroutines.delay
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.busmate.model.UserModel
import kotlinx.coroutines.launch

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
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
    val isConnected=isInternetAvailable(context)
    val snackbarHostState= remember{ SnackbarHostState() }
    val coroutineScope= rememberCoroutineScope ()


    if(!isConnected){
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("No Internet Connected")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (isConnected) {

            delay(2000)

            val sharedPreferences = context.getSharedPreferences("User", Context.MODE_PRIVATE)
            val gson = com.google.gson.Gson()
            val userJson = sharedPreferences.getString("user_model", null)

            if (userJson != null) {
                val savedUser = gson.fromJson(userJson, UserModel::class.java)
                val intent = Intent(context, ParentDashboardActivity::class.java)
                intent.putExtra("model", savedUser)
                context.startActivity(intent)
                activity.finish()
            } else {
                val intent = Intent(context, LoginScreen::class.java)
                context.startActivity(intent)
                activity.finish()
            }

        }

        }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BusMateBlue,
        contentColor = Color.White,
        snackbarHost = {SnackbarHost(hostState = snackbarHostState){
            Snackbar(
                snackbarData = it,
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White)
        }
        }
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
            LottieAnimation(composition,{
                progress},
                modifier=Modifier.size(200.dp),
                )
        }
    }
}

@Preview
@Composable
fun PreviewFunc(){
    SplashScreenUI()
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)

    return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}
