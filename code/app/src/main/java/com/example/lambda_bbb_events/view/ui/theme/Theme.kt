package com.example.lambda_bbb_events.view.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/*
 * Defines the application's Material 3 theme.
 *
 * Supports customized Coastal Blue colors
 * for both light and dark modes.
 */

// ========================================
// Light Theme
// ========================================

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = LightBlue,
    onPrimaryContainer = DarkBlue,

    secondary = SecondaryBlue,
    onSecondary = DarkBlue,
    secondaryContainer = LightBlue,
    onSecondaryContainer = DarkBlue,

    tertiary = AvailableGreen,
    onTertiary = Color.White,
    tertiaryContainer = AvailableBackground,
    onTertiaryContainer = AvailableGreen,

    background = BackgroundWhite,
    onBackground = TextPrimary,

    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = LightBlue,
    onSurfaceVariant = TextSecondary,

    outline = OutlineGray,
    outlineVariant = BorderGray,

    error = BookedRed,
    onError = Color.White,
    errorContainer = BookedBackground,
    onErrorContainer = BookedRed
)

// ========================================
// Dark Theme
// ========================================

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryBlue,
    onPrimary = DarkBlue,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkTextPrimary,

    secondary = DarkSecondaryBlue,
    onSecondary = DarkBlue,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkTextPrimary,

    tertiary = DarkAvailableGreen,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkAvailableBackground,
    onTertiaryContainer = DarkAvailableGreen,

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,

    outline = DarkBorder,
    outlineVariant = DarkBorderVariant,

    error = DarkBookedRed,
    onError = Color(0xFF690005),
    errorContainer = DarkBookedBackground,
    onErrorContainer = DarkBookedRed
)

// ========================================
// Booking Status Theme
// ========================================

data class BookingStatusColors(
    val available: Color,
    val availableBackground: Color,
    val booked: Color,
    val bookedBackground: Color,
    val pending: Color,
    val pendingBackground: Color,
    val selected: Color,
    val selectedBackground: Color
)

private val LightBookingStatusColors = BookingStatusColors(
    available = AvailableGreen,
    availableBackground = AvailableBackground,
    booked = BookedRed,
    bookedBackground = BookedBackground,
    pending = PendingAmber,
    pendingBackground = PendingBackground,
    selected = SelectedBlue,
    selectedBackground = SelectedBackground
)

private val DarkBookingStatusColors = BookingStatusColors(
    available = DarkAvailableGreen,
    availableBackground = DarkAvailableBackground,
    booked = DarkBookedRed,
    bookedBackground = DarkBookedBackground,
    pending = DarkPendingAmber,
    pendingBackground = DarkPendingBackground,
    selected = DarkSelectedBlue,
    selectedBackground = DarkSelectedBackground
)

private val LocalBookingStatusColors =
    staticCompositionLocalOf { LightBookingStatusColors }

/**
 * Returns booking status colors for the active theme.
 */
val MaterialTheme.bookingStatusColors: BookingStatusColors
    @Composable
    get() = LocalBookingStatusColors.current

/**
 * Applies the application's Coastal Blue Material 3 theme.
 *
 * @param darkTheme Whether to use dark mode (false by default, enabled in appearance settings).
 * @param dynamicColor Whether to use Android wallpaper colors.
 * Defaults to false.
 * @param content UI content displayed within the theme.
 */
@Composable
fun Lambda_BBBeventsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val bookingColors = if (darkTheme) {
        DarkBookingStatusColors
    } else {
        LightBookingStatusColors
    }

    CompositionLocalProvider(
        LocalBookingStatusColors provides bookingColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
