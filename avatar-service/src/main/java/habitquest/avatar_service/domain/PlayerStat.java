package habitquest.avatar_service.domain;

import common.ddd.ValueObject;

public interface PlayerStat extends ValueObject {
    PlayerStat increment();

    int value();
}
