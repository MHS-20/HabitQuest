package habitquest.guild.application;

import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleStatus;
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

  // --- Query ---
  @Override
  public Optional<Battle> getBattleByGuild(String guildId) {
    return battleRepository.findByGuildId(guildId);
  }

  @Override
  public boolean hasBattleInProgress(String guildId) throws BattleNotFoundException {
    return getBattleByGuild(guildId)
            .orElseThrow(() -> new BattleNotFoundException("No battle found for guild: " + guildId))
            .getBattleStatus()
        == BattleStatus.ONGOING;
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
  public void increaseNumOfTurn(String battleId) throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    battle.increaseNumOfTurns();
    battleRepository.save(battle);
  }

  @Override
  public void decreaseNumOfTurn(String battleId) throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    battle.decreaseNumOfTurns();
    battleRepository.save(battle);
  }

  // --- Combat ---
  @Override
  public void dealDamage(String battleId, Integer damage) throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    battle.dealDamage(damage);
    battleRepository.save(battle);

    if (battle.getBattleStatus() == BattleStatus.WON) {
      var boss = this.getBoss(battleId);
      battleObserver.notifyBattleEvent(
          new BattleWon(
              battleId, battle.getGuildId(), boss.experienceReward(), boss.moneyReward()));
    }

    if (battle.getBattleStatus() == BattleStatus.LOST) {
      var boss = this.getBoss(battleId);
      battleObserver.notifyBattleEvent(
          new BattleLost(battleId, battle.getGuildId(), boss.penalty()));
    }
  }

  @Override
  public BattleStatus getBattleStatus(String battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBattleStatus();
  }

  @Override
  public boolean isBattleOver(String battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBattleStatus() != BattleStatus.ONGOING;
  }

  @Override
  public boolean isBattleWon(String battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBattleStatus() == BattleStatus.WON;
  }
}
