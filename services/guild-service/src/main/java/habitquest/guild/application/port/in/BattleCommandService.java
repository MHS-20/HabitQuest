package habitquest.guild.application.port.in;

import common.ddd.Id;
import common.hexagonal.InBoundPort;
import habitquest.guild.application.exceptions.BattleNotFoundException;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossType;
import habitquest.guild.domain.guild.GuildMember;
import java.util.List;

@InBoundPort
public interface BattleCommandService {

  // --- Battle lifecycle (commands) ---
  Id<Battle> createBattle(
      Id<habitquest.guild.domain.guild.Guild> guildId,
      BossType bossType,
      Integer numOfTurns,
      List<Id<GuildMember>> members);

  void deleteBattle(Id<Battle> battleId);

  BattleOutcome markAsFallen(Id<Battle> battleId, Id<GuildMember> avatarId)
      throws BattleNotFoundException;

  // --- Turn management (commands) ---
  void nextTurn(Id<Battle> battleId) throws BattleNotFoundException;

  void increaseNumOfTurn(Id<Battle> battleId, Id<GuildMember> memberId)
      throws BattleNotFoundException;

  void decreaseNumOfTurn(Id<Battle> battleId, Id<GuildMember> memberId)
      throws BattleNotFoundException;

  // --- Combat (commands) ---
  BattleOutcome processDamage(Id<Battle> battleId, Id<GuildMember> attackerId, int damage)
      throws BattleNotFoundException;

  BattleOutcome dealDamageOnBoss(Id<Battle> battleId, Id<GuildMember> attackerId, int damage)
      throws BattleNotFoundException;

  BattleOutcome applyCounterattack(Id<Battle> battleId, Id<GuildMember> attackerId)
      throws BattleNotFoundException;
}
