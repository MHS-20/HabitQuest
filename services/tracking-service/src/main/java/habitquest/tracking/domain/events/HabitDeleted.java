package habitquest.tracking.domain.events;

public record HabitDeleted(String habitId, String avatarId) implements HabitEvent {}
