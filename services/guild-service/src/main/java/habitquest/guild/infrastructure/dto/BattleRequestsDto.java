package habitquest.guild.infrastructure.dto;

public class BattleRequestsDto {
  public record CreateBattleRequest(String guildId, String bossType, String requesterId) {}

  public record DeleteBattleRequest(String guildId, String requesterId) {}

  public record DamageRequest(Integer damage, String attackerAvatarId) {}
}
