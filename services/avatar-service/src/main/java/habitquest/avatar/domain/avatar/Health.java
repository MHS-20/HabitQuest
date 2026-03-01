package habitquest.avatar.domain.avatar;

public record Health(Integer value) {
  public Health {
    if (value < 0) {
      throw new IllegalArgumentException("Health cannot be negative");
    }
  }
}
