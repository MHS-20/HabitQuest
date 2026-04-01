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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
fun QuestScreen(token: String) {
    val repository = remember { QuestRepository() }
    val scope = rememberCoroutineScope()

    var questName by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var quests by remember { mutableStateOf<List<QuestData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    suspend fun loadQuests() {
        when (val result = repository.fetchAllQuests(token)) {
            is QuestListResult.Success -> quests = result.quests
            is QuestListResult.Error -> message = result.message
        }
    }

    LaunchedEffect(token) {
        isLoading = true
        message = null
        loadQuests()
        isLoading = false
    }

    val filteredQuests = quests.filter { quest ->
        if (searchText.isBlank()) return@filter true
        val needle = searchText.trim().lowercase()
        quest.name.lowercase().contains(needle) || quest.id.lowercase().contains(needle)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Quest", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = questName,
            onValueChange = {
                questName = it
                message = null
            },
            label = { Text("Nome nuova quest") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (questName.isBlank()) {
                    message = "Inserisci il nome della quest"
                    return@Button
                }
                scope.launch {
                    isLoading = true
                    message = null
                    when (val result = repository.createQuest(token, questName.trim())) {
                        is CreateQuestResult.Success -> {
                            message = "Quest creata: ${result.questId}"
                            questName = ""
                            loadQuests()
                        }

                        is CreateQuestResult.Error -> {
                            message = result.message
                        }
                    }
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crea quest")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            label = { Text("Cerca quest per nome o id") },
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
                Text("Nessuna quest trovata")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredQuests, key = { it.id }) { quest ->
                        QuestRow(quest)
                    }
                }
            }
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
            Text("Durata: ${quest.duration}", style = MaterialTheme.typography.bodySmall)
            Text("Reward: ${quest.reward}", style = MaterialTheme.typography.bodySmall)
            Text(
                "Habits collegate: ${if (quest.habitIds.isEmpty()) "nessuna" else quest.habitIds.joinToString()}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

