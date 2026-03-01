package habitquest.guild.domain.battle.boss;

import common.ddd.ValueObject;
import habitquest.guild.domain.battle.Experience;
import habitquest.guild.domain.battle.Money;
import habitquest.guild.domain.battle.Penalty;
import habitquest.guild.domain.battle.stats.Stats;

public interface BossEnemy extends ValueObject {

  String name();

  Stats stats();

  Money moneyReward();

  Penalty penalty();

  Experience experienceReward();
}
