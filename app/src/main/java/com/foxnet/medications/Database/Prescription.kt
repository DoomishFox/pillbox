package com.foxnet.medications.Database

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey
import java.time.LocalTime
import java.time.Period

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Prescription(
    @PrimaryKey val id: Int,
    // [FOREIGN_KEY] -> Medication
    // every Prescription MUST be attached to a Medication, this is non-negotiable.
    val medicationId: Int,
    val type: Int,
    val friendlyName: String,
    // dose
    // eg: 0.1 mL
    // ^- im storing dose as Int despite knowing I want to display decimals sometimes. the database
    //    should store the dose here as 100 µL. the UI can do what it wants with this information.
    val defaultDoseUnit: String,
    val defaultDose: Int,
    val withFood: Boolean,
    // TODO:
    //  other things to include:
    //  - notification settings
    // schedule is a cron string that defines how often the prescription should repeat its
    // administrations.
    val schedule: String,
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Prescription::class,
            parentColumns = ["id"],
            childColumns = ["prescriptionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PrescriptionAdministration(
    @PrimaryKey val id: Int,
    // [FOREIGN_KEY] -> Prescription
    val prescriptionId: Int,
    // dose copies the dose in linked Prescription unless overridden.
    // eg: 0.1 mL
    // ^- im storing dose as Int despite knowing I want to display decimals sometimes. the database
    //    should store the dose here as 100 µL. the UI can do what it wants with this information.
    val doseUnit: String,
    val dose: Int,
    // multiplier for dose at frequency
    // eg: 2x [0.1 mL] every [4 hour]
    // default: 1
    val doseMultiplier: Int,
    // offset is used for prescriptions with different daily administration requirements. instead
    // of cobbling together something out of repeating PrescriptionSchedules you can simply add a
    // bunch of administration times 1 or more days out.
    // default: 0
    val offset: Period,
    // time of day when prescription is to be administered.
    val time: LocalTime?,
    val event: String?,
)
