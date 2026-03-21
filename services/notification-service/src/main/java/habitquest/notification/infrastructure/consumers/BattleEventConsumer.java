package habitquest.notification.infrastructure.consumers;

import common.hexagonal.Adapter;
import habitquest.notification.infrastructure.notification.NotificationService;
import habitquest.notification.infrastructure.repository.GuildMemberRepository;
import habitquest.notification.infrastructure.repository.UserEmailRepository;
import java.time.Instant;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class BattleEventConsumer extends GuildAwareEventConsumer {

  public BattleEventConsumer(
      UserEmailRepository userEmailRepository,
      GuildMemberRepository guildMemberRepository,
      NotificationService notificationService) {
    super(userEmailRepository, guildMemberRepository, notificationService);
  }

  @Bean
  public Consumer<BattleStartedMessage> guildBattleStarted() {
    return message -> {
      logger()
          .info(
              "Received BattleStarted: battleId={}, guildId={}",
              message.battleId(),
              message.guildId());
      sendToGuild(
          message.guildId(),
          "La tua guild è in battaglia!",
          "La battaglia \"" + message.battleId() + "\" è iniziata per la tua guild. Forza!");
    };
  }

  @Bean
  public Consumer<BattleWonMessage> guildBattleWon() {
    return message -> {
      logger()
          .info(
              "Received BattleWon: battleId={}, guildId={}", message.battleId(), message.guildId());
      sendToGuild(
          message.guildId(),
          "Vittoria! La tua guild ha vinto!",
          "Congratulazioni! La vostra guild ha vinto la battaglia \"" + message.battleId() + "\"!");
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
      sendToGuild(
          message.guildId(),
          "La tua guild ha perso la battaglia",
          "Purtroppo la vostra guild ha perso la battaglia \""
              + message.battleId()
              + "\". Ci rifaremo!");
    };
  }

  public record BattleStartedMessage(String battleId, String guildId, Instant occurredOn) {}

  public record BattleWonMessage(String battleId, String guildId, Instant occurredOn) {}

  public record BattleLostMessage(String battleId, String guildId, Instant occurredOn) {}
}
