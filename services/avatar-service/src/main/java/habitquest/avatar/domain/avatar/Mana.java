package habitquest.avatar.domain.avatar;

public record Mana(Integer value) {
  public Mana {
    if (value < 0) {
      throw new IllegalArgumentException("Mana cannot be negative");
    }
  }
}
