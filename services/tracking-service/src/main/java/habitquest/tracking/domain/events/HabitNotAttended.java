package habitquest.tracking.domain.events;

import common.ddd.Id;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;

public record HabitNotAttended(Habit habit, Id<Avatar> avatarId) implements HabitEvent {}
