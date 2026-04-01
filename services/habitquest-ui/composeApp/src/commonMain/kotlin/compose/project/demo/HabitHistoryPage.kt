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
fun HabitHistoryScreen(token: String, avatarState: AvatarUiState) {
    val habitRepository = remember { HabitsApiRepository() }
    var uiState by remember { mutableStateOf<HabitHistoryUiState>(HabitHistoryUiState.Loading) }

    LaunchedEffect(token, avatarState) {
        val avatar = (avatarState as? AvatarUiState.Ready)?.avatar
        uiState = when {
            token.isBlank() -> HabitHistoryUiState.Error("Sessione non valida")
            avatar == null -> HabitHistoryUiState.Error("Avatar non disponibile")
            else -> when (val result = habitRepository.fetchHistoryByAvatar(token, avatar.id)) {
                is HabitHistoryResult.Success -> HabitHistoryUiState.Ready(result.items)
                is HabitHistoryResult.Error -> HabitHistoryUiState.Error(result.message)
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
        Text("Storico abitudini", style = MaterialTheme.typography.headlineSmall)

        when (val state = uiState) {
            HabitHistoryUiState.Loading -> Text("Caricamento storico...")
            is HabitHistoryUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is HabitHistoryUiState.Ready -> {
                if (state.items.isEmpty()) {
                    Text("Nessun evento nello storico")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.items, key = { "${it.occurredAt}-${it.eventType}-${it.habitId}" }) { item ->
                            HabitHistoryRow(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitHistoryRow(item: HabitHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.eventType, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Data: ${item.occurredAt}", style = MaterialTheme.typography.bodySmall)
            Text("Habit ID: ${item.habitId}", style = MaterialTheme.typography.bodySmall)
            if (item.details.isNotBlank()) {
                Text("Dettagli: ${item.details}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private sealed interface HabitHistoryUiState {
    data object Loading : HabitHistoryUiState
    data class Ready(val items: List<HabitHistoryItem>) : HabitHistoryUiState
    data class Error(val message: String) : HabitHistoryUiState
}

