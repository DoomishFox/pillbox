package com.foxnet.medications.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.foxnet.medications.ui.theme.spacing
import com.foxnet.medications.viewmodels.AdministrationTask

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
fun TaskGroup(
    titleNoun: String = "task",
    tasks: List<AdministrationTask>,
    onTaskAccept: (Int) -> Unit,
    onTaskDecline: (Int) -> Unit,
) {
    Column() {
        SectionHeader(buildString {
            // ugly as sin
            append(tasks.size)
            append(" ")
            append(titleNoun)
            if (tasks.size != 1)
                append("s")
        })
        Spacer(modifier = Modifier.padding(MaterialTheme.spacing.small))
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            modifier = Modifier.clip(MaterialTheme.shapes.large)
        ) {
            tasks.forEach { task ->
                TaskItem(
                    task = task,
                    onAccept = { onTaskAccept(task.id) },
                    onDecline = { onTaskDecline(task.id) },
                )
            }
        }
    }
}
