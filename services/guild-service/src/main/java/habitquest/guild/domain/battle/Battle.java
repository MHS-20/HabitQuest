package habitquest.guild.domain.battle;

import common.ddd.Aggregate;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.battle.stats.Health;
import java.util.*;

public class Battle implements Aggregate<String> {
  private final String battleId;
  private final String guildId;
  private final BossEnemy boss;
  private Integer numOfTurns;
  private Integer currentTurn;
  private BossStatus bossRemainingHealth;
  private BattleOutcome battleStatus;
  private List<String> memberIds;
  private final Set<String> fallenAvatarIds;
  private static final int MIN_NUM_OF_TURNS = 1;

  public Battle(String battleId, String guildId, BossEnemy boss, Integer numOfTurns) {
    this.battleId = battleId;
    this.guildId = guildId;
    this.boss = boss;
    this.numOfTurns = numOfTurns;
    this.currentTurn = 0;
    this.bossRemainingHealth = new BossStatus(boss.stats().health());
    this.memberIds = new ArrayList<>();
    this.fallenAvatarIds = new HashSet<>();
    this.battleStatus = new BattleOutcome.Ongoing();
  }

  public String getGuildId() {
    return guildId;
  }

  public String getId() {
    return this.battleId;
  }

  public BossEnemy getBoss() {
    return boss;
  }

  public List<String> getMembers() {
    return Collections.unmodifiableList(memberIds);
  }

  public void nextTurn() {
    int attempts = 0;
    do {
      currentTurn = (currentTurn + 1) % numOfTurns;
      attempts++;
    } while (fallenAvatarIds.contains(memberIds.get(currentTurn)) && attempts < numOfTurns);
  }

  public Integer getCurrentTurn() {
    return currentTurn;
  }

  public Integer getNumOfTurns() {
    return numOfTurns;
  }

  public boolean isAttackerTurn(String memberId) {
    return memberIds.get(currentTurn).equals(memberId);
  }

  public void increaseNumOfTurns(String memberId) {
    numOfTurns++;
    this.memberIds.add(memberId);
  }

  public void decreaseNumOfTurns(String memberId) {
    numOfTurns--;
    this.memberIds.remove(memberId);
    this.fallenAvatarIds.remove(memberId);
  }

  public BossStatus getBossRemainingHealth() {
    return bossRemainingHealth;
  }

  public BattleOutcome dealDamageOnBoss(String attackerId, int damage) {
    int newHealth = bossRemainingHealth.remainingHealth().value() - damage;
    if (newHealth <= 0) {
      bossRemainingHealth = new BossStatus(new Health(0));
      this.battleStatus =
          new BattleOutcome.Won(boss.experienceReward().amount(), boss.moneyReward().amount());
    } else {
      bossRemainingHealth = new BossStatus(new Health(newHealth));
    }
    return battleStatus;
  }

  public BattleOutcome applyCounterattack(String attackerId) {
    fallenAvatarIds.add(attackerId);
    if (fallenAvatarIds.size() >= numOfTurns) {
      this.battleStatus = new BattleOutcome.Lost(boss.penalty().amount());
    }
    return battleStatus;
  }

  //  public void dealDamage(Integer damage) {
  //    int newHealth = bossRemainingHealth.remainingHealth().value() - damage;
  //    if (newHealth <= 0) {
  //      this.battleStatus = BattleStatus.WON;
  //      bossRemainingHealth = new BossStatus(new Health(0));
  //    } else {
  //      bossRemainingHealth = new BossStatus(new Health(newHealth));
  //    }
  //  }

  public BattleOutcome getBattleStatus() {
    return battleStatus;
  }

  public void markAsFallen(String avatarId) {
    fallenAvatarIds.add(avatarId);
    if (fallenAvatarIds.size() >= numOfTurns) {
      this.battleStatus = new BattleOutcome.Lost(boss.penalty().amount());
    }
  }

  public boolean hasFallen(String avatarId) {
    return fallenAvatarIds.contains(avatarId);
  }

  public Set<String> getFallenAvatarIds() {
    return Collections.unmodifiableSet(fallenAvatarIds);
  }
}
