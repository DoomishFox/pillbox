package com.foxnet.medications.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.foxnet.medications.ui.theme.spacing
import com.foxnet.medications.viewmodels.Task

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
fun SectionScaffold(
    label: @Composable (TextStyle) -> Unit = {},
    icon: @Composable (Color) -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column() {
        if (label != {})
        {
            Row() {
                icon(MaterialTheme.colorScheme.primary)
                if (icon != {})
                    Spacer(modifier = Modifier.padding(MaterialTheme.spacing.extraSmall))
                label(MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.padding(MaterialTheme.spacing.small))
        }
        CompositionLocalProvider(content = content)
    }
}
