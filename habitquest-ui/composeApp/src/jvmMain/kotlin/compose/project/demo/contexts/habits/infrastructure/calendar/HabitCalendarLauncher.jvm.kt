package compose.project.demo.contexts.habits.infrastructure.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import compose.project.demo.contexts.habits.domain.model.HabitCalendarEntry

@Composable
fun rememberHabitCalendarLauncher(): (HabitCalendarEntry) -> Unit {
  return remember { { _: HabitCalendarEntry -> } }
}

