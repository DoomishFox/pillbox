package com.foxnet.medications.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.foxnet.medications.R
import com.foxnet.medications.ui.theme.spacing

@Composable
fun IconLabel() {
    Row() {
        Icon(
            painter = painterResource(R.drawable.prescriptions_24px),
            contentDescription = "Pill icon",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.padding(MaterialTheme.spacing.extraSmall))
        Text("Active", style = MaterialTheme.typography.titleMedium)
    }
}