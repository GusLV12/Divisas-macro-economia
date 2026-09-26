package com.gusslinaresv.activitefinanca.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import android.app.Activity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.gusslinaresv.activitefinanca.data.ThemeMode
import com.gusslinaresv.activitefinanca.data.UserPreferences

private val LightColorScheme = lightColorScheme(
    primary = Emerald700,
    onPrimary = Color.White,
    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald900,
    secondary = Navy700,
    onSecondary = Color.White,
    secondaryContainer = Navy100,
    onSecondaryContainer = Navy900,
    tertiary = Gold600,
    onTertiary = Color.White,
    tertiaryContainer = Gold100,
    onTertiaryContainer = Color(0xFF4A3000),
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE6EEEB),
    onSurfaceVariant = Color(0xFF4B5563),
    surfaceContainer = Color(0xFFEFF4F2),
    surfaceContainerHigh = Color(0xFFE8EFEC),
    surfaceContainerHighest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF9FBFA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    outline = Color(0xFFB6C4BF),
)

private val DarkColorScheme = darkColorScheme(
    primary = Emerald300,
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF065F46),
    onPrimaryContainer = Emerald100,
    secondary = Navy300,
    onSecondary = Navy900,
    secondaryContainer = Navy700,
    onSecondaryContainer = Navy100,
    tertiary = Gold400,
    onTertiary = Color(0xFF3D2800),
    tertiaryContainer = Color(0xFF5C4200),
    onTertiaryContainer = Gold100,
    background = NightBackground,
    onBackground = Color(0xFFE5E9F0),
    surface = NightSurface,
    onSurface = Color(0xFFE5E9F0),
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = Color(0xFFA9B4C7),
    surfaceContainer = Color(0xFF15213A),
    surfaceContainerHigh = Color(0xFF1B2944),
    surfaceContainerHighest = Color(0xFF16223A),
    surfaceContainerLow = Color(0xFF101A2C),
    surfaceContainerLowest = Color(0xFF0C1524),
    outline = Color(0xFF3A4A66),
)

/** Colores extra que Material no trae: subida/bajada de una divisa. */
@Immutable
data class MarketColors(val gain: Color, val loss: Color)

val LocalMarketColors = staticCompositionLocalOf { MarketColors(GainLight, LossLight) }

@Composable
fun ActiviteFinancaTheme(content: @Composable () -> Unit) {
    // El modo se lee de las preferencias: al cambiarlo en Ajustes, todas las pantallas se actualizan al instante
    val mode by UserPreferences.themeMode.collectAsState()
    val darkTheme = when (mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val market = if (darkTheme) MarketColors(GainDark, LossDark) else MarketColors(GainLight, LossLight)

    // Íconos de la barra de estado claros u oscuros según el tema elegido en Ajustes (no solo el del sistema)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    CompositionLocalProvider(LocalMarketColors provides market) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}
