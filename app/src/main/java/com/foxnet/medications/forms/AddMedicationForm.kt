package com.foxnet.medications.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.foxnet.medications.R
import com.foxnet.medications.database.MedicationType
import com.foxnet.medications.ui.theme.spacing

@Composable
fun AddMedicationForm(
    name: String,
    type: MedicationType,
    defaultDose: String,
    defaultDoseUnit: String,
    inventoryQuantity: String,
    onNameChange: (String) -> Unit,
    onTypeChange: (MedicationType) -> Unit,
    onDefaultDoseChange: (String) -> Unit,
    onDefaultDoseUnitChange: (String) -> Unit,
    onInventoryQuantityChange: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Medication name") },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.pill_24px),
                    contentDescription = null,
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        MedicationType.entries.forEach { medicationType ->
            FilterChip(
                selected = type == medicationType,
                onClick = { onTypeChange(medicationType) },
                label = {
                    Text(medicationType.name.lowercase().replaceFirstChar(Char::uppercase))
                }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
            OutlinedTextField(
                defaultDose,
                onDefaultDoseChange,
                label = { Text("Usual dose") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                defaultDoseUnit,
                onDefaultDoseUnitChange,
                label = { Text("Dose unit") },
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            inventoryQuantity,
            onInventoryQuantityChange,
            label = { Text("Quantity in inventory") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
