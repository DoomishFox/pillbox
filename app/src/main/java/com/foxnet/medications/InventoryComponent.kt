package com.foxnet.medications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foxnet.medications.composables.SectionHeader
import com.foxnet.medications.forms.AddMedicationForm
import com.foxnet.medications.database.PersistentViewModelFactory
import com.foxnet.medications.ui.theme.spacing
import com.foxnet.medications.viewmodels.InventoryMedicationCard
import com.foxnet.medications.viewmodels.InventoryUiState
import com.foxnet.medications.viewmodels.InventoryViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun Inventory(
    context: android.content.Context = LocalContext.current,
    viewModel: InventoryViewModel = viewModel(factory = remember {
        PersistentViewModelFactory(
            context
        )
    }),
    outerPadding: PaddingValues,
    onAddMedication: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.consumeWindowInsets(outerPadding),
                title = {
                    Text(stringResource(R.string.profile_nav))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text("Log")
                },
                icon = {
                    Icon(
                        painterResource(R.drawable.add_24px),
                        contentDescription = null
                    )
                },
                onClick = onAddMedication,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraLarge.copy(
                        bottomEnd = CornerSize(0.dp),
                        bottomStart = CornerSize(0.dp)
                    )
                )
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            item {
                SectionHeader("In-date")
            }
            items(
                state.medications,
                key = InventoryMedicationCard::id
            ) { medication -> InventoryMedicationCardItem(medication) }
            if (state.medications.isEmpty()) item { Text("No medications in inventory.") }
        }
    }
}

@Composable
fun AddInventoryMedicationPage(
    context: android.content.Context = LocalContext.current,
    viewModel: InventoryViewModel = viewModel(factory = remember {
        PersistentViewModelFactory(
            context
        )
    }),
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.saveSucceeded) { if (state.saveSucceeded) onBack() }
    NewInventoryMedicationPage(state, onBack, viewModel::updateForm, viewModel::saveMedication)
}

@Composable
private fun InventoryMedicationCardItem(medication: InventoryMedicationCard) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
        ) {
            Text(medication.name, style = MaterialTheme.typography.titleLarge)
            Text(medication.type, color = MaterialTheme.colorScheme.onSurfaceVariant)
            // TODO: medication quantity should respect the type of medication. if its capsules, it
            //       should read "X capsules", if its injectable it should read "X mL". Or maybe add
            //       support for number of vials, idk.
            Text(buildString {
                append(medication.quantity)
                append(" ${medication.type}")
                if (medication.quantity > 1)
                    append("s")
            }, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun NewInventoryMedicationPage(
    state: InventoryUiState,
    onBack: () -> Unit,
    onUpdate: ((com.foxnet.medications.viewmodels.NewMedicationFormState) -> com.foxnet.medications.viewmodels.NewMedicationFormState) -> Unit,
    onSave: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Inventory medication") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painterResource(R.drawable.close_24px),
                            "Close"
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            AddMedicationForm(
                name = state.form.name,
                type = state.form.type,
                defaultDose = state.form.defaultDose,
                defaultDoseUnit = state.form.defaultDoseUnit,
                inventoryQuantity = state.form.inventoryQuantity,
                onNameChange = { value -> onUpdate { it.copy(name = value) } },
                onTypeChange = { value -> onUpdate { it.copy(type = value) } },
                onDefaultDoseChange = { value -> onUpdate { it.copy(defaultDose = value) } },
                onDefaultDoseUnitChange = { value -> onUpdate { it.copy(defaultDoseUnit = value) } },
                onInventoryQuantityChange = { value -> onUpdate { it.copy(inventoryQuantity = value) } },
            )
            state.formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = onSave,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (state.isSaving) "Saving…" else "Save medication") }
        }
    }
}
