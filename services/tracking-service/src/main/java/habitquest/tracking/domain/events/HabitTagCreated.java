package habitquest.tracking.domain.events;

import habitquest.tracking.domain.Tag;

public record HabitTagCreated(Tag tag) implements HabitEvent {
}
