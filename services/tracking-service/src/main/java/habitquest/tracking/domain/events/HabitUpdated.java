package habitquest.tracking.domain.events;

import habitquest.tracking.domain.Habit;

public record HabitUpdated(Habit habit) implements HabitEvent {
}
