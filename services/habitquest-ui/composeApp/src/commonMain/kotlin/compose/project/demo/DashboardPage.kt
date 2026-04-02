package compose.project.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
fun DashboardScreen(token: String, avatarState: AvatarUiState) {
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
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(12.dp))
                when (val state = avatarState) {
                    AvatarUiState.Loading -> Text("Caricamento avatar in corso...")
                    is AvatarUiState.Error -> Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )

                    is AvatarUiState.Ready -> {
                        AvatarCard(avatar = state.avatar)
                        Spacer(Modifier.height(12.dp))
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("Dati avatar aggiornati da avatar-service")

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        habitMessage = null
                        showCreateHabitDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nuova Habit")
                }

                if (showCreateHabitDialog) {
                    AlertDialog(
                        onDismissRequest = { showCreateHabitDialog = false },
                        title = { Text("Nuova Habit") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = {
                                        title = it
                                        habitMessage = null
                                    },
                                    label = { Text("Titolo") },
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
                                    label = { Text("Descrizione") },
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
                                        label = { Text("Day Of Week (es. MONDAY)") },
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
                                        habitMessage = "Avatar non disponibile"
                                        return@TextButton
                                    }

                                    if (title.isBlank()) {
                                        habitMessage = "Inserisci un titolo"
                                        return@TextButton
                                    }

                                    val normalizedDayOfWeek =
                                        if (recurrenceType == CreateHabitRecurrenceType.WEEKLY) dayOfWeek.trim().uppercase()
                                        else null
                                    val normalizedDayOfMonth =
                                        if (recurrenceType == CreateHabitRecurrenceType.MONTHLY) dayOfMonth.toIntOrNull()
                                        else null

                                    if (recurrenceType == CreateHabitRecurrenceType.WEEKLY && normalizedDayOfWeek !in allowedDaysOfWeek) {
                                        habitMessage = "Day Of Week non valido"
                                        return@TextButton
                                    }

                                    if (recurrenceType == CreateHabitRecurrenceType.MONTHLY && (normalizedDayOfMonth == null || normalizedDayOfMonth !in 1..31)) {
                                        habitMessage = "Day Of Month deve essere tra 1 e 31"
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
                                                habitMessage = "Habit creata (id: ${result.habitId})"
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
                                    Text("Crea")
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCreateHabitDialog = false }) {
                                Text("Annulla")
                            }
                        }
                    )
                }

                if (habitMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = habitMessage.orEmpty(),
                        color = if (habitMessage?.startsWith("Habit creata") == true) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }
    }
}

private val allowedDaysOfWeek = setOf(
    "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
)

