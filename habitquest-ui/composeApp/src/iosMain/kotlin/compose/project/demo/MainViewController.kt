package compose.project.demo

import androidx.compose.ui.window.ComposeUIViewController
import compose.project.demo.contexts.habits.infrastructure.calendar.rememberHabitCalendarLauncher

fun MainViewController() =
    ComposeUIViewController {
        App(habitCalendarLauncher = rememberHabitCalendarLauncher())
    }
