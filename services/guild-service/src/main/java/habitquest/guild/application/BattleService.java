package habitquest.guild.application;

import common.hexagonal.InBoundPort;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleStatus;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;

@InBoundPort
public interface BattleService {

  // --- Battle lifecycle ---
  String createBattle(String guildId, BossEnemy boss, Integer numOfTurns);

  Battle getBattleById(String battleId) throws BattleNotFoundException;

  void deleteBattle(String battleId);

  // --- Query ---
  Battle getBattleByGuild(String guildId) throws BattleNotFoundException;

  boolean hasBattleInProgress(String guildId) throws BattleNotFoundException;

  // --- Boss info ---
  String getGuildId(String battleId) throws BattleNotFoundException;

  BossEnemy getBoss(String battleId) throws BattleNotFoundException;

  BossStatus getBossRemainingHealth(String battleId) throws BattleNotFoundException;

  // --- Turn management ---
  Integer getCurrentTurn(String battleId) throws BattleNotFoundException;

  Integer getNumOfTurns(String battleId) throws BattleNotFoundException;

  void nextTurn(String battleId) throws BattleNotFoundException;

  void increaseNumOfTurn(String battleId) throws BattleNotFoundException;

  // --- Combat ---
  void dealDamage(String battleId, Integer damage) throws BattleNotFoundException;

  BattleStatus getBattleStatus(String battleId) throws BattleNotFoundException;

  boolean isBattleOver(String battleId) throws BattleNotFoundException;

  boolean isBattleWon(String battleId) throws BattleNotFoundException;
}
