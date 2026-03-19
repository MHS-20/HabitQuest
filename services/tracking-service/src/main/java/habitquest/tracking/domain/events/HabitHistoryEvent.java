package habitquest.tracking.domain.events;

import java.time.LocalDateTime;
import java.util.Objects;

public record HabitHistoryEvent(HabitEvent event, LocalDateTime occurredAt, String details) {

  public HabitHistoryEvent {
    Objects.requireNonNull(event);
    Objects.requireNonNull(occurredAt);
    details = details == null ? "" : details;
  }

  public String habitId() {
    return switch (event) {
      case HabitCreated e -> e.habit().getId();
      case HabitUpdated e -> e.habit().getId();
      case HabitAttended e -> e.habit().getId();
      case HabitNotAttended e -> e.habit().getId();
      case HabitDeleted e -> e.habitId();
      default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    };
  }
}
