package habitquest.notification.infrastructure.consumers.users;

import java.time.Instant;

public class UserMessages {
  public record UserRegisteredMessage(String avatarId, String email, Instant occurredOn) {}
}
