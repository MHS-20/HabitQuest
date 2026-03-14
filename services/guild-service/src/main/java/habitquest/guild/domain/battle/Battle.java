package habitquest.guild.domain.battle;

import common.ddd.Aggregate;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.battle.stats.Health;

public class Battle implements Aggregate<String> {
  private final String battleId;
  private final String guildId;
  private final BossEnemy boss;
  private Integer numOfTurns;
  private Integer currentTurn;
  private BossStatus bossRemainingHealth;
  private BattleStatus battleStatus;
  private static final int MIN_NUM_OF_TURNS = 1;

  public Battle(
      String battleId,
      String guildId,
      BossEnemy boss,
      Integer numOfTurns,
      Integer currentTurn,
      BossStatus bossRemainingHealth) {
    this.battleId = battleId;
    this.guildId = guildId;
    this.boss = boss;
    this.numOfTurns = numOfTurns;
    this.currentTurn = currentTurn;
    this.bossRemainingHealth = bossRemainingHealth;
    this.battleStatus = BattleStatus.ONGOING;
  }

  public String getGuildId() {
    return guildId;
  }

  public BossEnemy getBoss() {
    return boss;
  }

  public void nextTurn() {
    currentTurn = (currentTurn + 1) % numOfTurns;
  }

  public Integer getCurrentTurn() {
    return currentTurn;
  }

  public Integer getNumOfTurns() {
    return numOfTurns;
  }

  public void increaseNumOfTurns() {
    numOfTurns++;
  }

  public void decreaseNumOfTurns() {
    if (numOfTurns > MIN_NUM_OF_TURNS) {
      numOfTurns--;
    }
  }

  public BossStatus getBossRemainingHealth() {
    return bossRemainingHealth;
  }

  public void dealDamage(Integer damage) {
    int newHealth = bossRemainingHealth.remainingHealth().value() - damage;
    if (newHealth <= 0) {
      this.battleStatus = BattleStatus.WON;
      bossRemainingHealth = new BossStatus(new Health(0));
    } else {
      bossRemainingHealth = new BossStatus(new Health(newHealth));
    }
  }

  public BattleStatus getBattleStatus() {
    return battleStatus;
  }

  public String getId() {
    return this.battleId;
  }
}
