package habitquest.guild.application;

import common.hexagonal.InBoundPort;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleStatus;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;
import java.util.List;

@InBoundPort
public interface BattleService {

  // --- Battle lifecycle ---
  String createBattle(String guildId, BossEnemy boss, Integer numOfTurns);

  Battle getBattleById(String battleId);

  void deleteBattle(String battleId);

  // --- Query ---
  List<Battle> getBattlesByGuild(String guildId);

  boolean hasBattleInProgress(String guildId);

  // --- Boss info ---
  String getGuildId(String battleId);

  BossEnemy getBoss(String battleId);

  BossStatus getBossRemainingHealth(String battleId);

  // --- Turn management ---
  Integer getCurrentTurn(String battleId);

  Integer getNumOfTurns(String battleId);

  void nextTurn(String battleId);

  void increaseNumOfTurn(String battleId);

  // --- Combat ---
  void dealDamage(String battleId, Integer damage);

  BattleStatus getBattleStatus(String battleId);

  boolean isBattleOver(String battleId);

  boolean isBattleWon(String battleId);
}
