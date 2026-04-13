package compose.project.demo.contexts.habits.domain.contract

import compose.project.demo.contexts.habits.domain.model.AttendHabitResult
import compose.project.demo.contexts.habits.domain.model.CreateHabitRecurrenceType
import compose.project.demo.contexts.habits.domain.model.CreateHabitResult
import compose.project.demo.contexts.habits.domain.model.DeleteHabitResult
import compose.project.demo.contexts.habits.domain.model.HabitHistoryResult
import compose.project.demo.contexts.habits.domain.model.HabitListResult
import compose.project.demo.contexts.habits.domain.model.UpdateHabitResult

interface HabitsGateway {
  suspend fun createHabit(
    token: String,
    avatarId: String,
    title: String,
    description: String,
    recurrenceType: CreateHabitRecurrenceType,
    dayOfWeek: String?,
    dayOfMonth: Int?,
    tags: List<String>,
  ): CreateHabitResult

  suspend fun fetchHabitsByAvatar(token: String, avatarId: String): HabitListResult

  suspend fun attendHabit(token: String, habitId: String): AttendHabitResult

  suspend fun deleteHabit(token: String, habitId: String): DeleteHabitResult

  suspend fun updateHabit(
    token: String,
    habitId: String,
    title: String,
    description: String,
    recurrenceType: CreateHabitRecurrenceType,
    dayOfWeek: String?,
    dayOfMonth: Int?,
  ): UpdateHabitResult

  suspend fun fetchHistoryByAvatar(token: String, avatarId: String): HabitHistoryResult
}
