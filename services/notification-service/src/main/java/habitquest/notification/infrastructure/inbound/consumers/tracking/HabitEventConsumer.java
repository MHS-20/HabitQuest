package habitquest.notification.infrastructure.inbound.consumers.tracking;

import common.hexagonal.Adapter;
import habitquest.notification.application.port.out.NotificationService;
import habitquest.notification.application.port.out.UserEmailRepository;
import habitquest.notification.infrastructure.inbound.consumers.base.AvatarAwareEventConsumer;
import habitquest.notification.infrastructure.inbound.consumers.tracking.HabitMessages.*;
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
          "Habit removed",
          "The habit \""
              + message.habitId()
              + "\" has been removed. If it was important, consider creating a new one!");
    };
  }

  @Bean
  public Consumer<HabitAttendedMessage> habitAttended() {
    return message -> {
      logger()
          .info(
              "Received HabitAttended: habitId={}, avatarId= {}",
              message.habitId(),
              message.avatarId());
      sendToAvatar(
          message.avatarId(),
          "Habit completed!",
          "Great job! You completed the habit \"" + message.habitId() + "\" today.");
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
          "Habit missed",
          "You missed the habit \"" + message.habitId() + "\" today. Don't give up!");
    };
  }
}
