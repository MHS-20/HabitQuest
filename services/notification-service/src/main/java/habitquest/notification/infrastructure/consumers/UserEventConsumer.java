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
public class UserEventConsumer extends AvatarAwareEventConsumer {

  public UserEventConsumer(
      UserEmailRepository userEmailRepository, NotificationService notificationService) {
    super(userEmailRepository, notificationService);
  }

  @Bean
  public Consumer<UserRegisteredMessage> userRegistered() {
    return message -> {
      logger()
          .info(
              "Received UserRegistered: avatarId={}, email={}",
              message.avatarId(),
              message.email());
      super.getUserEmailRepository().save(message.avatarId(), message.email());
      super.getNotificationService()
          .send(
              message.email(),
              "Benvenuto su HabitQuest!",
              "Il tuo account è stato creato con successo. Buona avventura!");
    };
  }

  public record UserRegisteredMessage(String avatarId, String email, Instant occurredOn) {}
}
