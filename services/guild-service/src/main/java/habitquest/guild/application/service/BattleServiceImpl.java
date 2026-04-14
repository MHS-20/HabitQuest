package habitquest.guild.application.service;

import common.ddd.Id;
import habitquest.guild.application.exceptions.BattleNotFoundException;
import habitquest.guild.application.port.in.BattleService;
import habitquest.guild.application.port.out.AvatarClientPort;
import habitquest.guild.application.port.out.BattleRepository;
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
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BattleServiceImpl implements BattleService {
  private final BattleRepository battleRepository;
  private final BattleObserver battleObserver;
  private final BattleFactory battleFactory;
  private final AvatarClientPort avatarPort;

  public BattleServiceImpl(
      BattleRepository battleRepository,
      BattleObserver battleObserver,
      BattleFactory battleFactory,
      AvatarClientPort avatarPort) {
    this.battleRepository = battleRepository;
    this.battleObserver = battleObserver;
    this.battleFactory = battleFactory;
    this.avatarPort = avatarPort;
  }

  // --- Battle lifecycle ---
  @Override
  public Id<Battle> createBattle(
      Id<Guild> guildId, BossType bossType, Integer numOfTurns, List<Id<GuildMember>> members) {
    Battle battle = battleFactory.create(guildId, bossType, numOfTurns, members);
    battleRepository.save(battle);
    battleObserver.notifyBattleEvent(new BattleStarted(battle.getId(), battle.getGuildId()));
    return battle.getId();
  }

  @Override
  public void deleteBattle(Id<Battle> battleId) {
    battleRepository.deleteById(battleId);
  }

  @Override
  public Battle getBattleById(Id<Battle> battleId) throws BattleNotFoundException {
    return battleRepository
        .findById(battleId)
        .orElseThrow(() -> new BattleNotFoundException(battleId.value()));
  }

  public BattleOutcome markAsFallen(Id<Battle> battleId, Id<GuildMember> avatarId)
      throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    battle.markAsFallen(avatarId);
    if (battle.getBattleStatus().isLost()) {
      var boss = this.getBoss(battleId);
      battleObserver.notifyBattleEvent(
          new BattleLost(battleId, battle.getGuildId(), boss.penalty().amount()));
      battleRepository.deleteById(battleId);
    } else {
      battleRepository.save(battle);
    }
    return battle.getBattleStatus();
  }

  // --- Query ---
  @Override
  public Optional<Battle> getBattleByGuild(Id<Guild> guildId) {
    return battleRepository.findByGuildId(guildId);
  }

  @Override
  public boolean hasBattleInProgress(Id<Guild> guildId) throws BattleNotFoundException {
    return !getBattleByGuild(guildId)
        .orElseThrow(() -> new BattleNotFoundException("No battle found for guild: " + guildId))
        .getBattleStatus()
        .isOver();
  }

  // --- Boss info ---
  @Override
  public Id<Guild> getGuildId(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getGuildId();
  }

  @Override
  public BossEnemy getBoss(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBoss();
  }

  @Override
  public List<BossEnemy> getAllBossTypes() {
    return BossType.allBossTypes();
  }

  @Override
  public BossStatus getBossRemainingHealth(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBossRemainingHealth();
  }

  // --- Turn management ---
  @Override
  public Integer getCurrentTurn(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getCurrentTurn();
  }

  @Override
  public Integer getNumOfTurns(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getNumOfTurns();
  }

  @Override
  public void nextTurn(Id<Battle> battleId) throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    battle.nextTurn();
    battleRepository.save(battle);
  }

  @Override
  public void increaseNumOfTurn(Id<Battle> battleId, Id<GuildMember> memberId)
      throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    battle.increaseNumOfTurns(memberId);
    battleRepository.save(battle);
  }

  @Override
  public void decreaseNumOfTurn(Id<Battle> battleId, Id<GuildMember> memberId)
      throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    battle.decreaseNumOfTurns(memberId);
    battleRepository.save(battle);
  }

  // --- Battle ---
  @Override
  public BattleOutcome processDamage(Id<Battle> battleId, Id<GuildMember> attackerId, int damage)
      throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    BattleOutcome outcome = battle.dealDamageOnBoss(attackerId, damage);

    if (outcome instanceof BattleOutcome.Ongoing) {
      boolean attackerDied = avatarPort.applyDamage(attackerId.value(), damage).died();
      if (attackerDied) {
        outcome = battle.applyCounterattack(attackerId);
        if (outcome instanceof BattleOutcome.Lost(int penalty)) {
          battleObserver.notifyBattleEvent(new BattleLost(battleId, battle.getGuildId(), penalty));
          battle.getMembers().forEach(m -> avatarPort.applyPenalty(m.value(), penalty));
          battleRepository.deleteById(battleId);
        } else {
          battleRepository.save(battle);
        }
      } else {
        nextTurn(battleId);
      }
      return outcome;
    }

    if (outcome instanceof BattleOutcome.Won(int experienceReward, int moneyReward)) {
      battleObserver.notifyBattleEvent(
          new BattleWon(battleId, battle.getGuildId(), experienceReward, moneyReward));
      battle
          .getMembers()
          .forEach(
              m -> {
                avatarPort.grantExperience(m.value(), experienceReward);
                avatarPort.earnMoney(m.value(), moneyReward);
              });
      battleRepository.deleteById(battleId);
    } else if (outcome instanceof BattleOutcome.Lost(int penalty)) {
      battleObserver.notifyBattleEvent(new BattleLost(battleId, battle.getGuildId(), penalty));
      battle.getMembers().forEach(m -> avatarPort.applyPenalty(m.value(), penalty));
      battleRepository.deleteById(battleId);
    }

    return outcome;
  }

  @Override
  public BattleOutcome dealDamageOnBoss(Id<Battle> battleId, Id<GuildMember> attackerId, int damage)
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
  public BattleOutcome applyCounterattack(Id<Battle> battleId, Id<GuildMember> attackerId)
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

  @Override
  public boolean isAttackerTurn(Id<Battle> battleId, Id<GuildMember> attackerId)
      throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    return battle.isAttackerTurn(attackerId);
  }

  @Override
  public BattleOutcome getBattleStatus(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBattleStatus();
  }

  @Override
  public boolean isBattleOver(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBattleStatus().isOver();
  }

  @Override
  public boolean isBattleWon(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBattleStatus().isWon();
  }
}
