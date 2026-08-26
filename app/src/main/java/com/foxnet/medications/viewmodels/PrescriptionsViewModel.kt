package com.foxnet.medications.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxnet.medications.database.ChartDb
import com.foxnet.medications.database.Medication
import com.foxnet.medications.database.MedicationType
import com.foxnet.medications.database.Prescription
import com.foxnet.medications.database.PrescriptionAdministration
import com.foxnet.medications.database.PrescriptionSchedule
import com.foxnet.medications.database.PrescriptionScheduleKind
import com.foxnet.medications.database.PrescriptionSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PrescriptionCard(val id: Int, val medicationName: String, val medicationType: String, val doseSchedule: String, val nextDose: String, val active: Boolean)

enum class PrescriptionEndCondition { ONGOING, DURATION, END_DATE }

data class AdministrationDraft(
    val id: Int,
    val time: String,
    val useDoseOverride: Boolean = false,
    val overrideDose: String = "",
    val overrideDoseUnit: String = "",
    val instructions: String = "",
)

data class PrescriptionFormState(
    val selectedMedicationId: Int? = null,
    val addingMedication: Boolean = false,
    val newMedicationName: String = "",
    val newMedicationType: MedicationType = MedicationType.CAPSULE,
    val newMedicationInventoryQuantity: String = "0",
    val newMedicationDefaultDose: String = "1",
    val newMedicationDefaultDoseUnit: String = "mg",
    val scheduleKind: PrescriptionScheduleKind = PrescriptionScheduleKind.EVERY_N_HOURS,
    val interval: String = "24",
    val daysOfWeek: Set<Int> = emptySet(),
    val daysOfMonth: String = "",
    val daysOn: String = "",
    val daysOff: String = "",
    val administrations: List<AdministrationDraft> = listOf(AdministrationDraft(id = 0, time = "09:00")),
    val endCondition: PrescriptionEndCondition = PrescriptionEndCondition.ONGOING,
    val durationDays: String = "",
    val endDate: String = "",
)

data class PrescriptionsUiState(
    val prescriptions: List<PrescriptionCard> = emptyList(), val medications: List<Medication> = emptyList(),
    val form: PrescriptionFormState = PrescriptionFormState(), val isSaving: Boolean = false,
    val formError: String? = null, val formSubmissionSucceeded: Boolean = false,
)

private data class Inputs(val prescriptions: List<PrescriptionSummary>, val medications: List<Medication>, val form: PrescriptionFormState)

class PrescriptionsViewModel(private val chart: ChartDb) : ViewModel() {
    private val form = MutableStateFlow(PrescriptionFormState())
    private val isSaving = MutableStateFlow(false)
    private val formError = MutableStateFlow<String?>(null)
    private val formSubmissionSucceeded = MutableStateFlow(false)
    private val json = Json { ignoreUnknownKeys = true }

    private val inputs = combine(chart.observePrescriptions(), chart.observeMedications(), form) { prescriptions, medications, form -> Inputs(prescriptions, medications, form) }
    val uiState: StateFlow<PrescriptionsUiState> = combine(inputs, isSaving, formError, formSubmissionSucceeded) { input, saving, error, succeeded ->
        PrescriptionsUiState(input.prescriptions.map(::toCard), input.medications, input.form, saving, error, succeeded)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrescriptionsUiState())

    fun selectMedication(id: Int) = updateForm { it.copy(selectedMedicationId = id, addingMedication = false) }
    fun addMedication() = updateForm { it.copy(selectedMedicationId = null, addingMedication = true) }
    fun chooseFromInventory() = updateForm { it.copy(addingMedication = false) }
    fun updateForm(transform: (PrescriptionFormState) -> PrescriptionFormState) {
        form.update(transform); formError.value = null; formSubmissionSucceeded.value = false
    }

    fun addAdministration() = updateForm { current ->
        val nextId = (current.administrations.maxOfOrNull(AdministrationDraft::id) ?: -1) + 1
        current.copy(administrations = current.administrations + AdministrationDraft(nextId, "12:00"))
    }

    fun updateAdministration(id: Int, transform: (AdministrationDraft) -> AdministrationDraft) = updateForm { current ->
        current.copy(administrations = current.administrations.map { if (it.id == id) transform(it) else it })
    }

    fun removeAdministration(id: Int) = updateForm { current ->
        current.copy(administrations = current.administrations.filterNot { it.id == id })
    }

    fun savePrescription() {
        val input = form.value
        val medication = uiState.value.medications.firstOrNull { it.id == input.selectedMedicationId }
        val defaultDose = input.newMedicationDefaultDose.toIntOrNull()
        val inventoryQuantity = input.newMedicationInventoryQuantity.toIntOrNull()
        val interval = input.interval.toIntOrNull()
        val administrations = input.administrations.map { draft ->
            val overrideDose = draft.overrideDose.toIntOrNull()
            val time = runCatching { LocalTime.parse(draft.time) }.getOrNull()
            Triple(draft, overrideDose, time)
        }
        val schedule = input.toSchedule(interval)
        val endDate = if (input.endCondition == PrescriptionEndCondition.END_DATE) runCatching { LocalDate.parse(input.endDate) }.getOrNull() else null
        val duration = if (input.endCondition == PrescriptionEndCondition.DURATION) input.durationDays.toIntOrNull()?.takeIf { it > 0 }?.let(Period::ofDays) else null
        val error = when {
            medication == null && !input.addingMedication -> "Choose an inventoried medication or add one."
            medication == null && input.newMedicationName.isBlank() -> "Enter a medication name."
            medication == null && (inventoryQuantity == null || inventoryQuantity < 0) -> "Enter a valid inventory quantity."
            medication == null && (defaultDose == null || defaultDose <= 0) -> "Enter a valid usual dose."
            medication == null && input.newMedicationDefaultDoseUnit.isBlank() -> "Enter a dose unit."
            administrations.any { (draft, dose, _) -> draft.useDoseOverride && (dose == null || dose <= 0) } -> "Enter a valid administration override dose."
            administrations.any { (draft, _, _) -> draft.useDoseOverride && draft.overrideDoseUnit.isBlank() } -> "Enter an administration override dose unit."
            administrations.any { (_, _, time) -> time == null } -> "Use an administration time such as 09:00."
            schedule == null -> "Complete the selected schedule."
            input.endCondition == PrescriptionEndCondition.DURATION && duration == null -> "Enter a valid duration."
            input.endCondition == PrescriptionEndCondition.END_DATE && endDate == null -> "Use an end date such as 2026-12-31."
            endDate != null && endDate < LocalDate.now() -> "The end date must be today or later."
            else -> null
        }
        if (error != null) { formError.value = error; return }
        val prescription = Prescription(
            active = true, medicationId = medication?.id ?: 0, type = 0,
            friendlyName = medication?.name ?: input.newMedicationName.trim(),
            withFood = false, startDate = LocalDate.now(), endDate = endDate, duration = duration, schedule = json.encodeToString(PrescriptionSchedule.serializer(), schedule!!),
        )
        val administrationEntities = administrations.map { (draft, overrideDose, time) ->
            PrescriptionAdministration(
                prescriptionId = 0,
                doseUnit = draft.overrideDoseUnit.trim().takeIf { draft.useDoseOverride },
                dose = overrideDose?.takeIf { draft.useDoseOverride },
                doseMultiplier = 1,
                offset = Period.ZERO,
                time = time,
                event = null,
                administrationInstructions = draft.instructions.trim().takeIf(String::isNotEmpty),
            )
        }
        viewModelScope.launch {
            isSaving.value = true
            try {
                if (medication == null) chart.createMedicationAndPrescription(
                    Medication(
                        name = input.newMedicationName.trim(),
                        type = input.newMedicationType,
                        defaultDose = defaultDose!!,
                        defaultDoseUnit = input.newMedicationDefaultDoseUnit.trim(),
                        inventoryQuantity = inventoryQuantity!!,
                    ),
                    prescription,
                    administrationEntities,
                )
                else chart.createPrescription(medication.id, prescription, administrationEntities)
                form.value = PrescriptionFormState(); formSubmissionSucceeded.value = true
            } finally { isSaving.value = false }
        }
    }

    private fun PrescriptionFormState.toSchedule(interval: Int?): PrescriptionSchedule? = when (scheduleKind) {
        PrescriptionScheduleKind.EVERY_N_HOURS -> interval?.takeIf { it > 0 }?.let { PrescriptionSchedule(scheduleKind, interval = it) }
        PrescriptionScheduleKind.EVERY_N_DAYS -> interval?.takeIf { it > 0 }?.let { PrescriptionSchedule(scheduleKind, interval = it) }
        PrescriptionScheduleKind.WEEKLY -> interval?.takeIf { it > 0 }?.takeIf { daysOfWeek.isNotEmpty() }?.let { PrescriptionSchedule(scheduleKind, it, daysOfWeek = daysOfWeek) }
        PrescriptionScheduleKind.MONTHLY -> interval?.takeIf { it > 0 }?.takeIf { daysOfMonth.split(',').mapNotNull(String::trim).mapNotNull(String::toIntOrNull).all { day -> day in 1..31 } }?.let {
            PrescriptionSchedule(scheduleKind, it, daysOfMonth = daysOfMonth.split(',').mapNotNull(String::trim).mapNotNull(String::toIntOrNull).toSet())
        }
        PrescriptionScheduleKind.ON_OFF_CYCLE -> daysOn.toIntOrNull()?.takeIf { it > 0 }?.let { on -> daysOff.toIntOrNull()?.takeIf { it >= 0 }?.let { off -> PrescriptionSchedule(scheduleKind, daysOn = on, daysOff = off) } }
    }

    private fun toCard(summary: PrescriptionSummary): PrescriptionCard {
        val schedule = runCatching { json.decodeFromString(PrescriptionSchedule.serializer(), summary.schedule) }.getOrElse { PrescriptionSchedule(PrescriptionScheduleKind.EVERY_N_HOURS, 24) }
        return PrescriptionCard(summary.prescriptionId, summary.medicationName, summary.medicationType.displayName(), "${summary.dose} ${summary.doseUnit} ${schedule.description()}", summary.nextDose(schedule), summary.active)
    }
}

private fun PrescriptionSchedule.description(): String = when (kind) {
    PrescriptionScheduleKind.EVERY_N_HOURS -> "every $interval hour${if (interval == 1) "" else "s"}"
    PrescriptionScheduleKind.EVERY_N_DAYS -> "every $interval day${if (interval == 1) "" else "s"}"
    PrescriptionScheduleKind.WEEKLY -> "every $interval week${if (interval == 1) "" else "s"} on ${daysOfWeek.sorted().joinToString { it.dayName() }}"
    PrescriptionScheduleKind.MONTHLY -> "every $interval month${if (interval == 1) "" else "s"} on day ${daysOfMonth.sorted().joinToString()}"
    PrescriptionScheduleKind.ON_OFF_CYCLE -> "$daysOn days on, $daysOff days off"
}
private fun Int.dayName() = java.time.DayOfWeek.of(this).name.lowercase().replaceFirstChar(Char::uppercase)
private fun MedicationType.displayName() = name.lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase(Locale.getDefault()) }

private fun PrescriptionSummary.nextDose(schedule: PrescriptionSchedule): String {
    if (!active) return "Inactive"
    val now = LocalDateTime.now(); val time = administrationTime ?: LocalTime.MIDNIGHT
    val next = nextScheduledDateTime(schedule, now, time) ?: return "No upcoming dose"
    val finalDate = endDate ?: duration?.let(startDate::plus)
    if (finalDate != null && next.toLocalDate().isAfter(finalDate)) return "Course complete"
    val formattedTime = next.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    return when (ChronoUnit.DAYS.between(now.toLocalDate(), next.toLocalDate())) {
        0L -> "Next dose today at $formattedTime"
        1L -> "Next dose tomorrow at $formattedTime"
        else -> "Next dose ${next.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))} at $formattedTime"
    }
}

private fun PrescriptionSummary.nextScheduledDateTime(schedule: PrescriptionSchedule, now: LocalDateTime, time: LocalTime): LocalDateTime? {
    if (schedule.kind == PrescriptionScheduleKind.EVERY_N_HOURS) {
        var dose = LocalDateTime.of(startDate, time)
        while (!dose.isAfter(now)) dose = dose.plusHours(schedule.interval.toLong())
        return dose
    }
    var date = maxOf(startDate, now.toLocalDate())
    repeat(3660) {
        val candidate = LocalDateTime.of(date, time)
        if (candidate.isAfter(now) && schedule.appliesOn(date, startDate)) return candidate
        date = date.plusDays(1)
    }
    return null
}

private fun PrescriptionSchedule.appliesOn(date: LocalDate, start: LocalDate): Boolean = when (kind) {
    PrescriptionScheduleKind.WEEKLY -> ChronoUnit.WEEKS.between(start, date) % interval == 0L && date.dayOfWeek.value in daysOfWeek
    PrescriptionScheduleKind.MONTHLY -> ChronoUnit.MONTHS.between(start.withDayOfMonth(1), date.withDayOfMonth(1)) % interval == 0L && date.dayOfMonth in daysOfMonth
    PrescriptionScheduleKind.ON_OFF_CYCLE -> ChronoUnit.DAYS.between(start, date).let { it >= 0 && it % (daysOn + daysOff) < daysOn }
    PrescriptionScheduleKind.EVERY_N_DAYS -> ChronoUnit.DAYS.between(start, date).let { it >= 0 && it % interval == 0L }
    PrescriptionScheduleKind.EVERY_N_HOURS -> false
}
