package habitquest.guild.domain.battle.boss;

import common.ddd.ValueObject;
import habitquest.guild.domain.battle.stats.Stats;

public interface BossEnemy extends ValueObject {

  String name();

  Stats stats();
}
