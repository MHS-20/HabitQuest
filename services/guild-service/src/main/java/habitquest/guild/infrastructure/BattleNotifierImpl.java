package habitquest.guild.infrastructure;

import common.hexagonal.Adapter;
import habitquest.guild.application.BattleNotifier;
import habitquest.guild.domain.events.battleEvents.BattleLost;
import habitquest.guild.domain.events.battleEvents.BattleStarted;
import habitquest.guild.domain.events.battleEvents.BattleWon;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class BattleNotifierImpl implements BattleNotifier {

  private static final Logger LOG = LoggerFactory.getLogger(BattleNotifierImpl.class);

  static final String BATTLE_STARTED_BINDING = "guild-battle-started-out-0";
  static final String BATTLE_WON_BINDING = "guild-battle-won-out-0";
  static final String BATTLE_LOST_BINDING = "guild-battle-lost-out-0";

  private final StreamBridge streamBridge;

  public BattleNotifierImpl(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Override
  public void notifyBattleStarted(BattleStarted event) {
    BattleStartedMessage message =
        new BattleStartedMessage(event.battleId(), event.guildId(), Instant.now());

    LOG.info(
        "Publishing BattleStarted event: battleId={}, guildId={}",
        message.battleId(),
        message.guildId());
    boolean sent = streamBridge.send(BATTLE_STARTED_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish BattleStarted event for battleId {}", message.battleId());
    }
  }

  @Override
  public void notifyBattleWon(BattleWon event) {
    BattleWonMessage message =
        new BattleWonMessage(event.battleId(), event.guildId(), Instant.now());

    LOG.info(
        "Publishing BattleWon event: battleId={}, guildId={}",
        message.battleId(),
        message.guildId());
    boolean sent = streamBridge.send(BATTLE_WON_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish BattleWon event for battleId {}", message.battleId());
    }
  }

  @Override
  public void notifyBattleLost(BattleLost event) {
    BattleLostMessage message =
        new BattleLostMessage(event.battleId(), event.guildId(), Instant.now());

    LOG.info(
        "Publishing BattleLost event: battleId={}, guildId={}",
        message.battleId(),
        message.guildId());
    boolean sent = streamBridge.send(BATTLE_LOST_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish BattleLost event for battleId {}", message.battleId());
    }
  }

  public record BattleStartedMessage(String battleId, String guildId, Instant occurredOn) {}

  public record BattleWonMessage(String battleId, String guildId, Instant occurredOn) {}

  public record BattleLostMessage(String battleId, String guildId, Instant occurredOn) {}
}
