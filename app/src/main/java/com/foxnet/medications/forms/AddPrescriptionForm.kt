package com.foxnet.medications.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.foxnet.medications.R
import com.foxnet.medications.database.PrescriptionScheduleKind
import com.foxnet.medications.ui.theme.spacing
import com.foxnet.medications.viewmodels.AdministrationDraft
import com.foxnet.medications.viewmodels.PrescriptionEndCondition
import com.foxnet.medications.viewmodels.PrescriptionFormState
import com.foxnet.medications.viewmodels.PrescriptionsUiState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddPrescriptionForm(
    state: PrescriptionsUiState,
    onBack: () -> Unit,
    onSelectMedication: () -> Unit,
    onUpdateForm: ((PrescriptionFormState) -> PrescriptionFormState) -> Unit,
    onUpdateAdministration: (Int, (AdministrationDraft) -> AdministrationDraft) -> Unit,
    onAddAdministration: () -> Unit,
    onRemoveAdministration: (Int) -> Unit,
    onSave: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New prescription") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painterResource(R.drawable.close_24px),
                            "Close"
                        )
                    }
                })
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            item { Text("Medication", style = MaterialTheme.typography.titleMedium) }
            item {
                val selectedName =
                    state.medications.firstOrNull { it.id == state.form.selectedMedicationId }?.name
                        ?: state.form.newMedicationName.takeIf { state.form.addingMedication }
                Button(onClick = onSelectMedication, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        selectedName?.let { "Change medication: $it" } ?: "Select medication")
                }
            }
            item { Text("Administrations", style = MaterialTheme.typography.titleMedium) }
            items(state.form.administrations, key = AdministrationDraft::id) { administration ->
                AdministrationEditorCard(
                    administration = administration,
                    canRemove = state.form.administrations.size > 1,
                    onUpdate = { transform ->
                        onUpdateAdministration(
                            administration.id,
                            transform
                        )
                    },
                    onRemove = { onRemoveAdministration(administration.id) },
                )
            }
            item {
                Button(onClick = onAddAdministration, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Add administration"
                    )
                }
            }
            item { Text("Schedule", style = MaterialTheme.typography.titleMedium) }
            item {
                PrescriptionScheduleKind.entries.forEach { kind ->
                    FilterChip(
                        state.form.scheduleKind == kind,
                        { onUpdateForm { it.copy(scheduleKind = kind) } },
                        label = { Text(kind.displayName()) })
                }
            }
            when (state.form.scheduleKind) {
                PrescriptionScheduleKind.HOURLY -> item {
                    ScheduleTextField(
                        state.form.interval,
                        "Every N hours"
                    ) { onUpdateForm { form -> form.copy(interval = it) } }
                }

                PrescriptionScheduleKind.DAILY -> item {
                    ScheduleTextField(
                        state.form.interval,
                        "Every N days"
                    ) { onUpdateForm { form -> form.copy(interval = it) } }
                }

                PrescriptionScheduleKind.WEEKLY -> {
                    item {
                        ScheduleTextField(
                            state.form.interval,
                            "Every N weeks"
                        ) { onUpdateForm { form -> form.copy(interval = it) } }
                    }
                    item {
                        (1..7).forEach { day ->
                            FilterChip(
                                day in state.form.daysOfWeek,
                                { onUpdateForm { form -> form.copy(daysOfWeek = if (day in form.daysOfWeek) form.daysOfWeek - day else form.daysOfWeek + day) } },
                                label = { Text(day.shortName()) })
                        }
                    }
                }

                PrescriptionScheduleKind.MONTHLY -> {
                    item {
                        ScheduleTextField(
                            state.form.interval,
                            "Every N months"
                        ) { onUpdateForm { form -> form.copy(interval = it) } }
                    }
                    item {
                        ScheduleTextField(
                            state.form.daysOfMonth,
                            "Days of month (e.g. 1,15)"
                        ) { onUpdateForm { form -> form.copy(daysOfMonth = it) } }
                    }
                }

                PrescriptionScheduleKind.ON_OFF_CYCLE -> item {
                    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                        ScheduleTextField(
                            state.form.daysOn,
                            "Days on",
                            Modifier.weight(1f)
                        ) { onUpdateForm { form -> form.copy(daysOn = it) } }
                        ScheduleTextField(
                            state.form.daysOff,
                            "Days off",
                            Modifier.weight(1f)
                        ) { onUpdateForm { form -> form.copy(daysOff = it) } }
                    }
                }
            }
            item { Text("Prescription end", style = MaterialTheme.typography.titleMedium) }
            item {
                PrescriptionEndCondition.entries.forEach { condition ->
                    FilterChip(
                        state.form.endCondition == condition,
                        { onUpdateForm { it.copy(endCondition = condition) } },
                        label = { Text(condition.displayName()) })
                }
            }
            if (state.form.endCondition == PrescriptionEndCondition.DURATION) item {
                ScheduleTextField(
                    state.form.durationDays,
                    "Duration (days)"
                ) { onUpdateForm { form -> form.copy(durationDays = it) } }
            }
            if (state.form.endCondition == PrescriptionEndCondition.END_DATE) item {
                ScheduleTextField(
                    state.form.endDate,
                    "End date (YYYY-MM-DD)"
                ) { onUpdateForm { form -> form.copy(endDate = it) } }
            }
            state.formError?.let { error ->
                item {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            item {
                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (state.isSaving) "Saving…" else "Save prescription") }
            }
        }
    }
}

@Composable
private fun AdministrationEditorCard(
    administration: AdministrationDraft,
    canRemove: Boolean,
    onUpdate: ((AdministrationDraft) -> AdministrationDraft) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            Text("Administration", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                administration.time,
                { value -> onUpdate { it.copy(time = value) } },
                label = { Text("Time (e.g. 09:00)") },
                modifier = Modifier.fillMaxWidth()
            )
            FilterChip(
                administration.useDoseOverride,
                { onUpdate { it.copy(useDoseOverride = !it.useDoseOverride) } },
                label = { Text("Override medication default dose") })
            if (administration.useDoseOverride) Row(
                horizontalArrangement = Arrangement.spacedBy(
                    MaterialTheme.spacing.small
                )
            ) {
                OutlinedTextField(
                    administration.overrideDose,
                    { value -> onUpdate { it.copy(overrideDose = value) } },
                    label = { Text("Dose") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    administration.overrideDoseUnit,
                    { value -> onUpdate { it.copy(overrideDoseUnit = value) } },
                    label = { Text("Unit") },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                administration.instructions,
                { value -> onUpdate { it.copy(instructions = value) } },
                label = { Text("Administration note (e.g. With breakfast)") },
                modifier = Modifier.fillMaxWidth()
            )
            if (canRemove) Button(onClick = onRemove) { Text("Remove administration") }
        }
    }
}

@Composable
private fun ScheduleTextField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit
) {
    OutlinedTextField(value, onChange, label = { Text(label) }, modifier = modifier.fillMaxWidth())
}

private fun PrescriptionScheduleKind.displayName() = when (this) {
    PrescriptionScheduleKind.HOURLY -> "Hourly"
    PrescriptionScheduleKind.DAILY -> "Daily"
    PrescriptionScheduleKind.WEEKLY -> "Weekly"
    PrescriptionScheduleKind.MONTHLY -> "Monthly"
    PrescriptionScheduleKind.ON_OFF_CYCLE -> "Interval"
}

private fun PrescriptionEndCondition.displayName() = when (this) {
    PrescriptionEndCondition.ONGOING -> "Ongoing"; PrescriptionEndCondition.DURATION -> "Duration"; PrescriptionEndCondition.END_DATE -> "End date"
}

private fun Int.shortName() =
    java.time.DayOfWeek.of(this).name.take(3).lowercase().replaceFirstChar(Char::uppercase)
