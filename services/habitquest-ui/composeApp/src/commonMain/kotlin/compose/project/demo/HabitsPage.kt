package compose.project.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun HabitsScreen(
    token: String,
    avatarState: AvatarUiState,
    onHabitAttended: () -> Unit = {},
) {
    val habitRepository = remember { HabitsApiRepository() }
    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf<HabitsUiState>(HabitsUiState.Loading) }
    var attendingHabitId by remember { mutableStateOf<String?>(null) }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    suspend fun loadHabits(showLoading: Boolean) {
        val avatar = (avatarState as? AvatarUiState.Ready)?.avatar
        if (showLoading) {
            uiState = HabitsUiState.Loading
        }
        uiState = when {
            token.isBlank() -> HabitsUiState.Error("Sessione non valida")
            avatar == null -> HabitsUiState.Error("Avatar non disponibile")
            else -> when (val result = habitRepository.fetchHabitsByAvatar(token, avatar.id)) {
                is HabitListResult.Success -> HabitsUiState.Ready(result.habits)
                is HabitListResult.Error -> HabitsUiState.Error(result.message)
            }
        }
    }

    LaunchedEffect(token, avatarState) {
        loadHabits(showLoading = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Le mie abitudini", style = MaterialTheme.typography.headlineSmall)

        actionMessage?.let { message ->
            Text(
                text = message,
                color = if (message.startsWith("Habit completata")) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }

        when (val state = uiState) {
            HabitsUiState.Loading -> Text("Caricamento habits...")
            is HabitsUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is HabitsUiState.Ready -> {
                if (state.habits.isEmpty()) {
                    Text("Nessuna habit trovata")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.habits, key = { it.id }) { habit ->
                            HabitRow(
                                habit = habit,
                                isAttending = attendingHabitId == habit.id,
                                onAttend = {
                                    scope.launch {
                                        attendingHabitId = habit.id
                                        actionMessage = null
                                        when (val result = habitRepository.attendHabit(token, habit.id)) {
                                            AttendHabitResult.Success -> {
                                                actionMessage = "Habit completata: ${habit.title}"
                                                onHabitAttended()
                                                loadHabits(showLoading = false)
                                            }

                                            is AttendHabitResult.Error -> {
                                                actionMessage = result.message
                                            }
                                        }
                                        attendingHabitId = null
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitRow(
    habit: HabitListItem,
    isAttending: Boolean,
    onAttend: () -> Unit,
) {
    val tagsText = habit.tags.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "Nessun tag"
    val lastAttendedText = habit.lastAttendedDate ?: "Non disponibile"
    val nextRecurrenceText = habit.nextRecurrenceDate ?: "Non disponibile"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(habit.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(habit.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text("Recurrence: ${habit.recurrenceType}", style = MaterialTheme.typography.bodySmall)
            Text("Tags: $tagsText", style = MaterialTheme.typography.bodySmall)
            Text("Last attended: $lastAttendedText", style = MaterialTheme.typography.bodySmall)
            Text("Next recurrence: $nextRecurrenceText", style = MaterialTheme.typography.bodySmall)
            Text("ID: ${habit.id}", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onAttend,
                enabled = !isAttending,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isAttending) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Attendo...")
                } else {
                    Text("Segna come completata")
                }
            }
        }
    }
}

private sealed interface HabitsUiState {
    data object Loading : HabitsUiState
    data class Ready(val habits: List<HabitListItem>) : HabitsUiState
    data class Error(val message: String) : HabitsUiState
}
