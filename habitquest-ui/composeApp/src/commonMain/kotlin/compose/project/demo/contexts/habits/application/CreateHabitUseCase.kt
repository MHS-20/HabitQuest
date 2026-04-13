package compose.project.demo.contexts.habits.application

import compose.project.demo.contexts.habits.domain.contract.HabitsGateway
import compose.project.demo.contexts.habits.domain.model.CreateHabitRecurrenceType
import compose.project.demo.contexts.habits.domain.model.CreateHabitResult

class CreateHabitUseCase(private val habitsGateway: HabitsGateway) {
  suspend operator fun invoke(
    token: String,
    avatarId: String,
    title: String,
    description: String,
    recurrenceType: CreateHabitRecurrenceType,
    dayOfWeek: String?,
    dayOfMonth: Int?,
    tags: List<String>,
  ): CreateHabitResult {
    return habitsGateway.createHabit(
      token = token,
      avatarId = avatarId,
      title = title,
      description = description,
      recurrenceType = recurrenceType,
      dayOfWeek = dayOfWeek,
      dayOfMonth = dayOfMonth,
      tags = tags,
    )
  }
}
