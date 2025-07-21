package com.example.cateringapp.ui.theme

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

// Couleurs personnalisées : Noir, Or, Blanc
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFC107),       // Or
    onPrimary = Color.Black,           // Texte sur fond or
    background = Color(0xFF121212),    // Noir profond
    onBackground = Color.White,        // Texte sur fond noir
    surface = Color(0xFF1E1E1E),       // Surface plus claire
    onSurface = Color.White            // Texte sur surface
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFFC107),
    onPrimary = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color(0xFFF5F5F5),
    onSurface = Color.Black
)

@Composable
fun CateringAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // désactivé pour forcer le thème or/noir/blanc
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
