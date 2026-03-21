package habitquest.tracking.domain.events;

import habitquest.tracking.domain.Habit;

public record HabitCreated(Habit habit, String avatarId) implements HabitEvent {}
