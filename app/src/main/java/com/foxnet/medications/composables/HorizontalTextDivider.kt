package com.foxnet.medications.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.foxnet.medications.ui.theme.spacing

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
fun HorizontalTextDivider(
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val dividerThickness = 2.dp
        val dividerWaviness = 6.dp

        WavyDivider(
            modifier = Modifier.weight(1f),
            thickness = dividerThickness,
            color = color,
            waviness = dividerWaviness,
        )

        Box(
            modifier = Modifier.padding(MaterialTheme.spacing.medium)
        ) {
            content()
        }

        WavyDivider(
            modifier = Modifier.weight(1f),
            thickness = dividerThickness,
            color = color,
            waviness = dividerWaviness,
        )
    }
}