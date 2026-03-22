package habitquest.tracking.domain.events;

import common.ddd.Id;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;

public record HabitDeleted(Id<Habit> habitId, Id<Avatar> avatarId) implements HabitEvent {}
