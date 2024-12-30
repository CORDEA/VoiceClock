package jp.cordea.voiceclock.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue50,
    primaryContainer = Blue800,
    surface = Blue1000,
    surfaceContainer = Blue900,
    surfaceContainerHighest = Blue800,
    secondaryContainer = Blue800,
    background = Blue1000,
)

private val LightColorScheme = lightColorScheme(
    primary = Blue900,
    primaryContainer = Blue200,
    surface = Blue10,
    surfaceContainer = Blue50,
    surfaceContainerHighest = Blue100,
    secondaryContainer = Blue100,
    background = Blue10,
)

@Composable
fun VoiceClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
