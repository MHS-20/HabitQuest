package habitquest.guild.application.port.out;

import common.hexagonal.OutBoundPort;
import habitquest.guild.domain.battle.DamageResult;
import java.time.Instant;

@OutBoundPort
public interface AvatarClientPort {
  DamageResult applyDamage(String avatarId, int amount);

  void applyPenalty(String avatarId, int penaltyAmount);

  void grantExperience(String avatarId, int amount);

  void earnMoney(String avatarId, int amount);

  void sendInviteToAvatar(
      String inviteId, String avatarId, String guildId, String guildName, Instant expiresAt);
}
