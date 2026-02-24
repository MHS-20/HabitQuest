package habitquest.avatar_service.domain.stats;

import common.ddd.ValueObject;

public interface AvatarStat extends ValueObject {
  AvatarStat increment();

  Integer value();
}
