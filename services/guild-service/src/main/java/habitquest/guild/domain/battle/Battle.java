package habitquest.guild.domain.battle;

import common.ddd.Aggregate;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;

public class Battle implements Aggregate<String> {
  private String battleId;
  private String guildId;
  private BossEnemy boss;
  private Integer numOfTurns;
  private Integer currentTurn;
  private BossStatus bossRemainingHealth;

  public void dealDamage(Integer damage) {}

  public void nextTurn() {}

  public void endBattle() {}

  public String getId() {
    return battleId;
  }
}
