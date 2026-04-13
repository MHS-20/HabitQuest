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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun HabitsScreen(token: String, avatarState: AvatarUiState, onHabitAttended: () -> Unit = {}) {
  val habitRepository = remember { HabitsApiRepository() }
  val scope = rememberCoroutineScope()
  var uiState by remember { mutableStateOf<HabitsUiState>(HabitsUiState.Loading) }
  var attendingHabitId by remember { mutableStateOf<String?>(null) }
  var deletingHabitId by remember { mutableStateOf<String?>(null) }
  var updatingHabitId by remember { mutableStateOf<String?>(null) }
  var pendingDeleteHabit by remember { mutableStateOf<HabitListItem?>(null) }
  var pendingEditHabit by remember { mutableStateOf<HabitListItem?>(null) }
  var editTitle by remember { mutableStateOf("") }
  var editDescription by remember { mutableStateOf("") }
  var actionMessage by remember { mutableStateOf<String?>(null) }
  var editRecurrenceType by remember { mutableStateOf(CreateHabitRecurrenceType.DAILY) }
  var editDayOfWeek by remember { mutableStateOf("MONDAY") }
  var editDayOfMonth by remember { mutableStateOf("1") }

  fun recurrenceTypeFrom(habit: HabitListItem): CreateHabitRecurrenceType =
    runCatching { CreateHabitRecurrenceType.valueOf(habit.recurrenceType.trim().uppercase()) }
      .getOrElse { CreateHabitRecurrenceType.DAILY }

  suspend fun loadHabits(showLoading: Boolean) {
    val avatar = (avatarState as? AvatarUiState.Ready)?.avatar
    if (showLoading) {
      uiState = HabitsUiState.Loading
    }
    uiState =
      when {
        token.isBlank() -> HabitsUiState.Error("Invalid session")
        avatar == null -> HabitsUiState.Error("Avatar not available")
        else ->
          when (val result = habitRepository.fetchHabitsByAvatar(token, avatar.id)) {
            is HabitListResult.Success -> HabitsUiState.Ready(result.habits)
            is HabitListResult.Error -> HabitsUiState.Error(result.message)
          }
      }
  }

  LaunchedEffect(token, avatarState) { loadHabits(showLoading = true) }

  Column(
    modifier =
      Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text("My habits", style = MaterialTheme.typography.headlineSmall)

    actionMessage?.let { message ->
      Text(
        text = message,
        color =
          if (
            message.startsWith("Habit completed") ||
              message.startsWith("Habit deleted") ||
              message.startsWith("Habit updated")
          ) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.error
          },
      )
    }

    when (val state = uiState) {
      HabitsUiState.Loading -> Text("Loading habits...")
      is HabitsUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
      is HabitsUiState.Ready -> {
        if (state.habits.isEmpty()) {
          Text("No habits found")
        } else {
          LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.habits, key = { it.id }) { habit ->
              HabitRow(
                habit = habit,
                isAttending = attendingHabitId == habit.id,
                isDeleting = deletingHabitId == habit.id,
                isUpdating = updatingHabitId == habit.id,
                onAttend = {
                  scope.launch {
                    attendingHabitId = habit.id
                    actionMessage = null
                    when (val result = habitRepository.attendHabit(token, habit.id)) {
                      AttendHabitResult.Success -> {
                        actionMessage = "Habit completed: ${habit.title}"
                        onHabitAttended()
                        loadHabits(showLoading = false)
                      }

                      is AttendHabitResult.Error -> {
                        actionMessage = result.message
                      }
                    }
                    attendingHabitId = null
                  }
                },
                onEdit = {
                  pendingEditHabit = habit
                  editTitle = habit.title
                  editDescription = habit.description
                  editRecurrenceType = recurrenceTypeFrom(habit)
                  editDayOfWeek = habit.recurrenceDayOfWeek ?: "MONDAY"
                  editDayOfMonth = (habit.recurrenceDayOfMonth ?: 1).toString()
                },
                onDelete = { pendingDeleteHabit = habit },
              )
            }
          }
        }
      }
    }

    pendingDeleteHabit?.let { habit ->
      AlertDialog(
        onDismissRequest = { pendingDeleteHabit = null },
        title = { Text("Delete habit?") },
        text = { Text("Do you want to delete \"${habit.title}\"? This action cannot be undone.") },
        confirmButton = {
          TextButton(
            onClick = {
              scope.launch {
                deletingHabitId = habit.id
                actionMessage = null
                when (val result = habitRepository.deleteHabit(token, habit.id)) {
                  DeleteHabitResult.Success -> {
                    actionMessage = "Habit deleted: ${habit.title}"
                    pendingDeleteHabit = null
                    loadHabits(showLoading = false)
                  }

                  is DeleteHabitResult.Error -> {
                    actionMessage = result.message
                  }
                }
                deletingHabitId = null
              }
            }
          ) {
            Text("Delete")
          }
        },
        dismissButton = { TextButton(onClick = { pendingDeleteHabit = null }) { Text("Cancel") } },
      )
    }

    pendingEditHabit?.let { habit ->
      AlertDialog(
        onDismissRequest = {
          if (updatingHabitId == null) {
            pendingEditHabit = null
          }
        },
        title = { Text("Edit habit") },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = editTitle,
              onValueChange = { editTitle = it },
              label = { Text("Title") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
              value = editDescription,
              onValueChange = { editDescription = it },
              label = { Text("Description") },
              modifier = Modifier.fillMaxWidth(),
            )
            Row(modifier = Modifier.fillMaxWidth()) {
              OutlinedButton(
                onClick = { editRecurrenceType = CreateHabitRecurrenceType.DAILY },
                modifier = Modifier.weight(1f),
              ) {
                Text("Daily")
              }
              Spacer(Modifier.width(8.dp))
              OutlinedButton(
                onClick = { editRecurrenceType = CreateHabitRecurrenceType.WEEKLY },
                modifier = Modifier.weight(1f),
              ) {
                Text("Weekly")
              }
              Spacer(Modifier.width(8.dp))
              OutlinedButton(
                onClick = { editRecurrenceType = CreateHabitRecurrenceType.MONTHLY },
                modifier = Modifier.weight(1f),
              ) {
                Text("Monthly")
              }
            }
            Text("Recurrence: ${editRecurrenceType.name}")
            if (editRecurrenceType == CreateHabitRecurrenceType.WEEKLY) {
              OutlinedTextField(
                value = editDayOfWeek,
                onValueChange = { editDayOfWeek = it.uppercase() },
                label = { Text("Day Of Week (e.g. MONDAY)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
              )
            }
            if (editRecurrenceType == CreateHabitRecurrenceType.MONTHLY) {
              OutlinedTextField(
                value = editDayOfMonth,
                onValueChange = { editDayOfMonth = it },
                label = { Text("Day Of Month (1-31)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }
        },
        confirmButton = {
          TextButton(
            enabled = updatingHabitId == null && editTitle.isNotBlank(),
            onClick = {
              val normalizedDayOfWeek =
                if (editRecurrenceType == CreateHabitRecurrenceType.WEEKLY)
                  editDayOfWeek.trim().uppercase()
                else null
              val normalizedDayOfMonth =
                if (editRecurrenceType == CreateHabitRecurrenceType.MONTHLY)
                  editDayOfMonth.toIntOrNull()
                else null

              if (
                editRecurrenceType == CreateHabitRecurrenceType.WEEKLY &&
                  normalizedDayOfWeek !in allowedDaysOfWeek
              ) {
                actionMessage = "Invalid Day Of Week"
                return@TextButton
              }

              if (
                editRecurrenceType == CreateHabitRecurrenceType.MONTHLY &&
                  (normalizedDayOfMonth == null || normalizedDayOfMonth !in 1..31)
              ) {
                actionMessage = "Day Of Month must be between 1 and 31"
                return@TextButton
              }

              scope.launch {
                updatingHabitId = habit.id
                actionMessage = null
                when (
                  val result =
                    habitRepository.updateHabit(
                      token = token,
                      habitId = habit.id,
                      title = editTitle.trim(),
                      description = editDescription.trim(),
                      recurrenceType = editRecurrenceType,
                      dayOfWeek = normalizedDayOfWeek,
                      dayOfMonth = normalizedDayOfMonth,
                    )
                ) {
                  UpdateHabitResult.Success -> {
                    actionMessage = "Habit updated: ${editTitle.trim()}"
                    pendingEditHabit = null
                    loadHabits(showLoading = false)
                  }

                  is UpdateHabitResult.Error -> {
                    actionMessage = result.message
                  }
                }
                updatingHabitId = null
              }
            },
          ) {
            Text(if (updatingHabitId == habit.id) "Saving..." else "Save")
          }
        },
        dismissButton = {
          TextButton(enabled = updatingHabitId == null, onClick = { pendingEditHabit = null }) {
            Text("Cancel")
          }
        },
      )
    }
  }
}

@Composable
private fun HabitRow(
  habit: HabitListItem,
  isAttending: Boolean,
  isDeleting: Boolean,
  isUpdating: Boolean,
  onAttend: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  val tagsText = habit.tags.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "No tags"
  val lastAttendedText = habit.lastAttendedDate ?: "Not available"
  val nextRecurrenceText = habit.nextRecurrenceDate ?: "Not available"

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(habit.title, style = MaterialTheme.typography.titleMedium)
      habit.associatedQuestId?.let { questId ->
        Spacer(Modifier.height(4.dp))
        Row {
          Text(
            text = "From quest",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier =
              Modifier.background(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = MaterialTheme.shapes.small,
                )
                .padding(horizontal = 8.dp, vertical = 2.dp),
          )
          Spacer(Modifier.width(6.dp))
          Text(
            text = "($questId)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
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
        enabled = !isAttending && !isDeleting && !isUpdating,
        modifier = Modifier.fillMaxWidth(),
      ) {
        if (isAttending) {
          CircularProgressIndicator(strokeWidth = 2.dp)
          Spacer(Modifier.width(8.dp))
          Text("Marking...")
        } else {
          Text("Mark as completed")
        }
      }
      Spacer(Modifier.height(8.dp))
      Button(
        onClick = onEdit,
        enabled = !isAttending && !isDeleting && !isUpdating,
        modifier = Modifier.fillMaxWidth(),
      ) {
        if (isUpdating) {
          CircularProgressIndicator(strokeWidth = 2.dp)
          Spacer(Modifier.width(8.dp))
          Text("Saving...")
        } else {
          Text("Edit")
        }
      }
      Spacer(Modifier.height(8.dp))
      Button(
        onClick = onDelete,
        enabled = !isAttending && !isDeleting && !isUpdating,
        modifier = Modifier.fillMaxWidth(),
        colors =
          androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
          ),
      ) {
        if (isDeleting) {
          CircularProgressIndicator(strokeWidth = 2.dp)
          Spacer(Modifier.width(8.dp))
          Text("Deleting...")
        } else {
          Text("Delete")
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

private val allowedDaysOfWeek =
  setOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
