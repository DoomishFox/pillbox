package com.foxnet.medications.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxnet.medications.database.ChartDb
import com.foxnet.medications.database.Medication
import com.foxnet.medications.database.MedicationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class InventoryMedicationCard(
    val id: Int,
    val name: String,
    val type: String,
    val quantity: String,
    val defaultDose: String,
)

data class NewMedicationFormState(
    val name: String = "",
    val type: MedicationType = MedicationType.CAPSULE,
    val defaultDose: String = "1",
    val defaultDoseUnit: String = "mg",
    val inventoryQuantity: String = "0",
)

data class InventoryUiState(
    val medications: List<InventoryMedicationCard> = emptyList(),
    val form: NewMedicationFormState = NewMedicationFormState(),
    val isSaving: Boolean = false,
    val formError: String? = null,
    val saveSucceeded: Boolean = false,
)

class InventoryViewModel(private val chart: ChartDb) : ViewModel() {
    private val form = MutableStateFlow(NewMedicationFormState())
    private val isSaving = MutableStateFlow(false)
    private val formError = MutableStateFlow<String?>(null)
    private val saveSucceeded = MutableStateFlow(false)

    val uiState: StateFlow<InventoryUiState> = combine(chart.observeMedications(), form, isSaving, formError) { medications, form, saving, error ->
        InventoryUiState(
            medications = medications.map {
                InventoryMedicationCard(
                    id = it.id,
                    name = it.name,
                    type = it.type.displayName(),
                    quantity = "${it.inventoryQuantity} ${it.defaultDoseUnit}",
                    defaultDose = "Usual dose: ${it.defaultDose} ${it.defaultDoseUnit}",
                )
            },
            form = form,
            isSaving = saving,
            formError = error,
            saveSucceeded = saveSucceeded.value,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InventoryUiState())

    fun updateForm(transform: (NewMedicationFormState) -> NewMedicationFormState) {
        form.update(transform)
        formError.value = null
        saveSucceeded.value = false
    }

    fun saveMedication() {
        val input = form.value
        val dose = input.defaultDose.toIntOrNull()
        val quantity = input.inventoryQuantity.toIntOrNull()
        val error = when {
            input.name.isBlank() -> "Enter a medication name."
            dose == null || dose <= 0 -> "Enter a valid usual dose."
            input.defaultDoseUnit.isBlank() -> "Enter a dose unit."
            quantity == null || quantity < 0 -> "Enter a valid inventory quantity."
            else -> null
        }
        if (error != null) { formError.value = error; return }
        viewModelScope.launch {
            isSaving.value = true
            try {
                chart.insertMedication(Medication(name = input.name.trim(), type = input.type, defaultDose = dose!!, defaultDoseUnit = input.defaultDoseUnit.trim(), inventoryQuantity = quantity!!))
                form.value = NewMedicationFormState()
                saveSucceeded.value = true
            } finally { isSaving.value = false }
        }
    }
}

private fun MedicationType.displayName() = name.lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase(Locale.getDefault()) }
