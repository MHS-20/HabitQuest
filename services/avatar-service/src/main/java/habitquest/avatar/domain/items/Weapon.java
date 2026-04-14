package habitquest.avatar.domain.items;

public record Weapon(BaseItem baseItem) implements Equipment {
  public Weapon(String name, String description, int power) {
    this(new BaseItem(name, description, power));
  }
}
