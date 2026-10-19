package com.example.esportsnews.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.example.esportsnews.LocalAnimationsEnabled
import com.example.esportsnews.rememberAnimationsEnabled

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = DarkBackground,
    primaryContainer = AccentContainer,
    onPrimaryContainer = AccentBlue,
    secondary = TextSecondary,
    onSecondary = DarkBackground,
    tertiary = AccentPurple,
    onTertiary = TextPrimary,
    tertiaryContainer = PurpleContainer,
    onTertiaryContainer = Purple80,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardHeader,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceOutline,
    outlineVariant = SurfaceOutline,
    error = LiveRed,
    onError = DarkBackground
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun EsportsNewsTheme(
    darkTheme: Boolean = true,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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

    CompositionLocalProvider(LocalAnimationsEnabled provides rememberAnimationsEnabled()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = HubShapes,
            content = content
        )
    }
}
