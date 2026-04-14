package habitquest.notification.infrastructure.consumers.tracking;

import java.time.Instant;

public class HabitMessages {

  public record HabitDeletedMessage(String habitId, String avatarId, Instant occurredOn) {}

  public record HabitAttendedMessage(String habitId, String avatarId, Instant occurredOn) {}

  public record HabitNotAttendedMessage(String habitId, String avatarId, Instant occurredOn) {}
}
