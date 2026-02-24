package habitquest.avatar_service.domain;

public record BaseStat(Integer value) {
  public BaseStat {
    if (value < 0) {
      throw new IllegalArgumentException("Stat value cannot be negative");
    }
  }

  public BaseStat increment() {
    return new BaseStat(value + 1);
  }
}
