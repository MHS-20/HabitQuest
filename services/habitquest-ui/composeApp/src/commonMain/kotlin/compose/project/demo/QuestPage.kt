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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun QuestScreen(token: String, avatarState: AvatarUiState) {
    val repository = remember { QuestRepository() }
    val habitRepository = remember { HabitsApiRepository() }
    val scope = rememberCoroutineScope()

    var questName by remember { mutableStateOf("") }
    var questDuration by remember { mutableStateOf("PT30M") }
    var searchText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var quests by remember { mutableStateOf<List<QuestData>>(emptyList()) }
    var availableHabits by remember { mutableStateOf<List<HabitListItem>>(emptyList()) }
    var selectedHabitIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    suspend fun loadQuests() {
        when (val result = repository.fetchAllQuests(token)) {
            is QuestListResult.Success -> quests = result.quests
            is QuestListResult.Error -> message = result.message
        }
    }

    suspend fun loadAvatarHabits() {
        val avatar = (avatarState as? AvatarUiState.Ready)?.avatar
        if (avatar == null) {
            availableHabits = emptyList()
            selectedHabitIds = emptySet()
            return
        }
        when (val result = habitRepository.fetchHabitsByAvatar(token, avatar.id)) {
            is HabitListResult.Success -> {
                availableHabits = result.habits
                selectedHabitIds = selectedHabitIds.intersect(result.habits.map { it.id }.toSet())
            }
            is HabitListResult.Error -> {
                availableHabits = emptyList()
            }
        }
    }

    LaunchedEffect(token) {
        isLoading = true
        message = null
        loadQuests()
        isLoading = false
    }

    LaunchedEffect(token, avatarState) {
        loadAvatarHabits()
    }

    val normalizedSearch = searchText.trim()
    val filteredQuests = if (normalizedSearch.isBlank()) {
        quests
    } else {
        val needle = normalizedSearch.lowercase()
        quests.filter { quest ->
            quest.name.lowercase().contains(needle) || quest.id.lowercase().contains(needle)
        }
    }

    val selectedHabits = availableHabits.filter { it.id in selectedHabitIds }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Quest", style = MaterialTheme.typography.headlineSmall)

        Button(
            onClick = {
                message = null
                showCreateDialog = true
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create quest")
        }

        Text("Quest list", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            label = { Text("Search quest by name or id") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (isLoading) {
            CircularProgressIndicator()
        }

        message?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (!isLoading) {
            if (filteredQuests.isEmpty()) {
                if (quests.isEmpty()) {
                    Text("No quests available")
                } else {
                    Text("No quests found for '${normalizedSearch}'")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredQuests, key = { it.id }) { quest ->
                        QuestRow(quest)
                    }
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create new quest") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = questName,
                            onValueChange = {
                                questName = it
                                message = null
                            },
                            label = { Text("New quest name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = questDuration,
                            onValueChange = {
                                questDuration = it
                                message = null
                            },
                            label = { Text("Duration (e.g. PT30M, PT2H)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Select habits to include", style = MaterialTheme.typography.bodyMedium)

                        if (availableHabits.isEmpty()) {
                            Text("No habits available for the current avatar")
                        } else {
                            LazyColumn(
                                modifier = Modifier.height(140.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(availableHabits, key = { it.id }) { habit ->
                                    val isSelected = habit.id in selectedHabitIds
                                    OutlinedButton(
                                        onClick = {
                                            selectedHabitIds = if (isSelected) {
                                                selectedHabitIds - habit.id
                                            } else {
                                                selectedHabitIds + habit.id
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (isSelected) "[x] ${habit.title}" else "[ ] ${habit.title}")
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (questName.isBlank()) {
                                message = "Enter the quest name"
                                return@TextButton
                            }
                            if (questDuration.isBlank()) {
                                message = "Enter a valid duration (e.g. PT30M)"
                                return@TextButton
                            }
                            scope.launch {
                                isLoading = true
                                message = null
                                when (
                                    val result = repository.createQuestWithDetails(
                                        token = token,
                                        name = questName.trim(),
                                        duration = questDuration.trim(),
                                        habits = selectedHabits,
                                    )
                                ) {
                                    is CreateQuestResult.Success -> {
                                        message = "Quest created: ${result.questId}"
                                        questName = ""
                                        selectedHabitIds = emptySet()
                                        loadQuests()
                                        showCreateDialog = false
                                    }

                                    is CreateQuestResult.Error -> {
                                        message = result.message
                                    }
                                }
                                isLoading = false
                            }
                        },
                        enabled = !isLoading,
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun QuestRow(quest: QuestData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(quest.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("ID: ${quest.id}", style = MaterialTheme.typography.bodySmall)
            Text("Duration: ${quest.duration}", style = MaterialTheme.typography.bodySmall)
            Text("Reward: ${quest.reward}", style = MaterialTheme.typography.bodySmall)
            Text(
                "Linked habits: ${if (quest.habitIds.isEmpty()) "none" else quest.habitIds.joinToString()}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
