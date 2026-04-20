package compose.project.demo.contexts.habits.infrastructure.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import be.vandeas.kalendar.kit.CalendarEventManager
import be.vandeas.kalendar.kit.Event
import compose.project.demo.contexts.habits.domain.model.HabitCalendarEntry
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

@Composable
fun rememberHabitCalendarLauncher(): (HabitCalendarEntry) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val manager = remember(context) { CalendarEventManager().apply { setup(context) } }

    return remember(scope, manager) {
        { entry ->
            val zone = TimeZone.currentSystemDefault()
            val startInstant = entry.startDateTime.toInstant(zone)
            val endDateTime = (startInstant + entry.durationMinutes.minutes).toLocalDateTime(zone)

            scope.launch {
                manager.createEvent(
                    Event(
                        title = entry.title,
                        startDate = entry.startDateTime,
                        endDate = endDateTime,
                        notes = entry.description,
                    ),
                )
            }
        }
    }
}
