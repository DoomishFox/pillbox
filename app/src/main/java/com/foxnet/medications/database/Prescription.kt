package com.foxnet.medications.database

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period

@Entity(
    indices = [Index(value = ["medicationId"])],
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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val active: Boolean,
    // [FOREIGN_KEY] -> Medication
    // every Prescription MUST be attached to a Medication, this is non-negotiable.
    val medicationId: Int,
    val type: Int,
    val friendlyName: String,
    val withFood: Boolean,
    // The first day this prescription can produce administrations.
    val startDate: LocalDate,
    // Prescriptions are ongoing unless either of these end conditions is supplied.
    val endDate: LocalDate? = null,
    val duration: Period? = null,
    // TODO:
    //  other things to include:
    //  - notification settings
    // JSON-encoded PrescriptionSchedule defining when the prescription repeats.
    val schedule: String,
    // Optional directions for medications that need a specific administration method.
    val administrationInstructions: String? = null,
) {
    init {
        require(endDate == null || duration == null) {
            "A prescription cannot have both an endDate and a duration."
        }
    }
}

@Entity(
    indices = [Index(value = ["prescriptionId"])],
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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // [FOREIGN_KEY] -> Prescription
    val prescriptionId: Int,
    // dose copies the dose in linked Prescription unless overridden.
    // eg: 0.1 mL
    // ^- im storing dose as Int despite knowing I want to display decimals sometimes. the database
    //    should store the dose here as 100 µL. the UI can do what it wants with this information.
    val doseUnit: String?,
    val dose: Int?,
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
    // Optional note specific to this administration, such as "With breakfast".
    val administrationInstructions: String? = null,
)
