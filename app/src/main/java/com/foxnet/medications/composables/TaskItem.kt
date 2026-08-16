package com.foxnet.medications.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.foxnet.medications.R
import com.foxnet.medications.ui.theme.spacing
import com.foxnet.medications.viewmodels.Task

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
fun TaskItem(
    task: Task,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer
            )
            .padding(MaterialTheme.spacing.small)
    ) {
        Box(
            modifier = Modifier
                .size(MaterialTheme.spacing.extraExtraLarge)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraLarge
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.pill_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.width(MaterialTheme.spacing.medium))
        Column(
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLargeEmphasized,
            )
            Text(
                text = "1 capsule",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
        ) {
            TextButton(
                onClick = {}
            ) {
                Text("skip")
            }
            FilledTonalToggleButton(
                checked = task.completed,
                onCheckedChange = { onAccept() },
                colors = FilledTonalToggleButtonDefaults.filledTonalToggleButtonColors(
                    checkedContainerColor = MaterialTheme.colorScheme.primary
                )
                //onClick = {},
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.check_24px),
                    contentDescription = null,
                )
            }
        }

    }
}