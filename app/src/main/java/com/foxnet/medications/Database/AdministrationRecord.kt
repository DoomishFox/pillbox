package com.foxnet.medications.Database

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = PrescriptionAdministration::class,
            parentColumns = ["id"],
            childColumns = ["prescriptionAdministrationId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class AdministrationRecord(
    @PrimaryKey val id: Int,
    // [FOREIGN_KEY] -> Medication
    // this is linked to a Medication rather than a Prescription because if the Prescription gets
    // deleted the AdministrationRecord should retain all necessary information.
    val medicationId: Int,
    // [FOREIGN_KEY] -> PrescriptionAdministration
    // this CAN be tied to a specific prescription administration, but it doesn't have to be. as is
    // the case with as-needed administrations.
    // is: nullable
    val prescriptionAdministrationId: Int?,
    // skipped doses can ignore dose info
    val skipped: Boolean,
    // dose can be modified as needed so it's stored here also.
    // default: dose in linked PrescriptionAdministration
    val doseUnit: String,
    val dose: Int,
    val withFood: Boolean,
    val date: LocalDate,
    val time: LocalTime,
)
