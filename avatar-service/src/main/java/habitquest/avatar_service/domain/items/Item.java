package habitquest.avatar_service.domain.items;

import common.ddd.ValueObject;

public interface Item extends ValueObject {
    String name();
    String description();
}
