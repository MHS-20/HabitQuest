package compose.project.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    token: String,
    avatarState: AvatarUiState,
    onRefreshStats: () -> Unit,
) {
    var showContent by remember { mutableStateOf(true) }
    val habitRepository = remember { HabitsApiRepository() }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var recurrenceType by remember { mutableStateOf(CreateHabitRecurrenceType.DAILY) }
    var dayOfWeek by remember { mutableStateOf("MONDAY") }
    var dayOfMonth by remember { mutableStateOf("1") }
    var isCreatingHabit by remember { mutableStateOf(false) }
    var habitMessage by remember { mutableStateOf<String?>(null) }
    var showCreateHabitDialog by remember { mutableStateOf(false) }
    var historyState by remember { mutableStateOf<DashboardHistoryUiState>(DashboardHistoryUiState.Loading) }

    LaunchedEffect(token, avatarState) {
        val avatar = (avatarState as? AvatarUiState.Ready)?.avatar
        historyState = when {
            token.isBlank() -> DashboardHistoryUiState.Error("Invalid session")
            avatar == null -> DashboardHistoryUiState.Error("Avatar not available")
            else -> when (val result = habitRepository.fetchHistoryByAvatar(token, avatar.id)) {
                is HabitHistoryResult.Success -> DashboardHistoryUiState.Ready(result.items)
                is HabitHistoryResult.Error -> DashboardHistoryUiState.Error(result.message)
            }
        }
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
//        Button(onClick = { showContent = !showContent }) {
//            Text(if (showContent) "Nascondi" else "Mostra")
//        }

        AnimatedVisibility(showContent) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(12.dp))
                when (val state = avatarState) {
                    AvatarUiState.Loading -> Text("Loading avatar...")
                    is AvatarUiState.Error -> Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )

                    is AvatarUiState.Ready -> {
                        AvatarCard(
                            avatar = state.avatar,
                            onRefreshStats = onRefreshStats,
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("Avatar data updated by avatar-service")

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        habitMessage = null
                        showCreateHabitDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("New Habit")
                }

                if (showCreateHabitDialog) {
                    AlertDialog(
                        onDismissRequest = { showCreateHabitDialog = false },
                        title = { Text("New Habit") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = {
                                        title = it
                                        habitMessage = null
                                    },
                                    label = { Text("Title") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = description,
                                    onValueChange = {
                                        description = it
                                        habitMessage = null
                                    },
                                    label = { Text("Description") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = {
                                            recurrenceType = CreateHabitRecurrenceType.DAILY
                                            habitMessage = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Daily") }
                                    Spacer(Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            recurrenceType = CreateHabitRecurrenceType.WEEKLY
                                            habitMessage = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Weekly") }
                                    Spacer(Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            recurrenceType = CreateHabitRecurrenceType.MONTHLY
                                            habitMessage = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Monthly") }
                                }

                                Text("Recurrence: ${recurrenceType.name}")

                                if (recurrenceType == CreateHabitRecurrenceType.WEEKLY) {
                                    OutlinedTextField(
                                        value = dayOfWeek,
                                        onValueChange = {
                                            dayOfWeek = it.uppercase()
                                            habitMessage = null
                                        },
                                        label = { Text("Day Of Week (e.g. MONDAY)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (recurrenceType == CreateHabitRecurrenceType.MONTHLY) {
                                    OutlinedTextField(
                                        value = dayOfMonth,
                                        onValueChange = {
                                            dayOfMonth = it
                                            habitMessage = null
                                        },
                                        label = { Text("Day Of Month (1-31)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val readyAvatar = (avatarState as? AvatarUiState.Ready)?.avatar
                                    if (readyAvatar == null) {
                                        habitMessage = "Avatar not available"
                                        return@TextButton
                                    }

                                    if (title.isBlank()) {
                                        habitMessage = "Enter a title"
                                        return@TextButton
                                    }

                                    val normalizedDayOfWeek =
                                        if (recurrenceType == CreateHabitRecurrenceType.WEEKLY) dayOfWeek.trim().uppercase()
                                        else null
                                    val normalizedDayOfMonth =
                                        if (recurrenceType == CreateHabitRecurrenceType.MONTHLY) dayOfMonth.toIntOrNull()
                                        else null

                                    if (recurrenceType == CreateHabitRecurrenceType.WEEKLY && normalizedDayOfWeek !in allowedDaysOfWeek) {
                                        habitMessage = "Invalid Day Of Week"
                                        return@TextButton
                                    }

                                    if (recurrenceType == CreateHabitRecurrenceType.MONTHLY && (normalizedDayOfMonth == null || normalizedDayOfMonth !in 1..31)) {
                                        habitMessage = "Day Of Month must be between 1 and 31"
                                        return@TextButton
                                    }

                                    scope.launch {
                                        isCreatingHabit = true
                                        habitMessage = null
                                        when (
                                            val result = habitRepository.createHabit(
                                                token = token,
                                                avatarId = readyAvatar.id,
                                                title = title.trim(),
                                                description = description.trim(),
                                                recurrenceType = recurrenceType,
                                                dayOfWeek = normalizedDayOfWeek,
                                                dayOfMonth = normalizedDayOfMonth
                                            )
                                        ) {
                                            is CreateHabitResult.Success -> {
                                                habitMessage = "Habit created (id: ${result.habitId})"
                                                title = ""
                                                description = ""
                                                showCreateHabitDialog = false
                                            }

                                            is CreateHabitResult.Error -> {
                                                habitMessage = result.message
                                            }
                                        }
                                        isCreatingHabit = false
                                    }
                                },
                                enabled = !isCreatingHabit
                            ) {
                                if (isCreatingHabit) {
                                    CircularProgressIndicator(strokeWidth = 2.dp)
                                } else {
                                    Text("Create")
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCreateHabitDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (habitMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = habitMessage.orEmpty(),
                        color = if (habitMessage?.startsWith("Habit created") == true) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))
                Text("Habit history", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                when (val state = historyState) {
                    DashboardHistoryUiState.Loading -> Text("Loading history...")
                    is DashboardHistoryUiState.Error -> Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )

                    is DashboardHistoryUiState.Ready -> {
                        if (state.items.isEmpty()) {
                            Text("No history events")
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = state.items,
                                    key = { "${it.occurredAt}-${it.eventType}-${it.habitId}" }
                                ) { event ->
                                    HistoryEventCard(item = event)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val allowedDaysOfWeek = setOf(
    "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
)

@Composable
private fun HistoryEventCard(item: HabitHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.eventType, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Date: ${item.occurredAt}", style = MaterialTheme.typography.bodySmall)
            Text("Habit ID: ${item.habitId}", style = MaterialTheme.typography.bodySmall)
            if (item.details.isNotBlank()) {
                Text("Details: ${item.details}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private sealed interface DashboardHistoryUiState {
    data object Loading : DashboardHistoryUiState
    data class Ready(val items: List<HabitHistoryItem>) : DashboardHistoryUiState
    data class Error(val message: String) : DashboardHistoryUiState
}

