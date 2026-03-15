package habitquest.guild.application;

import common.hexagonal.InBoundPort;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.battle.boss.BossType;
import java.util.Optional;

@InBoundPort
public interface BattleService {

  // --- Battle lifecycle ---
  String createBattle(String guildId, BossType bossType, Integer numOfTurns);

  Battle getBattleById(String battleId) throws BattleNotFoundException;

  void deleteBattle(String battleId);

  BattleOutcome markAsFallen(String battleId, String avatarId) throws BattleNotFoundException;

  // --- Query ---
  Optional<Battle> getBattleByGuild(String guildId) throws BattleNotFoundException;

  boolean hasBattleInProgress(String guildId) throws BattleNotFoundException;

  // --- Boss info ---
  String getGuildId(String battleId) throws BattleNotFoundException;

  BossEnemy getBoss(String battleId) throws BattleNotFoundException;

  BossStatus getBossRemainingHealth(String battleId) throws BattleNotFoundException;

  // --- Turn management ---
  Integer getCurrentTurn(String battleId) throws BattleNotFoundException;

  Integer getNumOfTurns(String battleId) throws BattleNotFoundException;

  void nextTurn(String battleId) throws BattleNotFoundException;

  void increaseNumOfTurn(String battleId, String memberId) throws BattleNotFoundException;

  void decreaseNumOfTurn(String battleId, String memberId) throws BattleNotFoundException;

  // --- Combat ---
  //  BattleOutcome dealDamage(String battleId, String attackerId, int damage, boolean attackerDied)
  // throws BattleNotFoundException;

  BattleOutcome dealDamageOnBoss(String battleId, String attackerId, int damage)
      throws BattleNotFoundException;

  BattleOutcome applyCounterattack(String battleId, String attackerId)
      throws BattleNotFoundException;

  boolean isAttackerTurn(String battleId, String attackerId) throws BattleNotFoundException;

  BattleOutcome getBattleStatus(String battleId) throws BattleNotFoundException;

  boolean isBattleOver(String battleId) throws BattleNotFoundException;

  boolean isBattleWon(String battleId) throws BattleNotFoundException;
}
