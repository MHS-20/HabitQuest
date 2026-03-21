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
public class AvatarEventConsumer extends AvatarAwareEventConsumer {

  public AvatarEventConsumer(
      UserEmailRepository userEmailRepository, NotificationService notificationService) {
    super(userEmailRepository, notificationService);
  }

  @Bean
  public Consumer<LevelUppedMessage> avatarLevelUpped() {
    return message -> {
      logger()
          .info(
              "Received LevelUpped: avatarId={}, level={}", message.avatarId(), message.newLevel());
      sendToAvatar(
          message.avatarId(),
          "Livello aumentato!",
          "Congratulazioni! Hai raggiunto il livello " + message.newLevel() + "!");
    };
  }

  @Bean
  public Consumer<DeadMessage> avatarDead() {
    return message -> {
      logger().info("Received Dead: avatarId={}", message.avatarId());
      logger().info("### CONSUMER TRIGGERED: avatarDead, avatarId={}", message.avatarId());
      sendToAvatar(
          message.avatarId(),
          "Il tuo avatar è morto!",
          "Il tuo avatar " + message.avatarId() + " è morto! Torna in gioco per rinascere.");
    };
  }

  @Bean
  public Consumer<SkillPointAssignedMessage> avatarSkillPointAssigned() {
    return message -> {
      logger()
          .info(
              "Received SkillPointAssigned: avatarId= {}, stat={}, newValue={}",
              message.avatarId(),
              message.statType(),
              message.newValue());
      sendToAvatar(
          message.avatarId(),
          "Punto abilità assegnato!",
          "Hai assegnato un punto abilità a "
              + message.statType()
              + ". Il nuovo valore è "
              + message.newValue()
              + ".");
    };
  }

  public record LevelUppedMessage(String avatarId, Integer newLevel, Instant occurredOn) {}

  public record DeadMessage(String avatarId, Instant occurredOn) {}

  public record SkillPointAssignedMessage(
      String avatarId, String statType, Integer newValue, Instant occurredOn) {}
}
