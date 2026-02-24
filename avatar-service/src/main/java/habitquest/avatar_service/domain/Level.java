package habitquest.avatar_service.domain;

import common.ddd.ValueObject;

import java.util.Objects;

public record Level(Integer levelNumber, Experience experienceRequired) implements ValueObject {
    public Level {
        Objects.requireNonNull(experienceRequired);
        if (levelNumber < 1) {
            throw new IllegalArgumentException("Level number must be at least 1");
        }
    }

    public boolean canLevelUp(Experience currentExperience) {
        return currentExperience.isAtLeast(experienceRequired);
    }

    public Level levelUp(Experience nextLevelRequirement) {
        return new Level(levelNumber + 1, nextLevelRequirement);
    }
}
