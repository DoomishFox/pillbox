package com.foxnet.medications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foxnet.medications.Database.ChartDb
import com.foxnet.medications.Database.PersistentViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Locale
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DailyTask (
    val id: Int,
    val importance: Int,
    val title: String,
    val completed: Boolean = false,
    val alarmed: Boolean = false
)

data class TodayUIState(
    val dayName: String = "",
    val tasks: List<DailyTask> = emptyList()
)

class ProgressViewModel(
    chart: ChartDb
) : ViewModel() {
    private val _currentDate = MutableStateFlow(LocalDate.now())
    private val _tasks = MutableStateFlow<List<DailyTask>>(emptyList())

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

    fun addTask(newTask: DailyTask) {
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
            DailyTask(0, 1, "task 1"),
            DailyTask(1, 1, "task 2"),
            DailyTask(2, 1, "task 3")
        )
    }

    init {
        loadTasks()
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
public fun Progress(
    context: android.content.Context = LocalContext.current,
    viewModel: ProgressViewModel = viewModel(
        factory = remember { PersistentViewModelFactory(context) }
    ),
) {
    val todayState by viewModel.todayUiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState),
    ) {
        TopAppBar(
            expandedHeight = TopAppBarDefaults.LargeAppBarExpandedHeight,
            title = {
                Text(
                    todayState.dayName,
                    style = MaterialTheme.typography.displayMediumEmphasized,
                )
            },
            modifier = Modifier.consumeWindowInsets(TopAppBarDefaults.windowInsets)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            OutlinedCard(
                shape = CardDefaults.shape,
                colors = CardDefaults.outlinedCardColors(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                todayState.tasks.forEach { task ->
                    ListItem(
                        checked = task.completed,
                        onCheckedChange = { viewModel.completeDailyTask(task.id) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.pill_24px),
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Checkbox(
                                checked = task.completed,
                                onCheckedChange = null,
                            )
                        },
                        content = { Text(task.title) }
                    )
                }
            }
            Card(
                shape = CardDefaults.shape,
                colors = CardDefaults.cardColors(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column() {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            Text(
                                "Yesterday",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                ) {
                                    Text("0", style = MaterialTheme.typography.displayMediumEmphasized, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 16.dp))
                                    Text("Skipped", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                ) {
                                    Text("2", style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(horizontal = 16.dp))
                                    Text("As needed", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Button(onClick = {}) {
                            Text("More")
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Icon(
                                painter = painterResource(R.drawable.arrow_forward_24px),
                                contentDescription = "More details",
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                        }
                    }
                }
            }
            Card(
                shape = CardDefaults.shape,
                colors = CardDefaults.cardColors(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column() {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Text(
                            "History",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .padding(16.dp)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Button(onClick = {}) {
                            Text("More")
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Icon(
                                painter = painterResource(R.drawable.arrow_forward_24px),
                                contentDescription = "More details",
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                        }
                    }
                }
            }
        }
    }
}
