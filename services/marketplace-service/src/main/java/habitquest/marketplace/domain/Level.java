package habitquest.marketplace.domain;

import common.ddd.ValueObject;

public record Level(Integer levelNumber) implements ValueObject {
  private static final int MIN_LEVEL = 1;

  public Level {
    if (levelNumber < MIN_LEVEL) {
      throw new IllegalArgumentException("Level number must be at least 1");
    }
  }
}
