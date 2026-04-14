package habitquest.guild.infrastructure.outbound;

import common.hexagonal.Adapter;
import habitquest.guild.application.port.out.BattleNotifier;
import habitquest.guild.application.port.out.GuildLogger;
import habitquest.guild.domain.events.battleEvents.BattleLost;
import habitquest.guild.domain.events.battleEvents.BattleStarted;
import habitquest.guild.domain.events.battleEvents.BattleWon;
import java.time.Instant;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class BattleNotifierImpl implements BattleNotifier {

  static final String BATTLE_STARTED_BINDING = "guild.battle-started";
  static final String BATTLE_WON_BINDING = "guild.battle-won";
  static final String BATTLE_LOST_BINDING = "guild.battle-lost";

  private final StreamBridge streamBridge;
  private final GuildLogger log;

  public BattleNotifierImpl(StreamBridge streamBridge, GuildLogger log) {
    this.streamBridge = streamBridge;
    this.log = log;
  }

  @Override
  public void notifyBattleStarted(BattleStarted event) {
    BattleStartedMessage message =
        new BattleStartedMessage(event.battleId().value(), event.guildId().value(), Instant.now());
    log.info(message, "Publishing BattleStarted event");
    boolean sent = streamBridge.send(BATTLE_STARTED_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish BattleStarted event", null);
    }
  }

  @Override
  public void notifyBattleWon(BattleWon event) {
    BattleWonMessage message =
        new BattleWonMessage(event.battleId().value(), event.guildId().value(), Instant.now());
    log.info(message, "Publishing BattleWon event");
    boolean sent = streamBridge.send(BATTLE_WON_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish BattleWon event", null);
    }
  }

  @Override
  public void notifyBattleLost(BattleLost event) {
    BattleLostMessage message =
        new BattleLostMessage(event.battleId().value(), event.guildId().value(), Instant.now());
    log.info(message, "Publishing BattleLost event");
    boolean sent = streamBridge.send(BATTLE_LOST_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish BattleLost event", null);
    }
  }

  public record BattleStartedMessage(String battleId, String guildId, Instant occurredOn) {}

  public record BattleWonMessage(String battleId, String guildId, Instant occurredOn) {}

  public record BattleLostMessage(String battleId, String guildId, Instant occurredOn) {}
}
