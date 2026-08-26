package com.foxnet.medications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foxnet.medications.database.Medication
import com.foxnet.medications.database.PersistentViewModelFactory
import com.foxnet.medications.forms.AddMedicationForm
import com.foxnet.medications.forms.AddPrescriptionForm
import com.foxnet.medications.ui.theme.spacing
import com.foxnet.medications.viewmodels.PrescriptionCard
import com.foxnet.medications.viewmodels.PrescriptionFormState
import com.foxnet.medications.viewmodels.PrescriptionsUiState
import com.foxnet.medications.viewmodels.PrescriptionsViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun Prescriptions(context: android.content.Context = LocalContext.current, viewModel: PrescriptionsViewModel = viewModel(factory = remember { PersistentViewModelFactory(context) }), outerPadding: PaddingValues, onAddPrescription: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
            topBar = { CenterAlignedTopAppBar(title = { Text("Prescriptions") }) },
            floatingActionButton = { ExtendedFloatingActionButton({ Text("Add prescription") }, { Icon(painterResource(R.drawable.add_24px), null) }, onAddPrescription) },
        ) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(outerPadding).padding(MaterialTheme.spacing.medium), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                items(state.prescriptions, key = PrescriptionCard::id) { card -> PrescriptionCardItem(card) }
                if (state.prescriptions.isEmpty()) item { Text("No prescriptions yet.") }
            }
        }
}

@Composable
fun AddPrescriptionPage(context: android.content.Context = LocalContext.current, viewModel: PrescriptionsViewModel = viewModel(factory = remember { PersistentViewModelFactory(context) }), onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectingMedication by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.formSubmissionSucceeded) { if (state.formSubmissionSucceeded) onBack() }
    BackHandler(enabled = selectingMedication) { selectingMedication = false }
    if (selectingMedication) MedicationSelectionPage(state, { selectingMedication = false }, { id -> viewModel.selectMedication(id); selectingMedication = false }, { viewModel.addMedication(); selectingMedication = false }, viewModel::updateForm)
    else AddPrescriptionForm(state, onBack, { selectingMedication = true }, viewModel::updateForm, viewModel::updateAdministration, viewModel::addAdministration, viewModel::removeAdministration, viewModel::savePrescription)
}

@Composable private fun PrescriptionCardItem(card: PrescriptionCard) {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(MaterialTheme.spacing.medium), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)) {
        Text(card.medicationName, style = MaterialTheme.typography.titleLarge); Text(card.medicationType); Text(card.doseSchedule); Text(card.nextDose, color = MaterialTheme.colorScheme.primary)
    } }
}

@Composable
private fun MedicationSelectionPage(state: PrescriptionsUiState, onBack: () -> Unit, onSelect: (Int) -> Unit, onUseNew: () -> Unit, onUpdateForm: ((PrescriptionFormState) -> PrescriptionFormState) -> Unit) {
    var addingNew by rememberSaveable { mutableStateOf(state.form.addingMedication) }
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Select medication") }, navigationIcon = { IconButton(onClick = onBack) { Icon(painterResource(R.drawable.close_24px), "Close") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(MaterialTheme.spacing.medium), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
            if (!addingNew) {
                if (state.medications.isEmpty()) item { Text("No medications are in inventory yet.") }
                items(state.medications, key = Medication::id) { medication -> Button({ onSelect(medication.id) }, Modifier.fillMaxWidth()) { Text(medication.name) } }
                item { Button({ addingNew = true }, Modifier.fillMaxWidth()) { Text("Add a new medication") } }
            } else {
                item { AddMedicationForm(state.form.newMedicationName, state.form.newMedicationType, state.form.newMedicationDefaultDose, state.form.newMedicationDefaultDoseUnit, state.form.newMedicationInventoryQuantity, { value -> onUpdateForm { it.copy(newMedicationName = value) } }, { value -> onUpdateForm { it.copy(newMedicationType = value) } }, { value -> onUpdateForm { it.copy(newMedicationDefaultDose = value) } }, { value -> onUpdateForm { it.copy(newMedicationDefaultDoseUnit = value) } }, { value -> onUpdateForm { it.copy(newMedicationInventoryQuantity = value) } }) }
                item { Button(onUseNew, Modifier.fillMaxWidth()) { Text("Use this medication") } }
            }
        }
    }
}
