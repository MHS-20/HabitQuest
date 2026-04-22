package compose.project.demo.contexts.habits.infrastructure.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberHabitCalendarLauncher(): HabitCalendarLauncher =
    remember {
        {
            HabitCalendarLaunchResult.Failure("Calendar integration is unavailable on web")
        }
    }
