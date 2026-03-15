package habitquest.guild.application;

import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.battle.boss.BossType;
import habitquest.guild.domain.events.battleEvents.BattleLost;
import habitquest.guild.domain.events.battleEvents.BattleObserver;
import habitquest.guild.domain.events.battleEvents.BattleStarted;
import habitquest.guild.domain.events.battleEvents.BattleWon;
import habitquest.guild.domain.factory.BattleFactory;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
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
  public String createBattle(String guildId, BossType bossType, Integer numOfTurns) {
    Battle battle = battleFactory.create(guildId, bossType, numOfTurns);
    battleRepository.save(battle);
    battleObserver.notifyBattleEvent(new BattleStarted(battle.getId(), battle.getGuildId()));
    return battle.getId();
  }

  @Override
  public void deleteBattle(String battleId) {
    battleRepository.deleteById(battleId);
  }

  @Override
  public Battle getBattleById(String battleId) throws BattleNotFoundException {
    return battleRepository
        .findById(battleId)
        .orElseThrow(() -> new BattleNotFoundException(battleId));
  }

  public BattleOutcome markAsFallen(String battleId, String avatarId)
      throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    battle.markAsFallen(avatarId);
    battleRepository.save(battle);
    if (battle.getBattleStatus().isLost()) {
      var boss = this.getBoss(battleId);
      battleObserver.notifyBattleEvent(
          new BattleLost(battleId, battle.getGuildId(), boss.penalty().amount()));
    }
    return battle.getBattleStatus();
  }

  // --- Query ---
  @Override
  public Optional<Battle> getBattleByGuild(String guildId) {
    return battleRepository.findByGuildId(guildId);
  }

  @Override
  public boolean hasBattleInProgress(String guildId) throws BattleNotFoundException {
    return !getBattleByGuild(guildId)
        .orElseThrow(() -> new BattleNotFoundException("No battle found for guild: " + guildId))
        .getBattleStatus()
        .isOver();
  }

  // --- Boss info ---
  @Override
  public String getGuildId(String battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getGuildId();
  }

  @Override
  public BossEnemy getBoss(String battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBoss();
  }

  @Override
  public BossStatus getBossRemainingHealth(String battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBossRemainingHealth();
  }

  // --- Turn management ---
  @Override
  public Integer getCurrentTurn(String battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getCurrentTurn();
  }

  @Override
  public Integer getNumOfTurns(String battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getNumOfTurns();
  }

  @Override
  public void nextTurn(String battleId) throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    battle.nextTurn();
    battleRepository.save(battle);
  }

  @Override
  public void increaseNumOfTurn(String battleId, String memberId) throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    battle.increaseNumOfTurns(memberId);
    battleRepository.save(battle);
  }

  @Override
  public void decreaseNumOfTurn(String battleId, String memberId) throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    battle.decreaseNumOfTurns(memberId);
    battleRepository.save(battle);
  }

  // --- Battle ---
  @Override
  public BattleOutcome dealDamageOnBoss(String battleId, String attackerId, int damage)
      throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    var outcome = battle.dealDamageOnBoss(attackerId, damage);
    if (outcome instanceof BattleOutcome.Won(int experienceReward, int moneyReward)) {
      battleObserver.notifyBattleEvent(
          new BattleWon(battleId, battle.getGuildId(), experienceReward, moneyReward));
      battleRepository.deleteById(battleId);
    } else {
      battleRepository.save(battle);
    }
    return outcome;
  }

  @Override
  public BattleOutcome applyCounterattack(String battleId, String attackerId)
      throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    var outcome = battle.applyCounterattack(attackerId);
    if (outcome instanceof BattleOutcome.Lost(int penalty)) {
      battleObserver.notifyBattleEvent(new BattleLost(battleId, battle.getGuildId(), penalty));
      battleRepository.deleteById(battleId);
    } else {
      battleRepository.save(battle);
    }
    return outcome;
  }

  //  @Override
  //  public BattleOutcome dealDamage(String battleId, String attackerId, int damage, boolean
  // attackerDied)
  //          throws BattleNotFoundException {
  //    Battle battle = getBattleById(battleId);
  //    BossEnemy boss = battle.getBoss();
  //    battle.dealDamage(damage);
  //
  //    if (battle.getBattleStatus() == BattleStatus.WON) {
  //      battleObserver.notifyBattleEvent(
  //              new BattleWon(battleId, battle.getGuildId(), boss.experienceReward(),
  // boss.moneyReward()));
  //      battleRepository.save(battle);
  //      return new BattleOutcome.Won(boss.experienceReward().amount(),
  // boss.moneyReward().amount());
  //    }
  //
  //    if (attackerDied) {
  //      battle.markAsFallen(attackerId);
  //      if (battle.getBattleStatus() == BattleStatus.LOST) {
  //        battleObserver.notifyBattleEvent(
  //                new BattleLost(battleId, battle.getGuildId(), boss.penalty()));
  //        battleRepository.save(battle);
  //        return new BattleOutcome.Lost(boss.penalty().amount());
  //      }
  //    }
  //
  //    battleRepository.save(battle);
  //    return new BattleOutcome.Ongoing(attackerId, attackerDied);
  //  }

  @Override
  public boolean isAttackerTurn(String battleId, String attackerId) throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    return battle.isAttackerTurn(attackerId);
  }

  @Override
  public BattleOutcome getBattleStatus(String battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBattleStatus();
  }

  @Override
  public boolean isBattleOver(String battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBattleStatus().isOver();
  }

  @Override
  public boolean isBattleWon(String battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBattleStatus().isWon();
  }
}
