package habitquest.notification.infrastructure.inbound.consumers.users;

import common.hexagonal.Adapter;
import habitquest.notification.application.port.out.NotificationService;
import habitquest.notification.application.port.out.UserEmailRepository;
import habitquest.notification.infrastructure.inbound.consumers.base.AvatarAwareEventConsumer;
import habitquest.notification.infrastructure.inbound.consumers.users.UserMessages.*;
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
      saveEmail(message.avatarId(), message.email());
      sendNotification(
          message.email(),
          "Welcome to HabitQuest!",
          "Your account has been created successfully. Enjoy your adventure!");
    };
  }
}
