package habitquest.guild.domain.battle.stats;

import common.ddd.ValueObject;

public interface Stat extends ValueObject {
  Stat increment();

  Integer value();
}
