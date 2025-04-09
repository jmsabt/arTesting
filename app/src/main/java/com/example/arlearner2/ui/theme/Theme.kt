package com.example.arlearner2.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun ARLearner2Theme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Get the system dark theme setting in the composable scope
    val systemDarkTheme = isSystemInDarkTheme()
    // Use remember to persist the state, initialized with system setting
    val isDarkTheme: MutableState<Boolean> = remember { mutableStateOf(systemDarkTheme) }
    val context = LocalContext.current

    // Compute colorScheme within the composable scope
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDarkTheme.value) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        if (isDarkTheme.value) DarkColorScheme else LightColorScheme
    }

    // Set LocalThemeState before MaterialTheme
    LocalThemeState.isDarkTheme = isDarkTheme.value
    LocalThemeState.toggleDarkTheme = { isDarkTheme.value = !isDarkTheme.value }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

object LocalThemeState {
    var isDarkTheme: Boolean = false
    var toggleDarkTheme: () -> Unit = {}
}