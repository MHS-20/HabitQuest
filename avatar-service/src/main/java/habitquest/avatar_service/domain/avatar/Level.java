package habitquest.avatar_service.domain.avatar;

import common.ddd.ValueObject;
import java.util.Objects;

public record Level(Integer levelNumber, Experience experienceRequired) implements ValueObject {
  private static final int MIN_LEVEL = 1;

  public Level {
    Objects.requireNonNull(experienceRequired);
    if (levelNumber < MIN_LEVEL) {
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
