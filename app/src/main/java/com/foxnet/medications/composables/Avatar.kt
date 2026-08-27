package com.foxnet.medications.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.painterResource
import com.foxnet.medications.R
import com.foxnet.medications.ui.theme.spacing

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
fun Avatar(
    icon: @Composable (Color) -> Unit = {},
    colors: AvatarColors = AvatarDefaults.avatarColors()
) {
    Box(
        modifier = Modifier
            .size(MaterialTheme.spacing.extraExtraLarge)
            .background(
                color = colors.containerColor,
                shape = MaterialTheme.shapes.extraLarge
            ),
        contentAlignment = Alignment.Center,
    ) {
        icon(colors.contentColor)
    }
}

public class AvatarColors(
    public val containerColor: Color,
    public val contentColor: Color,
) {
    public fun copy(
        containerColor: Color = this.containerColor,
        contentColor: Color = this.contentColor,
    ): AvatarColors = AvatarColors(
        containerColor.takeOrElse { this.containerColor },
        contentColor.takeOrElse { this.contentColor },
    )
}

object AvatarDefaults {
    @Composable
    public fun avatarColors(
        containerColor: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
    ): AvatarColors = AvatarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
    )
}