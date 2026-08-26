package com.foxnet.medications.database

import kotlinx.serialization.Serializable

/** A serializable schedule stored in [Prescription.schedule]. Day values use ISO numbering: 1–7. */
@Serializable
data class PrescriptionSchedule(
    val kind: PrescriptionScheduleKind,
    val interval: Int = 1,
    val daysOfWeek: Set<Int> = emptySet(),
    val daysOfMonth: Set<Int> = emptySet(),
    val daysOn: Int = 0,
    val daysOff: Int = 0,
)

@Serializable
enum class PrescriptionScheduleKind {
    EVERY_N_HOURS,
    EVERY_N_DAYS,
    WEEKLY,
    MONTHLY,
    ON_OFF_CYCLE,
}
