package habitquest.tracking.domain;

import common.ddd.ValueObject;

import java.util.Objects;

public record Tag(String name) implements ValueObject {
    public Tag {
        Objects.requireNonNull(name, "Tag name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Tag name cannot be blank");
        }
    }
}
