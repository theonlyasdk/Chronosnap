package io.github.theonlyasdk.chronosnap.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Fallback Light Colors (Cobalt Blue Monet Seed)
private val LightColorScheme = lightColorScheme(
    primary = CyanPrimary,
    onPrimary = CyanOnPrimary,
    primaryContainer = CyanPrimaryContainer,
    onPrimaryContainer = CyanOnPrimaryContainer,
    secondary = CyanSecondary,
    onSecondary = CyanOnSecondary,
    secondaryContainer = CyanSecondaryContainer,
    onSecondaryContainer = CyanOnSecondaryContainer,
    tertiary = CyanTertiary,
    onTertiary = CyanOnTertiary,
    tertiaryContainer = CyanTertiaryContainer,
    onTertiaryContainer = CyanOnTertiaryContainer,
    error = CyanError,
    onError = CyanOnError,
    errorContainer = CyanErrorContainer,
    onErrorContainer = CyanErrorContainer,
    background = CyanBackground,
    onBackground = CyanOnBackground,
    surface = CyanSurface,
    onSurface = CyanOnSurface
)

// Fallback Dark Colors
private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimaryDark,
    onPrimary = CyanOnPrimaryDark,
    primaryContainer = CyanPrimaryContainerDark,
    onPrimaryContainer = CyanOnPrimaryContainerDark,
    secondary = CyanSecondaryDark,
    onSecondary = CyanOnSecondaryDark,
    secondaryContainer = CyanSecondaryContainerDark,
    onSecondaryContainer = CyanOnSecondaryContainerDark,
    tertiary = CyanTertiaryDark,
    onTertiary = CyanOnTertiaryDark,
    tertiaryContainer = CyanTertiaryContainerDark,
    onTertiaryContainer = CyanOnTertiaryContainerDark,
    error = CyanErrorDark,
    onError = CyanOnErrorDark,
    errorContainer = CyanErrorContainerDark,
    onErrorContainer = CyanOnErrorContainerDark,
    background = CyanBackgroundDark,
    onBackground = CyanOnBackgroundDark,
    surface = CyanSurfaceDark,
    onSurface = CyanOnSurfaceDark
)

@Composable
fun ChronosnapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}