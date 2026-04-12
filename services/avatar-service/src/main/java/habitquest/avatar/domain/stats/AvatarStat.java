package habitquest.avatar.domain.stats;

import common.ddd.ValueObject;

public sealed interface AvatarStat extends ValueObject permits Strength, Defense, Intelligence {
  AvatarStat increment();

  Integer value();
}
