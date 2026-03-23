package habitquest.edge.infrastructure;

import common.hexagonal.Adapter;
import habitquest.edge.application.UserNotifier;
import habitquest.edge.domain.User;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class UserNotifierImpl implements UserNotifier {

  private static final Logger LOG = LoggerFactory.getLogger(UserNotifierImpl.class);

  static final String USER_REGISTERED_BINDING = "user.registered";

  private final StreamBridge streamBridge;

  public UserNotifierImpl(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Override
  public void notifyUserRegistered(User user) {
    UserRegisteredMessage message =
        new UserRegisteredMessage(user.getId().value(), user.getEmail(), Instant.now());

    LOG.info(
        "Publishing UserRegistered event: userId={}, email={}",
        message.avatarId(),
        message.email());
    boolean sent = streamBridge.send(USER_REGISTERED_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish UserRegistered event for userId {}", message.avatarId());
    }
  }

  public record UserRegisteredMessage(String avatarId, String email, Instant occurredOn) {}
}
