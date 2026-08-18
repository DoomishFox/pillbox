package com.foxnet.medications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foxnet.medications.composables.HorizontalTextDivider
import com.foxnet.medications.composables.SectionScaffold
import com.foxnet.medications.composables.TaskGroup
import com.foxnet.medications.database.PersistentViewModelFactory
import com.foxnet.medications.ui.theme.fonts
import com.foxnet.medications.ui.theme.spacing
import com.foxnet.medications.viewmodels.AdministrationOutcome
import com.foxnet.medications.viewmodels.ProgressViewModel


@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
public fun Progress(
    context: android.content.Context = LocalContext.current,
    viewModel: ProgressViewModel = viewModel(
        factory = remember { PersistentViewModelFactory(context) }
    ),
    outerPadding: PaddingValues,
) {
    val todayState by viewModel.todayUiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.consumeWindowInsets(outerPadding),
                title = {
                    Text(stringResource(R.string.home_nav))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {

        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(innerPadding))
        {
            Column(
                modifier = Modifier.padding(MaterialTheme.spacing.medium)
            ) {
                Text(todayState.dayName)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.extraLarge.copy(bottomEnd = CornerSize(0.dp), bottomStart = CornerSize(0.dp))
                    )
                    .padding(MaterialTheme.spacing.medium)
            ) {
                if (todayState.taskGroups.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = MaterialTheme.spacing.extraExtraLarge)
                            .align(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.heart_check_24px),
                            contentDescription = "No medications scheduled today.",
                            tint = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier
                                .size(MaterialTheme.spacing.extraLarge),
                        )
                        Text(
                            text = "All caught up!",
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                } else {
                    todayState.taskGroups.forEach { group ->
                        SectionScaffold(
                            label = { style -> Text(group.label, style = style) },
                            icon = { color -> Icon(
                                painter = painterResource(R.drawable.prescriptions_24px),
                                contentDescription = "Pill icon",
                                tint = color
                            ) },
                        ) {
                            TaskGroup(
                                titleNoun = "medication",
                                tasks = group.tasks,
                                onTaskAccept = { id ->
                                    viewModel.recordAdministration(id, AdministrationOutcome.TAKEN)
                                },
                                onTaskDecline = { id ->
                                    viewModel.recordAdministration(id, AdministrationOutcome.SKIPPED)
                                },
                            )
                        }
                    }
                }

                HorizontalTextDivider(
                    color = MaterialTheme.colorScheme.outline
                ) {
                    Text(
                        text = "Yesterday",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = MaterialTheme.fonts.googleSansFlexRounded,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Card(
                    shape = CardDefaults.shape,
                    colors = CardDefaults.cardColors(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column() {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Text(
                                    "Yesterday",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                    ) {
                                        Text(
                                            "0",
                                            style = MaterialTheme.typography.displayMediumEmphasized,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                        Text(
                                            "Skipped",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                    ) {
                                        Text(
                                            "2",
                                            style = MaterialTheme.typography.displaySmall,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                        Text(
                                            "As needed",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Button(onClick = {}) {
                                Text("More")
                                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                                Icon(
                                    painter = painterResource(R.drawable.arrow_forward_24px),
                                    contentDescription = "More details",
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                            }
                        }
                    }
                }
                Card(
                    shape = CardDefaults.shape,
                    colors = CardDefaults.cardColors(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column() {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Text(
                                "History",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .padding(16.dp)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Button(onClick = {}) {
                                Text("More")
                                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                                Icon(
                                    painter = painterResource(R.drawable.arrow_forward_24px),
                                    contentDescription = "More details",
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
