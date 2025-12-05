package com.example.busmate.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// --- WebView Imports for Embedded Map ---
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
// ----------------------------------------

// --- Colors and Constants ---
private object LiveLocationColors {
    val BackgroundColor = Color(0xFFF0F6F7)
    val TitleColor = Color(0xFF333333)
    val CardOrange = Color(0xFFE4904C)
    val CardGreen = Color(0xFF67B774)
    val BusIconColor = Color(0xFFC70039) // Reddish color for the bus icon on map
}



/**
 * The main Composable function for the Live Location Tracking UI.
 */
@Composable
fun LiveLocationScreen() {
    // Use constants from the object
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = LiveLocationColors.BackgroundColor // Light background color
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Title
            Text(
                text = "Live Location Tracking",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = LiveLocationColors.TitleColor,
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 24.dp)
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Start)
            )

            // 2. Map Area (Embedded WebView)
            MapPrototype(modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // 3. User Label
            Text(
                text = "Aliza Regmi",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = LiveLocationColors.TitleColor,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Cards Row - Now horizontally scrollable using LazyRow
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp), // Padding for the edges
                horizontalArrangement = Arrangement.spacedBy(16.dp), // Spacing between items
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Card: ETA and Bus Info
                item {
                    ETACard(
                        modifier = Modifier.width(280.dp) // Fixed width for scrollability
                    )
                }



                // Third Card (Placeholder for demonstration of scroll)
                item {
                    ETACard(
                        modifier = Modifier.width(280.dp),
                        cardColor = LiveLocationColors.CardGreen
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Composable for embedding the Google Map prototype using a WebView.
 * This replaces the native GoogleMap composable.
 */
@Composable
fun MapPrototype(modifier: Modifier = Modifier) {
    // Mock data for embedding, maintaining the previous focus area (Kathmandu)
    val kathmanduLatitude = 27.7013
    val kathmanduLongitude = 85.3206
    val zoom = 14

    // Construct a Google Maps Embed URL for a simple view.
    // This typically works without an API key in the AndroidManifest.
    val embedUrl = "<iframe>\"https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3532.3609923548975!2d85.327404275571!3d27.706138376183215!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x39eb190a74aa1f23%3A0x74ebef82ad0e5c15!2sSoftwarica%20College%20of%20IT%20and%20E-Commerce!5e0!3m2!1sen!2snp!4v1764858914671!5m2!1sen!2snp\"</iframe>"

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f) // Takes up 60% of the remaining vertical space
    ) {
        // Use AndroidView to host the WebView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    // Configure the WebView
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    // Ensure links open within the WebView, not an external browser
                    webViewClient = WebViewClient()

                    // Load the Google Maps Embed URL
                    loadUrl(embedUrl)
                }
            }
        )
    }
}


/**
 * Composable for the ETA and Bus Info card.
 */
@Composable
fun ETACard(modifier: Modifier = Modifier, cardColor: Color = LiveLocationColors.CardOrange) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .aspectRatio(2.5f) // Make it wider than tall
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Profile Image Placeholder (Girl)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(100))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder for Aliza's image
                Text("👩", fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Center: ETA and Bus Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "ETA 15 minutes",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bus No: 1533",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            // Right: Bus Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = "Bus Icon",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Composable for a smaller profile card.
 */


@Preview(showBackground = true)
@Composable
fun LiveLocationScreenPreview() {
    // Note: The BUSMATETheme import must be resolvable in your project structure.
    LiveLocationScreen()
}