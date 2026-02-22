package habitquest.avatar_service.domain;

import common.ddd.ValueObject;

public record Item(String name, String description) implements ValueObject {
}
