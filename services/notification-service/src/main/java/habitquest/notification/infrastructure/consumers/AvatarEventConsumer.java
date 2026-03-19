package habitquest.notification.infrastructure.consumers;

import common.hexagonal.Adapter;
import habitquest.notification.infrastructure.notification.NotificationService;
import java.time.Instant;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class AvatarEventConsumer implements EventConsumer {

  private final NotificationService notificationService;

  public AvatarEventConsumer(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Bean
  public Consumer<LevelUppedMessage> avatarLevelUpped() {
    return message -> {
      logger().info("Received LevelUpped: level={}", message.newLevel());
      notificationService.send("Hai raggiunto il livello " + message.newLevel() + "!");
    };
  }

  @Bean
  public Consumer<DeadMessage> avatarDead() {
    return message -> {
      logger().info("Received Dead: avatarId={}", message.avatarId());
      notificationService.send("Il tuo avatar " + message.avatarId() + " è morto!");
    };
  }

  @Bean
  public Consumer<SkillPointAssignedMessage> avatarSkillPointAssigned() {
    return message -> {
      logger()
          .info(
              "Received SkillPointAssigned: stat={}, newValue={}",
              message.statType(),
              message.newValue());
      notificationService.send(
          "Hai assegnato un punto a "
              + message.statType()
              + ", nuovo valore: "
              + message.newValue());
    };
  }

  public record LevelUppedMessage(Integer newLevel, Instant occurredOn) {}

  public record DeadMessage(String avatarId, Instant occurredOn) {}

  public record SkillPointAssignedMessage(String statType, Integer newValue, Instant occurredOn) {}
}
