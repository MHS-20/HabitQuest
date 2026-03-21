package habitquest.notification.infrastructure.consumers;

import common.hexagonal.Adapter;
import habitquest.notification.infrastructure.notification.NotificationService;
import habitquest.notification.infrastructure.repository.UserEmailRepository;
import java.time.Instant;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class UserEventConsumer implements EventConsumer {

  private final UserEmailRepository userEmailRepository;
  private final NotificationService notificationService;

  public UserEventConsumer(
      UserEmailRepository userEmailRepository, NotificationService notificationService) {
    this.userEmailRepository = userEmailRepository;
    this.notificationService = notificationService;
  }

  @Bean
  public Consumer<UserRegisteredMessage> userRegistered() {
    return message -> {
      logger()
          .info("Received UserRegistered: userId={}, email={}", message.userId(), message.email());
      userEmailRepository.save(message.userId(), message.email());
      notificationService.send("Welcome! Your account has been successfully created.");
    };
  }

  public record UserRegisteredMessage(String userId, String email, Instant occurredOn) {}
}
