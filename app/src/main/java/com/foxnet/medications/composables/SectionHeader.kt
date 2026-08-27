package com.foxnet.medications.composables

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.foxnet.medications.ui.theme.fonts

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLargeEmphasized.copy(
            fontFamily = MaterialTheme.fonts.googleSansFlexRounded,
            fontWeight = FontWeight.ExtraBold
        )
    )
}