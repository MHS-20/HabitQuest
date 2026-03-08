package habitquest.guild.domain.battle.boss;

import habitquest.guild.domain.battle.Experience;
import habitquest.guild.domain.battle.Money;
import habitquest.guild.domain.battle.Penalty;
import habitquest.guild.domain.battle.stats.Defense;
import habitquest.guild.domain.battle.stats.Health;
import habitquest.guild.domain.battle.stats.Stats;
import habitquest.guild.domain.battle.stats.Strength;

public record Minotaur(
    String name, Stats stats, Money moneyReward, Penalty penalty, Experience experienceReward)
    implements BossEnemy {

  public static final Minotaur INSTANCE =
      new Minotaur(
          "Minotaur",
          new Stats("minotaur-stats", new Health(1000), new Strength(150), new Defense(50)),
          new Money(100),
          new Penalty(50),
          new Experience(200));
}
