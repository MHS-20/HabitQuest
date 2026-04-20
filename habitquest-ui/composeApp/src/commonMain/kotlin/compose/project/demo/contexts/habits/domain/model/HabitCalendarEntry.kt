package compose.project.demo.contexts.habits.domain.model

import kotlinx.datetime.LocalDateTime

data class HabitCalendarEntry(
    val title: String,
    val description: String,
    val startDateTime: LocalDateTime,
    val durationMinutes: Int = 30,
)
