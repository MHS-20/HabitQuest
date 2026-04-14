package habitquest.notification.infrastructure.inbound.consumers.avatar;

import common.hexagonal.Adapter;
import habitquest.notification.application.port.out.NotificationService;
import habitquest.notification.application.port.out.UserEmailRepository;
import habitquest.notification.infrastructure.inbound.consumers.avatar.AvatarMessages.*;
import habitquest.notification.infrastructure.inbound.consumers.base.AvatarAwareEventConsumer;
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
          "Level up!",
          "Congratulations! You've reached level " + message.newLevel() + "!");
    };
  }

  @Bean
  public Consumer<DeadMessage> avatarDead() {
    return message -> {
      logger().info("Received Dead: avatarId={}", message.avatarId());
      logger().info("### CONSUMER TRIGGERED: avatarDead, avatarId={}", message.avatarId());
      sendToAvatar(
          message.avatarId(),
          "Your avatar has died!",
          "Your avatar " + message.avatarId() + " has died! Come back to the game to resurrect.");
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
          "Skill point assigned!",
          "You assigned a skill point to "
              + message.statType()
              + ". The new value is "
              + message.newValue()
              + ".");
    };
  }

  @Bean
  public Consumer<NewSpellLearnedMessage> avatarNewSpellLearned() {
    return message -> {
      logger()
          .info(
              "Received NewSpellLearned: avatarId={}, spell={}",
              message.avatarId(),
              message.spellName());
      sendToAvatar(
          message.avatarId(),
          "New spell learned!",
          "You learned a new spell: " + message.spellName() + "! " + message.description());
    };
  }
}
