package habitquest.guild.application;

import common.ddd.Id;
import common.hexagonal.InBoundPort;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.battle.boss.BossType;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import java.util.Optional;

@InBoundPort
public interface BattleService {

  // --- Battle lifecycle ---
  Id<Battle> createBattle(Id<Guild> guildId, BossType bossType, Integer numOfTurns);

  Battle getBattleById(Id<Battle> battleId) throws BattleNotFoundException;

  void deleteBattle(Id<Battle> battleId);

  BattleOutcome markAsFallen(Id<Battle> battleId, Id<GuildMember> avatarId)
      throws BattleNotFoundException;

  // --- Query ---
  Optional<Battle> getBattleByGuild(Id<Guild> guildId) throws BattleNotFoundException;

  boolean hasBattleInProgress(Id<Guild> guildId) throws BattleNotFoundException;

  // --- Boss info ---
  Id<Guild> getGuildId(Id<Battle> battleId) throws BattleNotFoundException;

  BossEnemy getBoss(Id<Battle> battleId) throws BattleNotFoundException;

  BossStatus getBossRemainingHealth(Id<Battle> battleId) throws BattleNotFoundException;

  // --- Turn management ---
  Integer getCurrentTurn(Id<Battle> battleId) throws BattleNotFoundException;

  Integer getNumOfTurns(Id<Battle> battleId) throws BattleNotFoundException;

  void nextTurn(Id<Battle> battleId) throws BattleNotFoundException;

  void increaseNumOfTurn(Id<Battle> battleId, Id<GuildMember> memberId)
      throws BattleNotFoundException;

  void decreaseNumOfTurn(Id<Battle> battleId, Id<GuildMember> memberId)
      throws BattleNotFoundException;

  // --- Combat ---
  BattleOutcome dealDamageOnBoss(Id<Battle> battleId, Id<GuildMember> attackerId, int damage)
      throws BattleNotFoundException;

  BattleOutcome applyCounterattack(Id<Battle> battleId, Id<GuildMember> attackerId)
      throws BattleNotFoundException;

  boolean isAttackerTurn(Id<Battle> battleId, Id<GuildMember> attackerId)
      throws BattleNotFoundException;

  BattleOutcome getBattleStatus(Id<Battle> battleId) throws BattleNotFoundException;

  boolean isBattleOver(Id<Battle> battleId) throws BattleNotFoundException;

  boolean isBattleWon(Id<Battle> battleId) throws BattleNotFoundException;
}
