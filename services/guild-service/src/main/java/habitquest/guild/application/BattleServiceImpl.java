package habitquest.guild.application;

import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleStatus;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.events.battleEvents.BattleLost;
import habitquest.guild.domain.events.battleEvents.BattleObserver;
import habitquest.guild.domain.events.battleEvents.BattleStarted;
import habitquest.guild.domain.events.battleEvents.BattleWon;
import habitquest.guild.domain.factory.BattleFactory;
import java.util.List;

public class BattleServiceImpl implements BattleService {
  private final BattleRepository battleRepository;
  private final BattleObserver battleObserver;
  private final BattleFactory battleFactory;

  public BattleServiceImpl(
      BattleRepository battleRepository,
      BattleObserver battleObserver,
      BattleFactory battleFactory) {
    this.battleRepository = battleRepository;
    this.battleObserver = battleObserver;
    this.battleFactory = battleFactory;
  }

  // --- Battle lifecycle ---

  @Override
  public String createBattle(String guildId, BossEnemy boss, Integer numOfTurns) {
    Battle battle = battleFactory.create(guildId, boss, numOfTurns);
    battleRepository.save(battle);
    battleObserver.notifyBattleEvent(new BattleStarted(battle.getId()));
    return battle.getId();
  }

  @Override
  public Battle getBattleById(String battleId) {
    return battleRepository
        .findById(battleId)
        .orElseThrow(() -> new BattleNotFoundException(battleId));
  }

  @Override
  public void deleteBattle(String battleId) {
    Battle battle = getBattleById(battleId);
    battleRepository.deleteById(battleId);
  }

  // --- Query ---
  @Override
  public List<Battle> getBattlesByGuild(String guildId) {
    return battleRepository.findByGuildId(guildId);
  }

  @Override
  public boolean hasBattleInProgress(String guildId) {
    return battleRepository.findByGuildId(guildId).stream()
        .anyMatch(b -> b.getBattleStatus() == BattleStatus.ONGOING);
  }

  // --- Boss info ---
  @Override
  public String getGuildId(String battleId) {
    return getBattleById(battleId).getGuildId();
  }

  @Override
  public BossEnemy getBoss(String battleId) {
    return getBattleById(battleId).getBoss();
  }

  @Override
  public BossStatus getBossRemainingHealth(String battleId) {
    return getBattleById(battleId).getBossRemainingHealth();
  }

  // --- Turn management ---
  @Override
  public Integer getCurrentTurn(String battleId) {
    return getBattleById(battleId).getCurrentTurn();
  }

  @Override
  public Integer getNumOfTurns(String battleId) {
    return getBattleById(battleId).getNumOfTurns();
  }

  @Override
  public void nextTurn(String battleId) {
    Battle battle = getBattleById(battleId);
    battle.nextTurn();
    battleRepository.save(battle);
  }

  @Override
  public void increaseNumOfTurn(String battleId) {
    Battle battle = getBattleById(battleId);
    battle.setNumOfTurns(battle.getNumOfTurns() + 1);
    battleRepository.save(battle);
  }

  // --- Combat ---
  @Override
  public void dealDamage(String battleId, Integer damage) {
    Battle battle = getBattleById(battleId);
    battle.dealDamage(damage);
    battleRepository.save(battle);
    if (battle.getBattleStatus() == BattleStatus.WON) {
      var boss = this.getBoss(battleId);
      battleObserver.notifyBattleEvent(
          new BattleWon(battleId, boss.experienceReward(), boss.moneyReward()));
    } else {
      var boss = this.getBoss(battleId);
      battleObserver.notifyBattleEvent(new BattleLost(battleId, boss.penalty()));
    }
  }

  @Override
  public BattleStatus getBattleStatus(String battleId) {
    return getBattleById(battleId).getBattleStatus();
  }

  @Override
  public boolean isBattleOver(String battleId) {
    return getBattleById(battleId).getBattleStatus() != BattleStatus.ONGOING;
  }

  @Override
  public boolean isBattleWon(String battleId) {
    return getBattleById(battleId).getBattleStatus() == BattleStatus.WON;
  }
}
