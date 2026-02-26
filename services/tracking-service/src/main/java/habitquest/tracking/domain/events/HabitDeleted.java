package habitquest.tracking.domain.events;

import habitquest.tracking.domain.Habit;

public record HabitDeleted(Habit habit) implements HabitEvent {}
