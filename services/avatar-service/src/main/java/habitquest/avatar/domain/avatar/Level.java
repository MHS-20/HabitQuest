package habitquest.avatar.domain.avatar;

import common.ddd.ValueObject;
import java.util.Objects;

public record Level(
    Integer levelNumber, Experience currentExperience, Experience experienceRequired)
    implements ValueObject {
  private static final int MIN_LEVEL = 1;

  public Level {
    Objects.requireNonNull(experienceRequired);
    if (levelNumber < MIN_LEVEL) {
      throw new IllegalArgumentException("Level number must be at least 1");
    }
  }

  public Level gainExperience(Experience gainedExperience) {
    if (gainedExperience.amount() < 0) {
      throw new IllegalArgumentException("Gained experience cannot be negative");
    }

    if (this.currentExperience.add(gainedExperience).isAtLeast(experienceRequired)) {
      return new Level(
          levelNumber + 1, new Experience(0), experienceRequired.add(experienceRequired));
    } else {
      return new Level(
          this.levelNumber, this.currentExperience.add(gainedExperience), experienceRequired);
    }
  }

  public Level resetExperience() {
    return new Level(this.levelNumber, new Experience(0), this.experienceRequired);
  }
}
