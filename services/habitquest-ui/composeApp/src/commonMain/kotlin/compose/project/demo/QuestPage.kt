package compose.project.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Slider
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
    var selectedDurationDays by remember { mutableStateOf(14f) }
    var searchText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var quests by remember { mutableStateOf<List<QuestData>>(emptyList()) }
    var progress by remember { mutableStateOf<List<QuestProgressData>>(emptyList()) }
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

    suspend fun loadProgressForAvatar() {
        val avatar = (avatarState as? AvatarUiState.Ready)?.avatar
        if (avatar == null) {
            progress = emptyList()
            return
        }

        when (val result = repository.fetchActiveProgressByAvatar(token, avatar.id)) {
            is QuestProgressListResult.Success -> progress = result.progress
            is QuestProgressListResult.Error -> message = result.message
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
        loadProgressForAvatar()
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
                        QuestRow(
                            quest = quest,
                            canJoin = avatarState is AvatarUiState.Ready,
                            onJoin = {
                                val avatarId = (avatarState as? AvatarUiState.Ready)?.avatar?.id
                                if (avatarId.isNullOrBlank()) {
                                    message = "Avatar not available for this session"
                                    return@QuestRow
                                }

                                scope.launch {
                                    isLoading = true
                                    message = null
                                    when (val result = repository.joinQuest(token, quest.id, avatarId)) {
                                        is JoinQuestResult.Success -> {
                                            message = "Joined quest: ${quest.name}"
                                            loadProgressForAvatar()
                                        }

                                        is JoinQuestResult.Error -> {
                                            message = result.message
                                        }
                                    }
                                    isLoading = false
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Active quest progress", style = MaterialTheme.typography.titleMedium)
        if (progress.isEmpty()) {
            Text("No active quest progress for this avatar")
        } else {
            LazyColumn(
                modifier = Modifier.height(220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(progress, key = { it.questId }) { item ->
                    QuestProgressRow(item)
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

                        Text("Duration", style = MaterialTheme.typography.bodyMedium)

                        Text("${selectedDurationDays.toInt()} days", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = selectedDurationDays,
                            onValueChange = {
                                selectedDurationDays = it
                                message = null
                            },
                            valueRange = 1f..90f,
                            steps = 88,
                            modifier = Modifier.fillMaxWidth()
                        )

                        val selectedDurationPreviewDays = resolveQuestDurationDays(
                            days = selectedDurationDays.toInt()
                        )
                        Text(
                            "Selected: ${selectedDurationPreviewDays ?: 0} days",
                            style = MaterialTheme.typography.bodySmall
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
                            val durationDays = resolveQuestDurationDays(
                                days = selectedDurationDays.toInt()
                            )
                            if (durationDays == null) {
                                message = "Duration must be a positive number of days"
                                return@TextButton
                            }
                            scope.launch {
                                isLoading = true
                                message = null
                                when (
                                    val result = repository.createQuestWithDetails(
                                        token = token,
                                        name = questName.trim(),
                                        durationDays = durationDays,
                                        habits = selectedHabits,
                                    )
                                ) {
                                    is CreateQuestResult.Success -> {
                                        message = "Quest created: ${result.questId}"
                                        questName = ""
                                        selectedDurationDays = 14f
                                        selectedHabitIds = emptySet()
                                        loadQuests()
                                        loadProgressForAvatar()
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

private fun resolveQuestDurationDays(
    days: Int,
): Int? {
    if (days <= 0) return null
    return days
}

@Composable
private fun QuestProgressRow(progress: QuestProgressData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(progress.questName, style = MaterialTheme.typography.titleSmall)
            Text("Status: ${progress.status}", style = MaterialTheme.typography.bodySmall)
            Text("Completion: ${progress.completionPercentage}%", style = MaterialTheme.typography.bodySmall)
            if (progress.habits.isEmpty()) {
                Text("No linked habits", style = MaterialTheme.typography.bodySmall)
            } else {
                progress.habits.forEach { habit ->
                    Text(
                        "- ${habit.title}: required ${habit.requiredOccurrences}, attended ${habit.attendedOccurrences}, remaining ${habit.remainingOccurrences}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestRow(quest: QuestData, canJoin: Boolean, onJoin: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(quest.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("ID: ${quest.id}", style = MaterialTheme.typography.bodySmall)
            Text("Duration: ${quest.durationDays} days", style = MaterialTheme.typography.bodySmall)
            Text("Reward: ${quest.reward}", style = MaterialTheme.typography.bodySmall)
            Text(
                "Linked habits: ${if (quest.habitIds.isEmpty()) "none" else quest.habitIds.joinToString()}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onJoin,
                enabled = canJoin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join quest")
            }
        }
    }
}
