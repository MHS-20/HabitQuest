package compose.project.demo.contexts.habits.infrastructure.calendar

import compose.project.demo.contexts.habits.domain.model.HabitCalendarEntry

sealed interface HabitCalendarLaunchResult {
    data object Success : HabitCalendarLaunchResult

    data class Failure(
        val message: String,
    ) : HabitCalendarLaunchResult
}

typealias HabitCalendarLauncher = suspend (HabitCalendarEntry) -> HabitCalendarLaunchResult

fun defaultHabitCalendarLauncher(): HabitCalendarLauncher =
    {
        HabitCalendarLaunchResult.Failure("Calendar integration is not available on this platform")
    }
