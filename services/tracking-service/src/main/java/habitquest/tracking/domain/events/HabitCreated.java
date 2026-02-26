package habitquest.tracking.domain.events;

import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.reminder.Recurrence;

public record HabitCreated(Habit habit) implements HabitEvent {
}
