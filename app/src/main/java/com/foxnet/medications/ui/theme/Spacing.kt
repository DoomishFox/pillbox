package com.foxnet.medications.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

@Immutable
data class MaterialSpacing(
    val default: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,   // M3 Space 50
    val small: Dp = 8.dp,        // M3 Space 100
    val medium: Dp = 16.dp,      // M3 Space 200
    val large: Dp = 24.dp,       // M3 Space 300
    val extraLarge: Dp = 32.dp,  // M3 Space 400
    val extraExtraLarge: Dp = 48.dp // M3 Space 600
)

val LocalSpacing = staticCompositionLocalOf { MaterialSpacing() }

val MaterialTheme.spacing: MaterialSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
