package habitquest.tracking.domain.events;

import habitquest.tracking.domain.Habit;

public record HabitAttended(Habit habit) implements HabitEvent {
}
