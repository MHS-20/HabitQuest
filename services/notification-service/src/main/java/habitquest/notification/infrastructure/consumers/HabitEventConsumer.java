package habitquest.notification.infrastructure.consumers;

import common.hexagonal.Adapter;
import habitquest.notification.infrastructure.EventConsumer;
import habitquest.notification.infrastructure.NotificationService;
import java.time.Instant;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class HabitEventConsumer implements EventConsumer {

  private final NotificationService notificationService;

  public HabitEventConsumer(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Bean
  public Consumer<HabitDeletedMessage> habitDeleted() {
    return message -> {
      logger().info("Received HabitDeleted: habitId={}", message.habitId());
      notificationService.send("L'abitudine " + message.habitId() + " è stata eliminata.");
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
      notificationService.send("Hai completato l'abitudine " + message.habitId() + " oggi!");
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
      notificationService.send("Hai mancato l'abitudine " + message.habitId() + " oggi.");
    };
  }

  public record HabitDeletedMessage(String habitId, Instant occurredOn) {}

  public record HabitAttendedMessage(String habitId, String avatarId, Instant occurredOn) {}

  public record HabitNotAttendedMessage(String habitId, String avatarId, Instant occurredOn) {}
}
