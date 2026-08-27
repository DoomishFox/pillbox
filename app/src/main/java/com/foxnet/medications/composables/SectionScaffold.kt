package com.foxnet.medications.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import com.foxnet.medications.ui.theme.spacing

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
fun SectionScaffold(
    modifier: Modifier = Modifier,
    colors: SectionScaffoldColors = SectionScaffoldDefaults.sectionScaffoldColors(),
    shape: Shape = RectangleShape,
    label: @Composable (TextStyle) -> Unit = {},
    icon: @Composable (Color) -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(
                color = colors.containerColor,
                shape = shape
            )
            .fillMaxWidth()
            .then(modifier)
    ) {
        if (label != {}) {
            Row(
                modifier = Modifier
                    .padding(bottom = MaterialTheme.spacing.small)
            ) {
                icon(colors.iconColor)
                if (icon != {})
                    Spacer(modifier = Modifier.padding(MaterialTheme.spacing.extraSmall))
                label(MaterialTheme.typography.titleMedium.copy(color = colors.contentColor))
            }
        }
        CompositionLocalProvider(content = content)
    }
}

public class SectionScaffoldColors(
    public val containerColor: Color,
    public val contentColor: Color,
    public val iconColor: Color
) {
    public fun copy(
        containerColor: Color = this.containerColor,
        contentColor: Color = this.contentColor,
        iconColor: Color = this.iconColor,
    ): SectionScaffoldColors = SectionScaffoldColors(
        containerColor.takeOrElse { this.containerColor },
        contentColor.takeOrElse { this.contentColor },
        iconColor.takeOrElse { this.iconColor },
    )
}

object SectionScaffoldDefaults {
    @Composable
    public fun sectionScaffoldColors(
        containerColor: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
        iconColor: Color = Color.Unspecified,
    ): SectionScaffoldColors = SectionScaffoldColors(
        containerColor = containerColor.takeOrElse { MaterialTheme.colorScheme.surface },
        contentColor = contentColor.takeOrElse { MaterialTheme.colorScheme.onSurface },
        iconColor = iconColor.takeOrElse { MaterialTheme.colorScheme.primary },
    )
}