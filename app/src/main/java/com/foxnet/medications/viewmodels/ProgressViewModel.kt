package com.foxnet.medications.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxnet.medications.database.ChartDb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Task (
    val id: Int,
    val importance: Int,
    val title: String,
    val completed: Boolean = false,
    val alarmed: Boolean = false
)

data class TodayUIState(
    val dayName: String = "",
    val tasks: List<Task> = emptyList()
)

class ProgressViewModel(
    chart: ChartDb
) : ViewModel() {
    private val _currentDate = MutableStateFlow(LocalDate.now())
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())

    private val _dayNameFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())

    val todayUiState: StateFlow<TodayUIState> = combine(_currentDate, _tasks) { date, tasks ->
        TodayUIState(
            dayName = date.format(_dayNameFormatter),
            tasks = tasks
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodayUIState(
            dayName = LocalDate.now().format(_dayNameFormatter),
            tasks = emptyList()
        )
    )

    fun addTask(newTask: Task) {
        _tasks.update { currentList ->
            currentList + newTask
        }
    }

    fun completeDailyTask(taskId: Int) {
        _tasks.update { currentList ->
            currentList.map { task ->
                if (task.id == taskId) {
                    task.copy(completed = !task.completed)
                } else {
                    task
                }
            }
        }
    }

    fun loadTasks() {
        _tasks.value = listOf(
            Task(0, 1, "Prozac"),
            Task(1, 1, "Progesterone"),
            Task(2, 1, "Dutasteride")
        )
    }

    init {
        loadTasks()
    }
}