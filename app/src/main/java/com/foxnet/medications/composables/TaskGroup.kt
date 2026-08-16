package com.foxnet.medications.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.foxnet.medications.R
import com.foxnet.medications.ui.theme.fonts
import com.foxnet.medications.ui.theme.spacing
import com.foxnet.medications.viewmodels.Task

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
fun TaskGroup(
    titleNoun: String = "task",
    tasks: List<Task>,
    onTaskAccept: (Int) -> Unit,
    onTaskDecline: (Int) -> Unit,
) {
    Column() {
        Text(
            text = buildString {
                // ugly as sin
                append(tasks.size)
                append(" ")
                append(titleNoun)
                if (tasks.size != 1)
                    append("s")
            },
            style = MaterialTheme.typography.titleLargeEmphasized.copy(
                fontFamily = MaterialTheme.fonts.googleSansFlexRounded,
                fontWeight = FontWeight.ExtraBold
            )
        )
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