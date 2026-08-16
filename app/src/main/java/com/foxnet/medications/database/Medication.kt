package com.foxnet.medications.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class Medication(
    @PrimaryKey val id: Int,
    val name: String,
    // type can be one of a few preprovided options or a "general" option. types are used for
    // selecting unit categories (mg, mL, etc.) and quick-access dosages (10mg, 25mg, 0.1mL, etc.).
    val type: Int,
)
