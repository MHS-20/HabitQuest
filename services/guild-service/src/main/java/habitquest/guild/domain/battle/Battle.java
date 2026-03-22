package habitquest.guild.domain.battle;

import common.ddd.Aggregate;
import common.ddd.Id;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.battle.stats.Health;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import java.util.*;

public class Battle implements Aggregate<Id<Battle>> {
  private final Id<Battle> battleId;
  private final Id<Guild> guildId;
  private final BossEnemy boss;
  private Integer numOfTurns;
  private Integer currentTurn;
  private BossStatus bossRemainingHealth;
  private BattleOutcome battleStatus;
  private final List<Id<GuildMember>> memberIds;
  private final Set<Id<GuildMember>> fallenAvatarIds;
  private static final int MIN_NUM_OF_TURNS = 1;

  public Battle(Id<Battle> battleId, Id<Guild> guildId, BossEnemy boss, Integer numOfTurns) {
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

  public Id<Guild> getGuildId() {
    return guildId;
  }

  public Id<Battle> getId() {
    return this.battleId;
  }

  public BossEnemy getBoss() {
    return boss;
  }

  public List<Id<GuildMember>> getMembers() {
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

  public boolean isAttackerTurn(Id<GuildMember> memberId) {
    return memberIds.get(currentTurn).equals(memberId);
  }

  public void increaseNumOfTurns(Id<GuildMember> memberId) {
    numOfTurns++;
    this.memberIds.add(memberId);
  }

  public void decreaseNumOfTurns(Id<GuildMember> memberId) {
    numOfTurns--;
    this.memberIds.remove(memberId);
    this.fallenAvatarIds.remove(memberId);
  }

  public BossStatus getBossRemainingHealth() {
    return bossRemainingHealth;
  }

  public BattleOutcome dealDamageOnBoss(Id<GuildMember> attackerId, int damage) {
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

  public BattleOutcome applyCounterattack(Id<GuildMember> attackerId) {
    fallenAvatarIds.add(attackerId);
    if (fallenAvatarIds.size() >= numOfTurns) {
      this.battleStatus = new BattleOutcome.Lost(boss.penalty().amount());
    }
    return battleStatus;
  }

  public BattleOutcome getBattleStatus() {
    return battleStatus;
  }

  public void markAsFallen(Id<GuildMember> avatarId) {
    fallenAvatarIds.add(avatarId);
    if (fallenAvatarIds.size() >= numOfTurns) {
      this.battleStatus = new BattleOutcome.Lost(boss.penalty().amount());
    }
  }

  public boolean hasFallen(Id<GuildMember> avatarId) {
    return fallenAvatarIds.contains(avatarId);
  }

  public Set<Id<GuildMember>> getFallenAvatarIds() {
    return Collections.unmodifiableSet(fallenAvatarIds);
  }
}
