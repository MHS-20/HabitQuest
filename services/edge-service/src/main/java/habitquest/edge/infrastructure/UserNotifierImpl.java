package habitquest.edge.infrastructure;

import common.hexagonal.Adapter;
import habitquest.edge.application.EdgeLogger;
import habitquest.edge.application.UserNotifier;
import habitquest.edge.domain.User;
import java.time.Instant;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class UserNotifierImpl implements UserNotifier {

  static final String USER_REGISTERED_BINDING = "user.registered";

  private final StreamBridge streamBridge;
  private final EdgeLogger log;

  public UserNotifierImpl(StreamBridge streamBridge, EdgeLogger log) {
    this.streamBridge = streamBridge;
    this.log = log;
  }

  @Override
  public void notifyUserRegistered(User user) {
    UserRegisteredMessage message =
        new UserRegisteredMessage(user.getId().value(), user.getEmail(), Instant.now());
    log.info(message, "Publishing UserRegistered event");
    boolean sent = streamBridge.send(USER_REGISTERED_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish UserRegistered event", null);
    }
  }

  public record UserRegisteredMessage(String avatarId, String email, Instant occurredOn) {}
}
