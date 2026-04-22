package habitquest.guild.infrastructure.dto;

import common.cqrs.Command;
import common.cqrs.CommandResponse;

public class BattleCommands {
  public record CreateBattleCommand(String guildId, String bossType, String requesterId)
      implements Command {}

  public record DeleteBattleCommand(String guildId, String requesterId) implements Command {}

  public record TakeDamageCommand(Integer damage, String attackerAvatarId) implements Command {}

  // Responses produced by command endpoints
  public record BattleCreatedResponse(String id) implements CommandResponse {}

  public record BattleOutcomeLog(String battleId, String outcome, int primary, int secondary)
      implements CommandResponse {}

  public record ErrorResponse(String message) implements CommandResponse {}
}
