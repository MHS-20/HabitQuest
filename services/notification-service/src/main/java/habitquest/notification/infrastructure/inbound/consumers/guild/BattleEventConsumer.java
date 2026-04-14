package habitquest.notification.infrastructure.inbound.consumers.guild;

import common.hexagonal.Adapter;
import habitquest.notification.application.port.out.GuildMemberRepository;
import habitquest.notification.application.port.out.NotificationService;
import habitquest.notification.application.port.out.UserEmailRepository;
import habitquest.notification.infrastructure.inbound.consumers.base.GuildAwareEventConsumer;
import habitquest.notification.infrastructure.inbound.consumers.guild.BattleMessages.*;
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
          "Your guild is in battle!",
          "The battle \"" + message.battleId() + "\" has started for your guild. Fight on!");
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
          "Victory! Your guild won!",
          "Congratulations! Your guild won the battle \"" + message.battleId() + "\"!");
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
          "Your guild lost the battle",
          "Unfortunately your guild lost the battle \""
              + message.battleId()
              + "\". We'll get them next time!");
    };
  }
}
