package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = JevCyanPrimary,
    onPrimary = Color(0xFF002026),
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = Color(0xFF8CF4FF),
    secondary = JevGoldAccent,
    onSecondary = Color(0xFF332000),
    secondaryContainer = Color(0xFF573B00),
    onSecondaryContainer = Color(0xFFFFE082),
    tertiary = JevPurpleAccent,
    onTertiary = Color.White,
    background = JevDarkBg,
    onBackground = JevTextPrimary,
    surface = JevDarkSurface,
    onSurface = JevTextPrimary,
    surfaceVariant = JevDarkSurfaceVariant,
    onSurfaceVariant = JevTextSecondary,
    error = JevCoralDanger,
    onError = Color.White
)

private val LightColorScheme = DarkColorScheme // Default to cyber dark terminal for developer & trading aesthetic

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Terminal & trading dashboard aesthetic works best dark
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
