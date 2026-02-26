package habitquest.avatar.domain.stats;

import common.ddd.ValueObject;

public interface AvatarStat extends ValueObject {
  AvatarStat increment();

  Integer value();
}
