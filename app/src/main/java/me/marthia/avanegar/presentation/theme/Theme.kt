package me.marthia.avanegar.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.ertebatbonyan.android.base.app.theme.GradientColors
import com.ertebatbonyan.android.base.app.theme.LocalGradientColors

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)



private val LightColorScheme = lightColorScheme(
    primary = theme_light_primary,
    onPrimary = theme_light_onPrimary,
    primaryContainer = theme_light_primaryContainer,
    onPrimaryContainer = theme_light_onPrimaryContainer,
    secondary = theme_light_secondary,
    onSecondary = theme_light_onSecondary,
    secondaryContainer = theme_light_secondaryContainer,
    onSecondaryContainer = theme_light_onSecondaryContainer,
    tertiary = theme_light_tertiary,
    onTertiary = theme_light_onTertiary,
    tertiaryContainer = theme_light_tertiaryContainer,
    onTertiaryContainer = theme_light_onTertiaryContainer,
    error = theme_light_error,
    onError = theme_light_onError,
    errorContainer = theme_light_errorContainer,
    onErrorContainer = theme_light_onErrorContainer,
    outline = theme_light_outline,
    background = theme_light_background,
    onBackground = theme_light_onBackground,
    surfaceDim = theme_light_surface_dim,
    surfaceBright = theme_light_surface,
    surface = theme_light_surface,
    surfaceContainer = theme_light_surface_container,
    surfaceContainerLow = theme_light_surface_low,
    surfaceContainerLowest = theme_light_surface_lowest,
    surfaceContainerHigh = theme_light_surface_high,
    surfaceContainerHighest = theme_light_surface_highest,
    onSurface = theme_light_onSurface,
    surfaceVariant = theme_light_surfaceVariant,
    onSurfaceVariant = theme_light_onSurfaceVariant,
    inverseSurface = theme_light_inverseSurface,
    inverseOnSurface = theme_light_inverseOnSurface,
    inversePrimary = theme_light_inversePrimary,
    surfaceTint = theme_light_surfaceTint,
    outlineVariant = theme_light_outlineVariant,
    scrim = theme_light_scrim,
)

/**
 * Light Android gradient colors
 */
//val LightAndroidGradientColors = GradientColors(container = DarkGreenGray95)

/**
 * Dark Android gradient colors
 */
val DarkAndroidGradientColors = GradientColors(container = Color.Black)

/**
 * Light Android background theme
 */
//val LightAndroidBackgroundTheme = BackgroundTheme(color = DarkGreenGray95)


/**
 * Now in Android theme.
 *
 * @param darkTheme Whether the theme should use a dark color scheme (follows system by default).
 * @param androidTheme Whether the theme should use the Android theme color scheme instead of the
 *        default theme.
 * @param disableDynamicTheming If `true`, disables the use of dynamic theming, even when it is
 *        supported. This parameter has no effect if [androidTheme] is `true`.
 */

var isDarkMode = false

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun supportsDynamicTheming() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S


@Composable
fun AvaNegarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    // Gradient colors
    val emptyGradientColors = GradientColors(container = colorScheme.surfaceColorAtElevation(2.dp))
    val defaultGradientColors = GradientColors(
        top = colorScheme.inverseOnSurface,
        bottom = colorScheme.primaryContainer,
        container = colorScheme.surface,
    )
    val gradientColors = when {
        dynamicColor && supportsDynamicTheming() -> emptyGradientColors
        else -> defaultGradientColors
    }



    // Composition locals
    CompositionLocalProvider(
        LocalGradientColors provides gradientColors,
//        LocalLayoutDirection provides layoutDirection,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}