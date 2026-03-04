package habitquest.tracking.domain.events;

public record HabitDeleted(String habitId) implements HabitEvent {}
