package com.foxnet.medications.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxnet.medications.database.AdministrationRecord
import com.foxnet.medications.database.ChartDb
import com.foxnet.medications.database.PrescriptionAdministrationTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AdministrationTask(
    val id: Int,
    val title: String,
    val administrationRecordId: Int?,
    val outcome: AdministrationOutcome?,
    val medicationId: Int,
    val doseUnit: String,
    val dose: Int,
    val withFood: Boolean,
)

enum class AdministrationOutcome {
    TAKEN,
    SKIPPED,
}

data class TaskGroup(
    val label: String,
    val tasks: List<AdministrationTask>,
)

data class TodayUIState(
    val dayName: String = "",
    val taskGroups: List<TaskGroup> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModel(
    private val chart: ChartDb,
) : ViewModel() {
    private val currentDate = MutableStateFlow(LocalDate.now())
    private val dayNameFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

    val todayUiState: StateFlow<TodayUIState> = currentDate
        .flatMapLatest(chart::observePrescriptionAdministrations)
        .combine(currentDate) { administrations, date ->
            TodayUIState(
                dayName = date.format(dayNameFormatter),
                taskGroups = administrations.toTaskGroups(),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodayUIState(dayName = LocalDate.now().format(dayNameFormatter)),
        )

    fun recordAdministration(taskId: Int, outcome: AdministrationOutcome) {
        val date = currentDate.value
        val task = todayUiState.value.taskGroups
            .asSequence()
            .flatMap { it.tasks.asSequence() }
            .firstOrNull { it.id == taskId }
            ?: return

        viewModelScope.launch {
            val record = AdministrationRecord(
                id = task.administrationRecordId ?: 0,
                medicationId = task.medicationId,
                prescriptionAdministrationId = task.id,
                skipped = outcome == AdministrationOutcome.SKIPPED,
                doseUnit = task.doseUnit,
                dose = task.dose,
                withFood = task.withFood,
                date = date,
                time = LocalTime.now(),
            )
            if (task.administrationRecordId == null) {
                chart.insertAdministrationRecord(record)
            } else {
                chart.updateAdministrationRecord(record)
            }
        }
    }

    private fun List<PrescriptionAdministrationTask>.toTaskGroups(): List<TaskGroup> =
        groupBy { it.time }
            .map { (time, administrations) ->
                TaskGroup(
                    label = time?.format(timeFormatter) ?: "As needed",
                    tasks = administrations.map { administration ->
                        AdministrationTask(
                            id = administration.prescriptionAdministrationId,
                            title = administration.medicationName,
                            administrationRecordId = administration.administrationRecordId,
                            outcome = administration.skipped?.let { skipped ->
                                if (skipped) AdministrationOutcome.SKIPPED else AdministrationOutcome.TAKEN
                            },
                            medicationId = administration.medicationId,
                            doseUnit = administration.doseUnit,
                            dose = administration.dose,
                            withFood = administration.withFood,
                        )
                    },
                )
            }
}
