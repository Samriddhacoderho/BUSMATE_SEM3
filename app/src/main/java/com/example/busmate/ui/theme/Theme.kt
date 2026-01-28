package com.example.busmate.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// --- FACEBOOK STYLE DARK PALETTE ---
private val FacebookDarkBackground = Color(0xFF18191A) // Deep Charcoal
private val FacebookSurface = Color(0xFF242526)         // Lighter Gray for Cards
private val FacebookTextPrimary = Color(0xFFE4E6EB)     // Off-white text
private val FacebookTextSecondary = Color(0xFFB0B3B8)   // Muted gray text

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2D88FF), // Facebook-style Blue
    secondary = PurpleGrey80,
    tertiary = Pink80,
    // Dark mode specific backgrounds (Facebook style)
    background = FacebookDarkBackground,
    surface = FacebookSurface,
    onBackground = FacebookTextPrimary,
    onSurface = FacebookTextPrimary,
    onSurfaceVariant = FacebookTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    // Light mode specific backgrounds
    background = Color(0xFFF7F7F7),
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// --- HELPER TO READ SAVED PREFERENCE ---
@Composable
fun isDarkMode(): Boolean {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    // 0 = Follow System, 1 = Light, 2 = Dark
    return when (prefs.getInt("dark_mode_pref", 0)) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
}

@Composable
fun BusMateTheme(
    darkTheme: Boolean = isDarkMode(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}