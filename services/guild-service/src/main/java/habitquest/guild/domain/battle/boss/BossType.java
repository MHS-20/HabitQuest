package habitquest.guild.domain.battle.boss;

import habitquest.guild.domain.battle.Experience;
import habitquest.guild.domain.battle.Money;
import habitquest.guild.domain.battle.Penalty;
import habitquest.guild.domain.battle.stats.Defense;
import habitquest.guild.domain.battle.stats.Health;
import habitquest.guild.domain.battle.stats.Stats;
import habitquest.guild.domain.battle.stats.Strength;

@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "PMD.NonSerializableClass"})
public enum BossType implements BossEnemy {
  MINOTAUR(
      "Minotaur",
      new Stats("minotaur-stats", new Health(100), new Strength(150), new Defense(50)),
      new Money(100),
      new Penalty(50),
      new Experience(200));

  private final String name;
  private final Stats stats;
  private final Money moneyReward;
  private final Penalty penalty;
  private final Experience experienceReward;

  BossType(
      String name, Stats stats, Money moneyReward, Penalty penalty, Experience experienceReward) {
    this.name = name;
    this.stats = stats;
    this.moneyReward = moneyReward;
    this.penalty = penalty;
    this.experienceReward = experienceReward;
  }

  @Override
  public Stats stats() {
    return stats;
  }

  @Override
  public Money moneyReward() {
    return moneyReward;
  }

  @Override
  public Penalty penalty() {
    return penalty;
  }

  @Override
  public Experience experienceReward() {
    return experienceReward;
  }
}
