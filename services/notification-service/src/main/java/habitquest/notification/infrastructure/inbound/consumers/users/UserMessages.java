package habitquest.notification.infrastructure.inbound.consumers.users;

import java.time.Instant;

public class UserMessages {
  public record UserRegisteredMessage(String avatarId, String email, Instant occurredOn) {}
}
