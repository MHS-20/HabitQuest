package habitquest.guild.domain.battle.boss;

import common.ddd.ValueObject;
import habitquest.guild.domain.battle.stats.Stats;

public interface BossEnemy extends ValueObject {
  String id();

  String name();

  Stats stats();
}
