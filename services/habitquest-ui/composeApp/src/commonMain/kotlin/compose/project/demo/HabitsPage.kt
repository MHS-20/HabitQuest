package compose.project.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HabitsScreen(token: String, avatarState: AvatarUiState) {
    val habitRepository = remember { HabitRepository() }
    var uiState by remember { mutableStateOf<HabitsUiState>(HabitsUiState.Loading) }

    LaunchedEffect(token, avatarState) {
        val avatar = (avatarState as? AvatarUiState.Ready)?.avatar
        uiState = when {
            token.isBlank() -> HabitsUiState.Error("Sessione non valida")
            avatar == null -> HabitsUiState.Error("Avatar non disponibile")
            else -> when (val result = habitRepository.fetchHabitsByAvatar(token, avatar.id)) {
                is HabitListResult.Success -> HabitsUiState.Ready(result.habits)
                is HabitListResult.Error -> HabitsUiState.Error(result.message)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Le mie abitudini", style = MaterialTheme.typography.headlineSmall)

        when (val state = uiState) {
            HabitsUiState.Loading -> Text("Caricamento habits...")
            is HabitsUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is HabitsUiState.Ready -> {
                if (state.habits.isEmpty()) {
                    Text("Nessuna habit trovata")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.habits, key = { it.id }) { habit ->
                            HabitRow(habit)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitRow(habit: HabitListItem) {
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
            Text("ID: ${habit.id}", style = MaterialTheme.typography.labelSmall)
        }
    }
}

private sealed interface HabitsUiState {
    data object Loading : HabitsUiState
    data class Ready(val habits: List<HabitListItem>) : HabitsUiState
    data class Error(val message: String) : HabitsUiState
}

