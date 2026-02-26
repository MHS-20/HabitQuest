package habitquest.tracking.domain.events;

import habitquest.tracking.domain.Habit;

public record HabitNotAttended(Habit habit) implements HabitEvent {
}
