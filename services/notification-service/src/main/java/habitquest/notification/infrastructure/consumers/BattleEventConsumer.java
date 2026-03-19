package habitquest.notification.infrastructure.consumers;

import common.hexagonal.Adapter;
import habitquest.notification.infrastructure.notification.NotificationService;
import java.time.Instant;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class BattleEventConsumer implements EventConsumer {

  private final NotificationService notificationService;

  public BattleEventConsumer(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Bean
  public Consumer<BattleStartedMessage> guildBattleStarted() {
    return message -> {
      logger()
          .info(
              "Received BattleStarted: battleId={}, guildId={}",
              message.battleId(),
              message.guildId());
      notificationService.send(
          "La battaglia "
              + message.battleId()
              + " è iniziata per la guild "
              + message.guildId()
              + "!");
    };
  }

  @Bean
  public Consumer<BattleWonMessage> guildBattleWon() {
    return message -> {
      logger()
          .info(
              "Received BattleWon: battleId={}, guildId={}", message.battleId(), message.guildId());
      notificationService.send(
          "La tua guild "
              + message.guildId()
              + " ha vinto la battaglia "
              + message.battleId()
              + "!");
    };
  }

  @Bean
  public Consumer<BattleLostMessage> guildBattleLost() {
    return message -> {
      logger()
          .info(
              "Received BattleLost: battleId={}, guildId={}",
              message.battleId(),
              message.guildId());
      notificationService.send(
          "La tua guild "
              + message.guildId()
              + " ha perso la battaglia "
              + message.battleId()
              + ".");
    };
  }

  public record BattleStartedMessage(String battleId, String guildId, Instant occurredOn) {}

  public record BattleWonMessage(String battleId, String guildId, Instant occurredOn) {}

  public record BattleLostMessage(String battleId, String guildId, Instant occurredOn) {}
}
