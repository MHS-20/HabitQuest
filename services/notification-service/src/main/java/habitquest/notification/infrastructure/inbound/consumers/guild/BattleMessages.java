package habitquest.notification.infrastructure.inbound.consumers.guild;

import java.time.Instant;

public class BattleMessages {
  public record BattleStartedMessage(String battleId, String guildId, Instant occurredOn) {}

  public record BattleWonMessage(String battleId, String guildId, Instant occurredOn) {}

  public record BattleLostMessage(String battleId, String guildId, Instant occurredOn) {}
}
