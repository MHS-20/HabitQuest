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
public class HabitEventConsumer extends AvatarAwareEventConsumer {

  public HabitEventConsumer(
      UserEmailRepository userEmailRepository, NotificationService notificationService) {
    super(userEmailRepository, notificationService);
  }

  @Bean
  public Consumer<HabitDeletedMessage> habitDeleted() {
    return message -> {
      logger().info("Received HabitDeleted: habitId={}", message.habitId());
      sendToAvatar(
          message.avatarId(),
          "Abitudine eliminata",
          "L'abitudine \""
              + message.habitId()
              + "\" è stata eliminata. Se è stata un'abitudine importante, considera di crearne una nuova!");
    };
  }

  @Bean
  public Consumer<HabitAttendedMessage> habitAttended() {
    return message -> {
      logger()
          .info(
              "Received HabitAttended: habitId={}, avatarId={}",
              message.habitId(),
              message.avatarId());
      sendToAvatar(
          message.avatarId(),
          "Abitudine completata!",
          "Ottimo lavoro! Hai completato l'abitudine \"" + message.habitId() + "\" oggi.");
    };
  }

  @Bean
  public Consumer<HabitNotAttendedMessage> habitNotAttended() {
    return message -> {
      logger()
          .info(
              "Received HabitNotAttended: habitId={}, avatarId={}",
              message.habitId(),
              message.avatarId());
      sendToAvatar(
          message.avatarId(),
          "Abitudine non completata",
          "Hai mancato l'abitudine \"" + message.habitId() + "\" oggi. Non mollare!");
    };
  }

  public record HabitDeletedMessage(String habitId, String avatarId, Instant occurredOn) {}

  public record HabitAttendedMessage(String habitId, String avatarId, Instant occurredOn) {}

  public record HabitNotAttendedMessage(String habitId, String avatarId, Instant occurredOn) {}
}
